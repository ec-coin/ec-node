package nl.hanze.ec.node.network.peers;

import com.google.inject.Inject;
import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.*;
import nl.hanze.ec.node.modules.annotations.*;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.requests.NeighborsRequest;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerConnection;
import nl.hanze.ec.node.network.peers.peer.PeerConnectionFactory;
import nl.hanze.ec.node.network.peers.peer.PeerState;
import nl.hanze.ec.node.utils.HashingUtils;
import nl.hanze.ec.node.utils.SignatureUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.io.IOException;
import java.net.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PeerPool implements Runnable {
    private static final Logger logger = LogManager.getLogger(PeerPool.class);
    private final int maxPeers;
    private final int minPeers;
    private final BlockingQueue<Socket> incomingConnectionsQueue;
    private final BlockingQueue<NodeState> nodeStateQueue;
    private final static int transactionThreshold = 3;

    // TODO: maybe use other data structure, this list will eventually get really big
    List<Command> receivedAnnouncements = new LinkedList<>();

    /**
     * List of all the peers we have tried to connect
     */
    List<Peer> triedPeers = new LinkedList<>();

    /**
     * Datetime when last searched for new peers
     */
    private DateTime lastSearchedForNewPeers = new DateTime(0);

    /**
     * Datetime when last cleared askingNeigbours and triedPeers
     */
    private DateTime lastClear = new DateTime();

    /**
     * All neighbours we asked for their peers
     */
    private final Set<Peer> askedNeighbours = new HashSet<>();

    /**
     * Time between searching new peers
     */
    private int searchDelta = 10;

    /**
     * Port we run the server on
     */
    private final int port;

    /**
     * Maps each peer to their command queue
     */
    private final ConcurrentMap<Peer, BlockingQueue<Command>> connectedPeers = new ConcurrentHashMap<>();

    /**
     * Factory to create new PeerConnection objects.
     */
    private final PeerConnectionFactory peerConnectionFactory;

    /**
     * Is the PeerPool running?
     */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Neighbours repo
     */
    private final NeighboursRepository neighboursRepository;

    /**
     * BalancesCache repo
     */
    private final BalancesCacheRepository balancesCacheRepository;

    /**
     * Block repo
     */
    private final BlockRepository blockRepository;

    /**
     * Transaction repo
     */
    private final TransactionRepository transactionRepository;

    /**
     * Own IPS. Do not connect to.
     */
    private final Set<String> ownIPs = new HashSet<>();

    @Inject
    public PeerPool(
        @MaxPeers int maxPeers,
        @MinPeers int minPeers,
        @Port int port,
        @IncomingConnectionsQueue BlockingQueue<Socket> incomingConnectionsQueue,
        PeerConnectionFactory peerConnectionFactory,
        NeighboursRepository neighboursRepository,
        BalancesCacheRepository balancesCacheRepository,
        BlockRepository blockRepository,
        TransactionRepository transactionRepository,
        @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue
    ) {
        this.neighboursRepository = neighboursRepository;
        this.balancesCacheRepository = balancesCacheRepository;
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.maxPeers = maxPeers;
        this.minPeers = minPeers;
        this.incomingConnectionsQueue = incomingConnectionsQueue;
        this.nodeStateQueue = nodeStateQueue;
        this.peerConnectionFactory = peerConnectionFactory;
        this.port = port;

        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = e.nextElement();
                Enumeration<InetAddress> ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = ee.nextElement();
                    ownIPs.add(i.getHostAddress());
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }

    private Peer getNewPeer() {
        List<Peer> peers = new ArrayList<>();

        peers.add(new Peer("seed001.ec.dylaan.nl", port));
        peers.add(new Peer("seed002.ec.dylaan.nl", port));
        peers.add(new Peer("seed003.ec.dylaan.nl", port));

        Collections.shuffle(peers);

        List<Peer> previousPeers = this.neighboursRepository.getAllNeighbours().stream()
                .map(n -> new Peer(n.getIp(), n.getPort()))
                .collect(Collectors.toList());

        Collections.shuffle(previousPeers);

        peers.addAll(previousPeers);
        peers.removeAll(connectedPeers.keySet());
        peers.removeAll(triedPeers);

        if (peers.size() < 1) {
            return null;
        }

        Peer newPeer = peers.get(peers.size() - 1);
        triedPeers.add(newPeer);
        return newPeer;
    }

    @Override
    public void run() {
        while (true) {
            //################################
            //  Handle incoming socket connections
            //################################
            Socket socket;
            while ((socket = incomingConnectionsQueue.poll()) != null) {
                if (ownIPs.contains(socket.getInetAddress().toString())) {
                    continue;
                }

                if (Math.max(maxPeers - connectedPeers.size(), 0) == 0) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {}
                    continue;
                }

                Peer peer = new Peer(socket.getInetAddress().getHostAddress(), socket.getPort());
                peer.setState(PeerState.CONNECTING);

                if (connectedPeers.containsKey(peer)) {
                    continue;
                }

                BlockingQueue<Command> commandsQueue = new LinkedBlockingQueue<>();
                (new Thread(peerConnectionFactory.create(peer, commandsQueue, socket, this))).start();

                neighboursRepository.updateNeighbour(socket.getInetAddress().getHostAddress(), port);
                connectedPeers.put(peer, commandsQueue);
            }

            //################################
            //  Discover more peers if connected peers size < min peers
            //################################
            DateTime now = new DateTime();
            if (Math.max(minPeers - connectedPeers.size(), 0) > 0 &&
                Seconds.secondsBetween(lastSearchedForNewPeers, now).getSeconds() >= searchDelta) {
                searchDelta = 10;
                lastSearchedForNewPeers = now;

                logger.info("Started searching for another peer...");
                Peer newPeer = getNewPeer();
                if (newPeer == null && connectedPeers.isEmpty()) {
                    logger.info("Could not find any other peers and not connected to any other peer.");
                } else if (newPeer == null) {
                    logger.info("Could not find any other peers and asking connect peers for their neighbours.");

                    for (Peer connected : connectedPeers.keySet()) {
                        if (askedNeighbours.contains(connected)) {
                            continue;
                        }

                        sendCommand(connected, new NeighborsRequest());
                        askedNeighbours.add(connected);
                    }
                } else if (ownIPs.contains(newPeer.getIp())) {
                    logger.debug("New peer is myself. Skipping");
                    searchDelta = 0;
                } else {
                    logger.info("Trying to connect to known peer: " + newPeer);
                    BlockingQueue<Command> commandsQueue = new LinkedBlockingQueue<>();

                    PeerConnection peerConnection;
                    try {
                        peerConnection = peerConnectionFactory.create(newPeer, commandsQueue, this);
                        (new Thread(peerConnection)).start();
                        connectedPeers.put(newPeer, commandsQueue);
                        newPeer.setState(PeerState.CONNECTING);
                    } catch (UnknownHostException e) {
                        logger.warn("Unknown host: " + newPeer);
                        searchDelta = 0;
                    } catch (IOException e) {
                        logger.warn("I/O error occurred when creating the socket " + newPeer);
                        searchDelta = 0;
                    }

                    neighboursRepository.updateNeighbour(newPeer.getIp(), port);
                }
            }

            //################################
            //  Change state according to connected peers size
            //################################
            if (connectedPeers.size() >= minPeers && Application.getState() == NodeState.COM_SETUP) {
                NodeState newState = NodeState.PARTICIPATING;

                // If start height differs by more than 5 blocks: Initiate a Blockchain Sync
                for (Peer peer : connectedPeers.keySet()) {
                    int blockStartHeight = blockRepository.getCurrentBlockHeight();
                    if (peer.getStartHeight() - blockStartHeight >= 5) {
                        newState = NodeState.SYNCING;
                        break;
                    }
                }

                nodeStateQueue.add(newState);
            }

            // Revert state to COM_SETUP when number of peers is less than minPeers
            if (Application.getState() != NodeState.COM_SETUP && connectedPeers.size() < minPeers) {
                nodeStateQueue.add(NodeState.COM_SETUP);
            }

            //################################
            //  Reset internal cache
            //################################
            if (Seconds.secondsBetween(lastClear, now).getSeconds() >= 30) {
                lastClear = now;
                triedPeers.clear();
                askedNeighbours.clear();
            }

            removeDeadPeers();
        }
    }

    private void removeDeadPeers() {
        for (Peer peer : connectedPeers.keySet()) {
            if (peer.getState() == PeerState.CLOSED) {
                logger.info(peer + " is dead. Removing.");
                triedPeers.add(peer);
                connectedPeers.remove(peer);
            }
        }
    }

    /**
     * Retrieves all the data objects for the currently connected peers
     *
     * @return Set of peers
     */
    public Set<Peer> getConnectedPeers() {
        return connectedPeers.keySet();
    }

    /**
     * Sends a command to all neighboring nodes.
     *
     * @param command command to be broadcast
     */
    public synchronized void sendBroadcast(Command command) {
        if (receivedAnnouncements.contains(command)) {
            logger.debug("Announcement not propagated further: " + command);
            return;
        }

        receivedAnnouncements.add(command);

        for (BlockingQueue<Command> queue : connectedPeers.values()) {
            queue.add(command);
        }
    }

    /**
     * Sends a command to a specific peer
     *
     * @param peer the peer to send a command to
     * @param command the command to be sent
     */
    public void sendCommand(Peer peer, Command command) {
        connectedPeers.get(peer).add(command);
    }

    public void closeAll() {
        running.set(false);

        for (Peer peer : connectedPeers.keySet()) {
            peer.setState(PeerState.CLOSING);
        }
    }

    public static int getTransactionThreshold() {
        return transactionThreshold;
    }

    private void logDatabaseInteraction() {
        String example = "7e35543e662e1ff7e399d1ad7f92f4f3945769328ff3cf58535cf5c5529de31e";
        float balance = transactionRepository.getBalance("3333333333333333333333333333333333333333333333333333333333333333");
        float stake = transactionRepository.getStake("3333333333333333333333333333333333333333333333333333333333333333");
        System.out.println("balance before: " + balance);
        System.out.println("stake before: " + stake);
        balancesCacheRepository.updateBalanceCache("3333333333333333333333333333333333333333333333333333333333333333", balance);

        if (connectedPeers.size() == 0) {
            nodeStateQueue.add(NodeState.PARTICIPATING);
        }

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        balance = transactionRepository.getBalance("3333333333333333333333333333333333333333333333333333333333333333");
        stake = transactionRepository.getStake("3333333333333333333333333333333333333333333333333333333333333333");
        System.out.println("\nbalance after: " + balance);
        System.out.println("stake after: " + stake);
        balancesCacheRepository.updateBalanceCache("3333333333333333333333333333333333333333333333333333333333333333", balance);

        Block block = blockRepository.getCurrentBlock();

        for (Transaction transaction : block.getTransactions()) {
            System.out.println("\nIn block with hash " + block.getHash() + " there are " + block.getTransactions().size() + " transactions");
            System.out.println("hash of transaction: " + transaction.getHash() + " with status: " + transaction.getStatus());
        }

        KeyPair keyPair = SignatureUtils.generateKeyPair();
        String value = "hello";
        byte[] signature = SignatureUtils.sign(keyPair, value);
        PublicKey publicKey = keyPair.getPublic();
        boolean verified = SignatureUtils.verify(publicKey, signature, value);
        System.out.println("signature verified: " + verified);
    }
}

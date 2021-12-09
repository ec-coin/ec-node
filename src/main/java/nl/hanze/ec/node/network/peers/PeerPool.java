package nl.hanze.ec.node.network.peers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.app.handlers.StateHandler;
import nl.hanze.ec.node.database.models.BalancesCache;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.*;
import nl.hanze.ec.node.modules.annotations.IncomingConnectionsQueue;
import nl.hanze.ec.node.modules.annotations.MaxPeers;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.modules.annotations.Port;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.announcements.TestAnnouncement;
import nl.hanze.ec.node.network.peers.commands.requests.NeighborsRequest;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerConnection;
import nl.hanze.ec.node.network.peers.peer.PeerConnectionFactory;
import nl.hanze.ec.node.network.peers.peer.PeerState;
import nl.hanze.ec.node.services.HashingService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PeerPool implements Runnable {
    private static final Logger logger = LogManager.getLogger(PeerPool.class);
    private final int maxPeers;
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

    @Inject
    public PeerPool(
            @MaxPeers int maxPeers,
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
        this.incomingConnectionsQueue = incomingConnectionsQueue;
        this.nodeStateQueue = nodeStateQueue;
        this.peerConnectionFactory = peerConnectionFactory;
        this.port = port;
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
        String password = "hello this is a hashhhhhhhhhhhhhhhhh";
        String hash = HashingService.hash(password);
        System.out.println("hash: " + hash);

        boolean testing = true;
        boolean testing1 = true;
        fillDatabaseWithMockData();

        float balance = transactionRepository.getBalance("333333333333333333333333333333333333333333333333333333333333");
        float stake = transactionRepository.getStake("333333333333333333333333333333333333333333333333333333333333");
        System.out.println("balance before: " + balance);
        System.out.println("stake before: " + stake);
        balancesCacheRepository.updateBalanceCache("333333333333333333333333333333333333333333333333333333333333", balance);

        if (connectedPeers.size() == 0 && testing) {
            testing = false;
            nodeStateQueue.add(NodeState.PARTICIPATING);
        }

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        balance = transactionRepository.getBalance("333333333333333333333333333333333333333333333333333333333333");
        stake = transactionRepository.getStake("333333333333333333333333333333333333333333333333333333333333");
        System.out.println("\nbalance after: " + balance);
        System.out.println("stake before: " + stake);
        balancesCacheRepository.updateBalanceCache("333333333333333333333333333333333333333333333333333333333333", balance);

        Block block = blockRepository.getCurrentBlock();
        Iterator<Transaction> iterator = block.getTransactions().iterator();
        Transaction transaction = iterator.next();
        System.out.println("\nIn block with hash " + block.getHash() + " there are " + block.getTransactions().size() + " transactions");
        System.out.println("hash of transaction 1: " + transaction.getHash() + " with status: " + transaction.getStatus());
        transaction = iterator.next();
        System.out.println("hash of transaction 2: " + transaction.getHash() + " with status: " + transaction.getStatus());
        transaction = iterator.next();
        System.out.println("hash of transaction 3: " + transaction.getHash() + " with status: " + transaction.getStatus());

        /*while (true) {
            boolean needMorePeers = Math.max(maxPeers - connectedPeers.size(), 0) > 0;

            Socket socket;
            while ((socket = incomingConnectionsQueue.poll()) != null) {
                if (!needMorePeers) {
                    // TODO: send: not accepting new connections
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

                needMorePeers = Math.max(maxPeers - connectedPeers.size(), 0) > 0;
            }

            DateTime now = new DateTime();
            if (needMorePeers && Seconds.secondsBetween(lastSearchedForNewPeers, now).getSeconds() >= searchDelta) {
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
                } else {
                    logger.info("Found known peer. " + newPeer);
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

            if (Seconds.secondsBetween(lastClear, now).getSeconds() >= 30) {
                lastClear = now;
                triedPeers.clear();
                askedNeighbours.clear();
            }

            // Debugging purposes
            // TODO: this is for testing purposes
            if (connectedPeers.size() == 0 && testing) {
                testing = false;
                nodeStateQueue.add(NodeState.PARTICIPATING);
            }

            // TODO: this is for testing purposes
//            if (connectedPeers.size() >= 3 && testing1) {
//                testing1 = false;
//                sendBroadcast(new TestAnnouncement("Hello world"));
//            }

            removeDeadPeers();
        }*/
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
     * Sends a command to all neighboring nodes.
     *
     * @param command command to be broadcast
     */
    public synchronized void sendBroadcast(Command command) {
        if (receivedAnnouncements.contains(command)) {
            logger.fatal("Announcement not propagated further: " + command);
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

    private void fillDatabaseWithMockData() {
        // Create some mock blocks;
        String blockHash1 =         "39523F928AF4839398BDCE38111111111111111111111111111111111111";
        String previousBlockHash1 = "39523F928AF4839398BDCE38000000000000000000000000000000000000";
        String merkleRootHash = "000000000000000000000000000000000000000000000000000000000000";
        int blockheight = 0;
        blockRepository.createBlock(blockHash1, previousBlockHash1, merkleRootHash, blockheight);

        String blockHash2 =         "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String previousBlockHash2 = "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
        blockRepository.createBlock(blockHash2, previousBlockHash2, merkleRootHash, ++blockheight);

        balancesCacheRepository.getAllBalancesInCache();
        List<Block> blocks = blockRepository.getAllBlocks();

        // Create some mock transactions;
        // TODO weghalen als ongevalideerde transacties gevalideerd moeten worden.
        String status = "pending";
        float amount = 5;

        String transactionHash1 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
        String fromHash1 = "222222222222222222222222222222222222222222222222222222222222";
        String toHash1 = "333333333333333333333333333333333333333333333333333333333333";
        String signature1 = "111111111111111111111111111111111111111111111111111111111111";
        transactionRepository.createTransaction(transactionHash1, null, fromHash1, toHash1, amount, signature1, "node");

        String transactionHash2 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFAAAAA";
        String fromHash2 = "333333333333333333333333333333333333333333333333333333333333";
        String toHash2 = "3333333333333333333333333333333333333333333333333333333AAAAA";
        String signature2 = "1111111111111111111111111111111111111111111111111111111AAAAA";
        transactionRepository.createTransaction(transactionHash2, null, fromHash1, toHash1, 10, signature2, "node");

        String transactionHash3 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFBBBBB";
        String fromHash3 = "2222222222222222222222222222222222222222222222222222222BBBBB";
        String toHash3 = "3333333333333333333333333333333333333333333333333333333BBBBB";
        String signature3 = "1111111111111111111111111111111111111111111111111111111BBBBB";
        transactionRepository.createTransaction(transactionHash3, null, fromHash1, toHash2, amount, signature3, "node");

        String transactionHash4 = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFCCCCC";
        String fromHash4 = "2222222222222222222222222222222222222222222222222222222CCCCC";
        String toHash4 = "3333333333333333333333333333333333333333333333333333333CCCCC";
        String signature4 = "1111111111111111111111111111111111111111111111111111111CCCCC";
        transactionRepository.createTransaction(transactionHash4, null, fromHash4, toHash4, amount, signature4, "wallet");
    }

    public static int getTransactionThreshold() {
        return transactionThreshold;
    }
}

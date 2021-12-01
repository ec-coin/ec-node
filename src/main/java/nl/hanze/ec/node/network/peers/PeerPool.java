package nl.hanze.ec.node.network.peers;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.app.handlers.StateHandler;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.modules.annotations.IncomingConnectionsQueue;
import nl.hanze.ec.node.modules.annotations.MaxPeers;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.modules.annotations.Port;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.requests.NeighborsRequest;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerConnection;
import nl.hanze.ec.node.network.peers.peer.PeerConnectionFactory;
import nl.hanze.ec.node.network.peers.peer.PeerState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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

    @Inject
    public PeerPool(
            @MaxPeers int maxPeers,
            @Port int port,
            @IncomingConnectionsQueue BlockingQueue<Socket> incomingConnectionsQueue,
            PeerConnectionFactory peerConnectionFactory,
            NeighboursRepository neighboursRepository,
            @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue
    ) {
        this.neighboursRepository = neighboursRepository;
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
        boolean testing = true;
        while (running.get()) {
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
                (new Thread(peerConnectionFactory.create(peer, commandsQueue, socket))).start();

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
                        peerConnection = peerConnectionFactory.create(newPeer, commandsQueue);
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
            if (connectedPeers.size() > 0 && testing) {
                testing = false;
                nodeStateQueue.add(NodeState.PARTICIPATING);
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

    public void sendBroadcast(Command command) {
        for (BlockingQueue<Command> queue : connectedPeers.values()) {
            queue.add(command);
        }
    }

    public void sendCommand(Peer peer, Command command) {
        connectedPeers.get(peer).add(command);
    }

    public void closeAll() {
        running.set(false);

        for (Peer peer : connectedPeers.keySet()) {
            peer.setState(PeerState.CLOSING);
        }
    }
}

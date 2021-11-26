package nl.hanze.ec.node.network.peers;

import com.google.inject.Inject;
import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.modules.annotations.IncomingConnectionsQueue;
import nl.hanze.ec.node.modules.annotations.MaxPeers;
import nl.hanze.ec.node.modules.annotations.Port;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.CommandFactory;
import nl.hanze.ec.node.network.peers.commands.requests.NeighborsRequest;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerConnection;
import nl.hanze.ec.node.network.peers.peer.PeerState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class PeerPool implements Runnable {
    private static final Logger logger = LogManager.getLogger(PeerPool.class);
    private final int maxPeers;
    private final BlockingQueue<Socket> incomingConnectionsQueue;

    /**
     * List of all the peers we tried to connect
     */
    List<Peer> triedPeers = new LinkedList<>();

    private DateTime lastSearchedForNewPeers = new DateTime(0);

    private int searchDelta = 10;

    /**
     * Maps each peer to their command queue
     */
    private final Map<Peer, BlockingQueue<Command>> connectedPeers = new HashMap<>();

    /**
     * Neighbours repo
     */
    private final NeighboursRepository neighboursRepository;

    @Inject
    public PeerPool(
            @MaxPeers int maxPeers,
            @Port int port,
            @IncomingConnectionsQueue BlockingQueue<Socket> incomingConnectionsQueue,
            NeighboursRepository neighboursRepository
    ) {
        this.neighboursRepository = neighboursRepository;
        this.maxPeers = maxPeers;
        this.incomingConnectionsQueue = incomingConnectionsQueue;

    }

    private Peer getNewPeer() {
        List<Peer> peers = new ArrayList<>();

        peers.add(new Peer("seed001.ec.dylaan.nl", 5000));
        peers.add(new Peer("seed002.ec.dylaan.nl", 5000));
        peers.add(new Peer("seed003.ec.dylaan.nl", 5000));

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

        triedPeers.add(peers.get(0));
        return peers.get(0);
    }

    @Override
    public void run() {
        while (true) {
            boolean needMorePeers = Math.max(maxPeers - connectedPeers.size(), 0) > 0;

            Socket socket;
            while ((socket = incomingConnectionsQueue.poll()) != null) {
                neighboursRepository.updateNeighbour(socket.getInetAddress().getHostAddress(), socket.getPort());

                if (!needMorePeers) {
                    // TODO: send: not accepting new connections
                }

                Peer peer = new Peer(socket.getInetAddress().getHostAddress(), socket.getPort());
                BlockingQueue<Command> commandsQueue = new LinkedBlockingQueue<>();
                (new Thread(
                        new PeerConnection(peer, commandsQueue, socket)
                )).start();
                connectedPeers.put(peer, commandsQueue);
            }

            DateTime now = new DateTime();
            if (needMorePeers && Seconds.secondsBetween(now, lastSearchedForNewPeers).getSeconds() >= searchDelta) {
                searchDelta = 10;
                lastSearchedForNewPeers = now;

                logger.info("Started searching for another peer...");
                Peer newPeer = getNewPeer();
                if (newPeer == null && connectedPeers.isEmpty()) {
                    logger.fatal("Could not find any other peers and not connected to any other peer.");
                } else if (newPeer == null && !connectedPeers.isEmpty()) {
                    logger.fatal("Could not find any other peers and asking connect peers for their neighbours.");
                    sendBroadcast(new NeighborsRequest());
                } else {
                    logger.info("Found known peer. " + newPeer);
                    BlockingQueue<Command> commandsQueue = new LinkedBlockingQueue<>();
                    PeerConnection peerConnection = PeerConnection.PeerConnectionFactory(newPeer, commandsQueue);
                    neighboursRepository.updateNeighbour(newPeer.getIp(), newPeer.getPort());

                    if (peerConnection == null) {
                        logger.info("Could not connect to known peer");
                        searchDelta = 0;
                    } else {
                        (new Thread(peerConnection)).start();
                        connectedPeers.put(newPeer, commandsQueue);
                    }
                }
            }

            removeDeadPeers();
        }
    }

    private void removeDeadPeers() {
        for (Peer peer : connectedPeers.keySet()) {
            if (peer.getState() == PeerState.CLOSING) {
                logger.info(peer + " is dead. Removing.");
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
}

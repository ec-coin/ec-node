package nl.hanze.ec.node.network.peers;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.app.handlers.StateHandler;
import nl.hanze.ec.node.modules.annotations.IncomingConnectionsQueue;
import nl.hanze.ec.node.modules.annotations.MaxPeers;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.modules.annotations.Port;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerConnection;
import nl.hanze.ec.node.network.peers.peer.PeerState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class PeerPool implements Runnable {
    private static final Logger logger = LogManager.getLogger(PeerPool.class);
    private final int maxPeers;
    private final BlockingQueue<Socket> incomingConnectionsQueue;
    private final BlockingQueue<NodeState> nodeStateQueue;

    /**
     * List containing all unconnected peers
     */
    private final Queue<Peer> unconnectedPeers = new LinkedList<>();

    /**
     * Maps each peer to their command queue
     */
    ConcurrentMap<Peer, BlockingQueue<Command>> connectedPeers = new ConcurrentHashMap<>();

    @Inject
    public PeerPool(
            @MaxPeers int maxPeers,
            @Port int port,
            @IncomingConnectionsQueue BlockingQueue<Socket> incomingConnectionsQueue,
            @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue
    ) {
        this.maxPeers = maxPeers;
        this.unconnectedPeers.addAll(List.of(new Peer[] {new Peer("127.0.0.1", port + 1)}));
        this.incomingConnectionsQueue = incomingConnectionsQueue;
        this.nodeStateQueue = nodeStateQueue;
    }

    @Override
    public void run() {
        boolean testing = true;
        while (true) {
            int peersNeeded = Math.max(maxPeers - connectedPeers.size(), 0);

            Socket socket;
            while ((socket = incomingConnectionsQueue.poll()) != null) {
                if (peersNeeded == 0) {
                    // TODO: send: not accepting new connections
                }

                Peer peer = new Peer(socket.getInetAddress().getHostAddress(), socket.getPort());

                BlockingQueue<Command> commandsQueue = new LinkedBlockingQueue<>();
                (new Thread(
                        new PeerConnection(peer, commandsQueue, socket)
                )).start();
                connectedPeers.put(peer, commandsQueue);
            }

            // TODO: don't know is this is needed al the time,
            //  otherwise other nodes will never be able to connect to you
            //  Maybe only on start up? or if number very high
            if (peersNeeded != 0) {
                connectToPeers(peersNeeded);
            }

            System.out.println("debug: " + (connectedPeers.size() > 0 && testing));
            // Debugging purposes
            if (connectedPeers.size() > 0 && testing) {
                testing = false;
                nodeStateQueue.add(NodeState.PARTICIPATING);
            }

            removeDeadPeers();

            logger.info("Connected peers: " + connectedPeers.size() + ", unconnected peers: "  + unconnectedPeers.size());

            try {
                // TODO: figure out if this is ok
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeDeadPeers() {
        for (Peer peer : connectedPeers.keySet()) {
            if (peer.getState() == PeerState.CLOSING) {
                logger.info(peer + " is dead. Removing.");

                connectedPeers.remove(peer);

                // TODO: totally remove peer if it could not connect x times
                unconnectedPeers.add(peer);
            }
        }
    }

    private void connectToPeers(int peersNeeded) {
        Peer peerCandidate;

        for (int i = 0; i < peersNeeded; i++) {
            peerCandidate = unconnectedPeers.poll();

            if (peerCandidate == null) {
                lookupNewPeers();
                peerCandidate = unconnectedPeers.poll();

                if(peerCandidate == null) {
                    logger.warn("Need more peers but none are available.");
                    break;
                }
            }

            BlockingQueue<Command> commandsQueue = new LinkedBlockingQueue<>();
            PeerConnection peerConnection = PeerConnection.PeerConnectionFactory(peerCandidate, commandsQueue);

            if (peerConnection == null) {
                unconnectedPeers.add(peerCandidate);
                break;
            }

            (new Thread(peerConnection)).start();

            connectedPeers.put(peerCandidate, commandsQueue);
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

    // TODO: asks connected peers to send their peers
    private void lookupNewPeers() {

    }
}

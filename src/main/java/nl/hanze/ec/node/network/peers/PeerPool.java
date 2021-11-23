package nl.hanze.ec.node.network.peers;

import com.google.inject.Inject;
import nl.hanze.ec.node.modules.annotations.IncomingConnectionsQueue;
import nl.hanze.ec.node.modules.annotations.MaxPeers;
import nl.hanze.ec.node.modules.annotations.Port;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.WaitForResponse;
import nl.hanze.ec.node.network.peers.commands.requests.NeighborsRequest;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.peer.PeerConnection;
import nl.hanze.ec.node.network.peers.peer.PeerState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PeerPool implements Runnable {
    private static final Logger logger = LogManager.getLogger(PeerPool.class);
    private final int maxPeers;
    private final BlockingQueue<Socket> incomingConnectionsQueue;

    /**
     * List containing all unconnected peers
     */
    private final Queue<Peer> unconnectedPeers = new LinkedList<>();

    /**
     * Maps each peer to their command queue
     */
    Map<Peer, BlockingQueue<Command>> connectedPeers = new HashMap<>();

    @Inject
    public PeerPool(
            @MaxPeers int maxPeers,
            @Port int port,
            @IncomingConnectionsQueue BlockingQueue<Socket> incomingConnectionsQueue
    ) {
        this.maxPeers = maxPeers;
        this.unconnectedPeers.addAll(List.of(new Peer[] {new Peer("127.0.0.1", port + 1)}));
        this.incomingConnectionsQueue = incomingConnectionsQueue;
    }

    @Override
    public void run() {
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
                // TODO: testing purposes
                if (this.connectedPeers.size() == 1) {
                    Iterator<BlockingQueue<Command>> it = this.connectedPeers.values().iterator();
                    BlockingQueue<Command> cmdQueue = it.next();

                    WaitForResponse cmd = new WaitForResponse(new NeighborsRequest());
                    cmdQueue.add(cmd);
                    cmd.await();
                    System.out.println("AWAIT IS RESOLVED");
                }

                connectToPeers(peersNeeded);
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

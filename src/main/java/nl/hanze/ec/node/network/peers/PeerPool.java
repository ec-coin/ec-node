package nl.hanze.ec.node.network.peers;

import nl.hanze.ec.node.network.commands.Command;
import nl.hanze.ec.node.network.commands.VersionCommand;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PeerPool implements Runnable {
    private static final Logger logger = LogManager.getLogger(PeerPool.class);
    private final int maxPeers;

    /**
     * List containing all unconnected peers
     */
    private final Queue<Peer> unconnectedPeers = new LinkedList<>();

    /**
     * Maps each peer to their command queue
     */
    Map<Peer, BlockingQueue<Command>> connectedPeers = new HashMap<>();

    public PeerPool(int maxPeers, Peer[] seeds) {
        this.maxPeers = maxPeers;
        this.unconnectedPeers.addAll(List.of(seeds));
    }

    @Override
    public void run() {
        while (true) {
            int peersNeeded = Math.max(maxPeers - connectedPeers.size(), 0);

            if (peersNeeded != 0) {
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
            if (peer.getState() == PeerState.DISCONNECTED) {
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
            Thread peerConnectionThread = new Thread(new PeerConnection(peerCandidate, commandsQueue));
            peerConnectionThread.start();

            if (peerCandidate.getState() == PeerState.UNKNOWN_VERSION) {
                connectedPeers.put(peerCandidate, commandsQueue);
                // Add version command
                commandsQueue.add(new VersionCommand());
            } else {
                unconnectedPeers.add(peerCandidate);
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

    // TODO: asks connected peers to send their peers
    private void lookupNewPeers() {

    }
}

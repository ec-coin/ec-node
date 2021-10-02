package nl.hanze.ec.node.network.peers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

public class PeerPool implements Runnable {
    private static final Logger logger = LogManager.getLogger(PeerPool.class);

    /**
     * List containing all unconnected peers
     */
    private final Queue<Peer> unconnectedPeers = new LinkedList<>();

    /**
     * List containing all connected peers
     */
    private final List<PeerConnection> connectedPeers = new ArrayList<>();

    private final int maxPeers;

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
        new ArrayList<>(connectedPeers).forEach(peerConnection -> {
            if (!peerConnection.isConnected()) {
                logger.info(peerConnection.getPeer() + " is dead. Removing.");
                connectedPeers.remove(peerConnection);
                // TODO: totally remove peer if it could not connect x times
                unconnectedPeers.add(peerConnection.getPeer());
            }
        });
    }

    private void connectToPeers(int peersNeeded) {
        Peer peerCandidate = null;

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

            PeerConnection connection = new PeerConnection(peerCandidate);
            if (connection.isConnected()) {
                connectedPeers.add(connection);
            } else {
                unconnectedPeers.add(peerCandidate);
            }
        }
    }

    // TODO: asks connected peers to send their peers
    private void lookupNewPeers() {

    }
}

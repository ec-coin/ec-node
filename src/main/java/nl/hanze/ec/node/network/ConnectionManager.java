package nl.hanze.ec.node.network;

import com.google.inject.Inject;
import nl.hanze.ec.node.network.peers.PeerPool;


public class ConnectionManager {
    private final Server server;
    private final PeerPool peerPool;

    @Inject
    public ConnectionManager(Server server, PeerPool peerPool) {
        this.server = server;
        this.peerPool = peerPool;
    }

    public void setup() {
        Thread serverThread = new Thread(this.server);
        Thread peersThread = new Thread(this.peerPool);

        serverThread.start();
        peersThread.start();
    }

    public PeerPool getPeerPool() {
        return peerPool;
    }
}

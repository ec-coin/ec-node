package nl.hanze.ec.node.network;

import com.google.inject.Inject;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionManager {
    public static final BlockingQueue<Socket> incomingConnections = new LinkedBlockingQueue<>();
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
}

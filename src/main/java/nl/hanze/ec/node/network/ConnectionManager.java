package nl.hanze.ec.node.network;

import com.google.inject.Inject;
import nl.hanze.ec.node.network.peers.PeerPool;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionManager {
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    public static final BlockingQueue<Socket> incomingConnections = new LinkedBlockingQueue<>();
    private final Server server;
    private final PeerPool peerPool;

    @Inject
    public ConnectionManager(Server server, PeerPool peerPool) {
        this.server = server;
        this.peerPool = peerPool;
    }

    @Inject
    public void setup() {
        // temporary
        try {
            Thread serverThread = new Thread(this.server);
            Thread peersThread = new Thread(this.peerPool);

            serverThread.start();
            peersThread.start();

            peersThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

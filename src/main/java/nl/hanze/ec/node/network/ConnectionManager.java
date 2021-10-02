package nl.hanze.ec.node.network;

import com.google.inject.Inject;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.hanze.ec.node.network.peers.Peer;
import nl.hanze.ec.node.network.peers.PeerPool;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class ConnectionManager {
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    // TODO: Not sure if this is the correct way/
    private final Namespace namespace;

    @Inject
    public ConnectionManager(Namespace namespace) {
        this.namespace = namespace;
    }

    public void setup() {
        // temporary
        try {
            // TODO: Should Server and PeerPool also be extracted from the container?
            Thread serverThread = new Thread(new Server(namespace.getInt("port")));
            Thread peersThread = new Thread(new PeerPool(namespace.getInt("max-peers"), new Peer[] {
                    new Peer("127.0.0.1", namespace.getInt("port") + 1)
            }));

            serverThread.start();
            peersThread.start();


            peersThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

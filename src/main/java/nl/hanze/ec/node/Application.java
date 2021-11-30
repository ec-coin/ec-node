package nl.hanze.ec.node;



import com.google.inject.Inject;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.modules.annotations.DatabaseConnection;
import nl.hanze.ec.node.modules.annotations.Delay;
import nl.hanze.ec.node.network.Server;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.utils.FileUtils;

import java.sql.SQLException;

public class Application {
    public static final double VERSION = 1.0;

    private final Server server;
    private final PeerPool peerPool;
    private final int delay;

    @Inject
    public Application(Server server, PeerPool peerPool, @Delay int delay) {
        this.server = server;
        this.peerPool = peerPool;
        this.delay = delay;
    }

    /**
     * Launches the application
     */
    public void run() {
        if (delay != 99999) {
            try {
                Thread.sleep(delay * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //  Prints welcome message to console
        System.out.println(FileUtils.readFromResources("welcome.txt"));

        // Sets up server and client communication
        Thread serverThread = new Thread(this.server);
        Thread peersThread = new Thread(this.peerPool);

        serverThread.start();
        peersThread.start();
    }
}

package nl.hanze.ec.node;



import com.google.inject.Inject;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.modules.annotations.DatabaseConnection;
import nl.hanze.ec.node.network.Server;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.utils.FileUtils;

import java.sql.SQLException;

public class Application {
    public static final double VERSION = 1.0;

    private final Server server;
    private final PeerPool peerPool;

    @Inject
    public Application(Server server, PeerPool peerPool) {
        this.server = server;
        this.peerPool = peerPool;
    }

    /**
     * Launches the application
     */
    public void run() {
        //  Prints welcome message to console
        System.out.println(FileUtils.readFromResources("welcome.txt"));

        // Sets up server and client communication
        Thread serverThread = new Thread(this.server);
        Thread peersThread = new Thread(this.peerPool);

        serverThread.start();
        peersThread.start();
    }
}

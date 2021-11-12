package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.database.adapters.Database;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.modules.annotations.DatabaseConnection;
import nl.hanze.ec.node.network.ConnectionManager;
import nl.hanze.ec.node.utils.FileUtils;

public class Application {
    public static final double VERSION = 1.0;

    private final ConnectionManager connectionManager;
    private final NeighboursRepository neighboursRepository;

    @Inject
    public Application(ConnectionManager connectionManager, NeighboursRepository neighboursRepository) {
        this.connectionManager = connectionManager;
        this.neighboursRepository = neighboursRepository;
    }

    /**
     * Launches the application
     */
    public void run() {
        // Sets up the connection manager
        this.connectionManager.setup();
    }
}

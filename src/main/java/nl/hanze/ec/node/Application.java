package nl.hanze.ec.node;

import com.google.inject.Inject;;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.modules.annotations.DatabaseConnection;
import nl.hanze.ec.node.network.ConnectionManager;
import nl.hanze.ec.node.utils.FileUtils;

import java.sql.SQLException;

public class Application {
    public static final double VERSION = 1.0;

    private final ConnectionManager connectionManager;
    private final ConnectionSource databaseConnection;
    private final NeighboursRepository neighboursRepository; // FOR EXAMPLE USE

    @Inject
    public Application(ConnectionManager connectionManager,
                       @DatabaseConnection ConnectionSource databaseConnection,
                       NeighboursRepository neighboursRepository) {
        this.connectionManager = connectionManager;
        this.databaseConnection = databaseConnection;
        this.neighboursRepository = neighboursRepository;
    }

    /**
     * Launches the application
     */
    public void run() {
        //  Prints welcome message to console
        System.out.println(FileUtils.readFromResources("welcome.txt"));

        // Setup database
        setupDatabase();

        // Run example, to show DB is working
        example();

        // Sets up the connection manager
        this.connectionManager.setup();
    }

    private void setupDatabase() {
        try {
            TableUtils.createTableIfNotExists(databaseConnection, Neighbour.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void example() {
        // Show all neighbours (for example)
        for (Neighbour neighbour : neighboursRepository.getAllNeighbours()) {
            System.out.println("NEIGHBOUR IN DATABASE: " + neighbour.getIp() + ":" + neighbour.getPort() + " " + neighbour.getLastConnectedAt());
        }

        // create or update neighbour
        neighboursRepository.updateNeighbour("192.168.10.10", 5000);
    }
}

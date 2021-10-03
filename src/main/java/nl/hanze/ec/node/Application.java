package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.network.ConnectionManager;
import nl.hanze.ec.node.utils.FileUtils;

public class Application {
    public static final double VERSION = 1.0;

    private final ConnectionManager connectionManager;

    @Inject
    public Application(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Launches the application
     */
    public void run() {
        printWelcome();
        setupConnectionManager();
    }

    /**
     * Sets up the connection manager
     */
    private void setupConnectionManager() {
        this.connectionManager.setup();
    }

    /**
     * Prints welcome message to console
     */
    private void printWelcome() {
        System.out.println(FileUtils.readFromResources("welcome.txt"));
    }
}

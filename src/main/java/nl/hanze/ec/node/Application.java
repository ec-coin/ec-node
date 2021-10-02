package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.network.ConnectionManager;
import nl.hanze.ec.node.network.Message;
import nl.hanze.ec.node.utils.FileUtils;

public class Application {

    private final ConnectionManager connectionManager;

    @Inject
    public Application(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Launch the application
     */
    public void run() {
        printWelcome();
        setupConnectionManager();
    }

    private void setupConnectionManager() {
        this.connectionManager.setup();
    }

    /**
     * Print welcome message to console
     */
    private void printWelcome() {
        System.out.println(FileUtils.readFromResources("welcome.txt"));
    }
}

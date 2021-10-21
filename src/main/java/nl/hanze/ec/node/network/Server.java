package nl.hanze.ec.node.network;

import com.google.inject.Inject;
import nl.hanze.ec.node.modules.annotations.Port;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private final int port;

    @Inject
    public Server(@Port int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("New client connected");

                ConnectionManager.incomingConnections.add(socket);
            }
        } catch (IOException ex) {
            logger.debug("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

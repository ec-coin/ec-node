package nl.hanze.ec.node.network;

import com.google.inject.Inject;
import nl.hanze.ec.node.modules.annotations.IncomingConnectionsQueue;
import nl.hanze.ec.node.modules.annotations.Port;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server implements Runnable {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private final int port;
    private final BlockingQueue<Socket> incomingConnectionsQueue;
    private final AtomicBoolean running = new AtomicBoolean(true);

    @Inject
    public Server(@Port int port, @IncomingConnectionsQueue BlockingQueue<Socket> incomingConnectionsQueue) {
        this.port = port;
        this.incomingConnectionsQueue = incomingConnectionsQueue;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server is listening on port " + port);

            while (running.get()) {
                Socket socket = serverSocket.accept();
                logger.debug("New client connected");
                incomingConnectionsQueue.add(socket);
            }
        } catch (IOException ex) {
            logger.error("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void close() {
        running.set(false);
    }
}

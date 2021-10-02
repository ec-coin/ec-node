package nl.hanze.ec.node.network;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager {
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    public void setup(int port) {
        // temporary
        try {
            Thread serverThread = new Thread(new Server(port));
            serverThread.start();
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

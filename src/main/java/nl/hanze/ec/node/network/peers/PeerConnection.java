package nl.hanze.ec.node.network.peers;

import nl.hanze.ec.node.network.commands.Command;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class PeerConnection {
    private static final Logger logger = LogManager.getLogger(PeerConnection.class);
    private static final int MAX_TIMEOUT = 10 * 1000; // 10 sec

    private Socket socket;
    private Peer peer;

    public PeerConnection(Peer peer) {
        this.peer = peer;

        try {
            this.socket = new Socket(peer.getIp(), peer.getPort());
            this.socket.setSoTimeout(MAX_TIMEOUT);
        } catch (UnknownHostException e) {
            logger.warn("Unknown host: " + peer);
        } catch (SocketTimeoutException e) {
            logger.warn("Socket timeout " + peer);
        } catch (Exception e) {
            logger.warn("Could not connect to: " + peer + ", " + e.getMessage());
        }
    }

    public void sendMessage(Command command) {

    }

    public Command receiveMessage() {
        return null;
    }

    public void closeConnection() {

    }

    public boolean isConnected() {
        if (socket == null) {
            return false;
        }

        try {
            // TODO, this does not work (it pings the host), we should have a heartbeat for each peer and check when
            // we got the last heartbeat
            return socket.getInetAddress().isReachable(MAX_TIMEOUT);
        } catch (IOException e) {
            return false;
        }
    }

    public Peer getPeer() {
        return this.peer;
    }
}

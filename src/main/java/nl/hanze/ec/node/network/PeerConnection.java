package nl.hanze.ec.node.network;

import nl.hanze.ec.node.network.commands.Command;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerConnection {
    public Socket socket;

    public PeerConnection(Peer peer) {
        try {
            this.socket = new Socket(peer.getIp(), peer.getPort());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + peer.getIp());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command) {

    }

    public Command receiveMessage() {
        return null;
    }

    public void closeConnection() {

    }
}

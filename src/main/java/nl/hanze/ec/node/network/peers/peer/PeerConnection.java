package nl.hanze.ec.node.network.peers.peer;

import nl.hanze.ec.node.exceptions.InvalidCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.CommandFactory;
import nl.hanze.ec.node.network.peers.commands.VersionAckCommand;
import nl.hanze.ec.node.network.peers.commands.VersionCommand;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

public class PeerConnection implements Runnable {
    private static final Logger logger = LogManager.getLogger(PeerConnection.class);
    private static final int MAX_TIMEOUT = 10 * 1000; // 10 sec

    private Socket socket;
    private Peer peer;

    private PrintWriter out;
    private BufferedReader in;
    private final BlockingQueue<Command> commandQueue;

    public PeerConnection(Peer peer, BlockingQueue<Command> commandQueue, Socket socket) {
        this.peer = peer;
        this.commandQueue = commandQueue;
        this.socket = socket;

        try {
            this.socket.setSoTimeout(MAX_TIMEOUT);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            peer.setState(PeerState.UNKNOWN_VERSION);

            commandQueue.add(new VersionCommand());
        } catch (SocketTimeoutException e) {
            logger.warn("Socket timeout " + peer);
        } catch (IOException e) {
            logger.warn("I/O error occurred when creating the socket or output/input stream " + peer);
        } catch (Exception e) {
            logger.warn("Could not connect to: " + peer + ", " + e.getMessage());
        }
    }

    @Override
    public void run() {
        Command command;
        String response;
        boolean running = true;

        while (running) {
            if (getPeer().getState() == PeerState.DISCONNECTED || getPeer().getState() == PeerState.UNCONNECTED) {
                if (getPeer().getState() == PeerState.DISCONNECTED) running = false;

                continue;
            }

            // send commands from queue to the peer
            while ((command = commandQueue.poll()) != null) {
                String cmd = command.execute().toString();

                System.out.println("Command:" + cmd);

                out.println(cmd);
                out.flush();
            }

            try {
                // Read peer responses and notify observers
                while (in.ready() && (response = in.readLine()) != null) {
                    System.out.println("Response:" + response);

                    try {
                        JSONObject payload = new JSONObject(response);

                        Command receivedCommand = CommandFactory.create(payload);

                        if (receivedCommand instanceof VersionCommand) {
                            this.getPeer().setVersion(((VersionCommand) receivedCommand).getVersion());
                            commandQueue.add(new VersionAckCommand());
                        }
                    } catch (JSONException | InvalidCommand e) {
                        System.err.println("Invalid json payload received");
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
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

    public static PeerConnection PeerConnectionFactory(Peer peer, BlockingQueue<Command> commandQueue) {
        try {
            return new PeerConnection(peer, commandQueue, new Socket(peer.getIp(), peer.getPort()));
        } catch (UnknownHostException e) {
            logger.warn("Unknown host: " + peer);
        } catch (IOException e) {
            logger.warn("I/O error occurred when creating the socket " + peer);
        }

        return null;
    }
}

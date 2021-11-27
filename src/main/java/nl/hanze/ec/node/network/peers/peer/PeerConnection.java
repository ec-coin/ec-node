package nl.hanze.ec.node.network.peers.peer;

import nl.hanze.ec.node.exceptions.InvalidCommand;
import nl.hanze.ec.node.network.peers.commands.*;
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

    private final Socket socket;
    private final Peer peer;
    private final PeerStateMachine stateMachine;

    private PrintWriter out;
    private BufferedReader in;
    private final BlockingQueue<Command> commandQueue;
    private CommandFactory commandFactory;

    public PeerConnection(
            Peer peer,
            BlockingQueue<Command> commandQueue,
            Socket socket,
            PeerStateMachine stateMachine,
            CommandFactory commandFactory) {
        this.peer = peer;
        this.commandQueue = commandQueue;
        this.socket = socket;
        this.stateMachine = stateMachine;
        this.commandFactory = commandFactory;

        /*
         * Retrieved input and output streams of socket
         */
        try {
            this.socket.setSoTimeout(MAX_TIMEOUT);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Initiate handshake
            stateMachine.start();
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
        // Allocated space for command (output stream) and response (input stream)
        Command command;
        String response;
        boolean running = true;

        /*
         * Continuously poll command queue and read the output buffer
         */
        while (running) {
            // When state closing, close socket and terminate thread
            if (peer.getState() == PeerState.CLOSING) {
                running = false;

                try {
                    socket.close();
                } catch (IOException ignored) {}

                continue;
            }

            // Poll command queue for to be outputted commands
            while ((command = commandQueue.poll()) != null) {
                // State machine validation on to be outputted command
                String cmd = stateMachine.output(command);

                // Invalid command in current state, ignoring
                if (cmd == null) continue;

                logger.info("Command:" + cmd);

                // Write to output stream
                out.println(cmd);
                out.flush();
            }

            try {
                // Read peer responses and notify observers
                while (in.ready() && (response = in.readLine()) != null) {
                    try {
                        JSONObject payload = new JSONObject(response);

                        Command cmd = commandFactory.create(payload);

                        logger.info("Response:" + response);

                        stateMachine.input(cmd);
                    } catch (JSONException | InvalidCommand e) {
                        logger.error("Invalid json payload received: " + e.getMessage());
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}

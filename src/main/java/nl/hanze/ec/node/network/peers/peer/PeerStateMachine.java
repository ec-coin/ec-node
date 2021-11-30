package nl.hanze.ec.node.network.peers.peer;

import nl.hanze.ec.node.network.peers.commands.*;
import nl.hanze.ec.node.network.peers.commands.handshake.Handshake;
import nl.hanze.ec.node.network.peers.commands.handshake.VersionCommand;
import nl.hanze.ec.node.network.peers.commands.handshake.VersionAckCommand;
import nl.hanze.ec.node.network.peers.commands.responses.Response;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class PeerStateMachine {
    private static final Logger logger = LogManager.getLogger(PeerStateMachine.class);
    private final Peer peer;
    private final BlockingQueue<Command> commandQueue;
    private final Map<Integer, WaitForResponse> requestsWaitingForResponse = new HashMap<>();
    private int commandCounter = 0;

    PeerStateMachine(
            Peer peer,
            BlockingQueue<Command> commandQueue
    ) {
        this.peer = peer;
        this.commandQueue = commandQueue;
    }

    public void start() {
        // Add version command to queue, so it will be sent when the thread is dispatched
        commandQueue.add(new VersionCommand(0));
    }

    public String output(Command command) {
        // When state not established only allow handshake commands
        if (peer.getState() != PeerState.ESTABLISHED && !(command instanceof Handshake)) {
            return null;
        }

        command.setMessageNumber(commandCounter);
        this.commandCounter++;

        if (command instanceof WaitForResponse) {
            requestsWaitingForResponse.put(command.getMessageNumber(), (WaitForResponse) command);
        }

        // Retrieve JSON representation of command
        return command.getPayload().toString();
    }

    public void input(Command command) {
        // When handshake command update state accordingly
        if (command instanceof Handshake) {
            if (command instanceof VersionCommand) {
                double version = ((VersionCommand) command).getVersion();
                peer.setVersion(version);

                // If VERSION_ACK already received transition to ESTABLISHED
                // else wait for VERSION_ACK
                peer.setState((peer.getState() == PeerState.VERSION_ACK)
                        ? PeerState.ESTABLISHED : PeerState.VERSION_RCVD);

                commandQueue.add(new VersionAckCommand());
            }

            if (command instanceof VersionAckCommand) {
                // If VERSION_RCVD already received transition to ESTABLISHED
                // else wait for VERSION_RCVD
                peer.setState((peer.getState() == PeerState.VERSION_RCVD)
                        ? PeerState.ESTABLISHED : PeerState.VERSION_ACK);

                logger.info("Connection with peer " + peer.getIp() + "@" + peer.getPort() + " is now established");
            }
        // Only allow non handshake commands when state is ESTABLISHED
        } else if (peer.getState() == PeerState.ESTABLISHED) {
            command.getWorker(command, commandQueue).run();

            if (command instanceof Response) {
                Integer responseTo = ((Response) command).inResponseTo();
                WaitForResponse request = requestsWaitingForResponse.get(responseTo);
                if (request != null) {
                    request.resolve();
                    requestsWaitingForResponse.remove(responseTo);
                }
            }
        }
    }
}

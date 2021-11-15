package nl.hanze.ec.node.network.peers.peer;

import nl.hanze.ec.node.network.peers.commands.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

public class PeerStateMachine {
    private static final Logger logger = LogManager.getLogger(PeerStateMachine.class);
    private final Peer peer;
    private final BlockingQueue<Command> commandQueue;

    PeerStateMachine(
            Peer peer,
            BlockingQueue<Command> commandQueue
    ) {
        this.peer = peer;
        this.commandQueue = commandQueue;
    }

    public void start() {
        // Add version command to queue, so it will be sent when the thread is dispatched
        commandQueue.add(new VersionCommand());
    }

    public String output(Command command) {
        // When state not established only allow handshake commands
        if (peer.getState() != PeerState.ESTABLISHED && !(command instanceof Handshake)) {
            return null;
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

                if (peer.getState() == PeerState.ESTABLISHED) {
                    commandQueue.add(new TestCommand());
                }
            }

            if (command instanceof VersionAckCommand) {
                // If VERSION_RCVD already received transition to ESTABLISHED
                // else wait for VERSION_RCVD
                peer.setState((peer.getState() == PeerState.VERSION_RCVD)
                        ? PeerState.ESTABLISHED : PeerState.VERSION_ACK);

                logger.info("Connection with peer " + peer.getIp() + "@" + peer.getPort() + " is now established");

                // TODO: This is temporary
                if (peer.getState() == PeerState.ESTABLISHED) {
                    commandQueue.add(new TestCommand());
                }
            }
        // Only allow non handshake commands when state is ESTABLISHED
        } else if (peer.getState() == PeerState.ESTABLISHED) {
            new Thread(command.getWorker(command, commandQueue)).start();
        }
    }
}

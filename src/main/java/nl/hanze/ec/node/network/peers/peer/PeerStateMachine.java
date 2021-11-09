package nl.hanze.ec.node.network.peers.peer;

import nl.hanze.ec.node.network.peers.commands.*;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;

public class PeerStateMachine {
    private final Peer peer;
    private final BlockingQueue<Command> commandQueue;
    private final Collection<BlockingQueue<Command>> commandResponder;

    PeerStateMachine(
            Peer peer,
            BlockingQueue<Command> commandQueue,
            Collection<BlockingQueue<Command>> commandResponder
    ) {
        this.peer = peer;
        this.commandQueue = commandQueue;
        this.commandResponder = commandResponder;
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
        return command.execute().toString();
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

                if (peer.getState() == PeerState.ESTABLISHED) {
                    commandQueue.add(new TestCommand());
                }
            }
        // Only allow non handshake commands when state is ESTABLISHED
        } else if (peer.getState() == PeerState.ESTABLISHED) {
            for (BlockingQueue<Command> queue : commandResponder) {
                queue.add(command);
            }
        }
    }
}

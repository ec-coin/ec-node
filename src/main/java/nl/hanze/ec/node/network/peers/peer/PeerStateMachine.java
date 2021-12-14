package nl.hanze.ec.node.network.peers.peer;

import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.network.peers.commands.*;
import nl.hanze.ec.node.network.peers.commands.announcements.Announcement;
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
    private final PeerPool peerPool;
    private final BlockRepository blockRepository;

    private final Map<Integer, WaitForResponse> requestsWaitingForResponse = new HashMap<>();
    private int commandCounter = 0;

    /**
     * Constructs a PeerStateMachine
     *
     * @param peer the peer for which this state machine is created
     * @param commandQueue the command queue for the given peer
     * @param peerPool the peer pool used when an announcement is received to further propagate the announcement
     */
    PeerStateMachine(
            Peer peer,
            BlockingQueue<Command> commandQueue,
            PeerPool peerPool,
            BlockRepository blockRepository
    ) {
        this.peer = peer;
        this.commandQueue = commandQueue;
        this.peerPool = peerPool;
        this.blockRepository = blockRepository;
    }

    /**
     * Starts the communication with a peer by sending a Version command
     */
    public void start() {
        // Add version command to queue, so it will be sent when the thread is dispatched
        commandQueue.add(new VersionCommand(blockRepository.getCurrentBlockHeight()));
    }

    /**
     * Handle commands that should be sent to the peer.
     *
     * @param command command to be sent
     * @return a stringified version of the command or null when command can not be sent in current state.
     */
    public String output(Command command) {
        // When the state is not ESTABLISHED only allow handshake commands
        if (peer.getState() != PeerState.ESTABLISHED && !(command instanceof Handshake)) {
            return null;
        }

        // Define the message number in the command (used when responding to a request)
        command.setMessageNumber(commandCounter);
        this.commandCounter++; // increase the message number for the next command

        // Save this command when it is wrapped inside a WaitForResponse decorator
        // s.t. when a response arrives it can wake up the thread that is waiting for the response.
        if (command instanceof WaitForResponse) {
            requestsWaitingForResponse.put(command.getMessageNumber(), (WaitForResponse) command);
        }

        // Retrieve JSON representation of command
        return command.getPayload().toString();
    }

    /**
     * Handle command that were received from the peer.
     *
     * @param command the command received
     */
    public void input(Command command) {
        // When the command is a handshake command, update state accordingly
        if (command instanceof Handshake) {
            if (command instanceof VersionCommand) {
                // Save the version number and start height from the peer.
                VersionCommand cmd = (VersionCommand) command;
                peer.setVersion(cmd.getVersion());
                peer.setStartHeight(cmd.getStartHeight());

                // If VERSION_ACK already received transition to ESTABLISHED
                // else wait for VERSION_ACK
                peer.setState((peer.getState() == PeerState.VERSION_ACK)
                        ? PeerState.ESTABLISHED : PeerState.VERSION_RCVD);

                // Send version acknowledgement
                commandQueue.add(new VersionAckCommand());
            }

            if (command instanceof VersionAckCommand) {
                // If VERSION_RCVD already received transition to ESTABLISHED
                // else wait for VERSION_RCVD
                peer.setState((peer.getState() == PeerState.VERSION_RCVD)
                        ? PeerState.ESTABLISHED : PeerState.VERSION_ACK);
            }

            // When connection is ESTABLISHED with peer inform event via logger.
            if (peer.getState() == PeerState.ESTABLISHED) {
                logger.info("Connection with peer " + peer.getIp() + " w/ startHeight: " + peer.getStartHeight() + " is now established");
            }
        } else {
            // When command is not a handshake command, only allow them when the state with this peer is ESTABLISHED.
            if (peer.getState() == PeerState.ESTABLISHED) {
                // Execute the associated worker for this command.
                Worker worker = command.getWorker(command, commandQueue);
                if (worker != null) {
                    worker.run();
                }

                // When the command is a response, try to resolve the associated request
                // and wake up the thread that is waiting for this response.
                if (command instanceof Response) {
                    Response response = (Response) command;
                    Integer responseTo = response.inResponseTo();
                    WaitForResponse request = requestsWaitingForResponse.get(responseTo);

                    if (request != null) {
                        request.setResponse(response);
                        request.resolve();
                        requestsWaitingForResponse.remove(responseTo);
                    }
                }

                // Inform peers (neighbors) about the received announcement
                if (command instanceof Announcement) {
                    peerPool.sendBroadcast(command);
                }
            }
        }
    }
}

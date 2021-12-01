package nl.hanze.ec.node.network.peers.peer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.CommandFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class PeerConnectionFactory {
    private final Provider<CommandFactory> commandFactoryProvider;

    @Inject
    public PeerConnectionFactory(Provider<CommandFactory> commandFactoryProvider) {
        this.commandFactoryProvider = commandFactoryProvider;
    }

    /**
     * Creates a peer connection when a socket connection already exists (incoming connection)
     *
     * @param peer the peer from the given socket
     * @param commandQueue the command queue that is used to listen for commands to need to be sent
     * @param socket the socket
     * @param peerPool the peer pool used when an announcement is received to further propagate the announcement
     * @return a peer connection
     */
    public PeerConnection create(Peer peer, BlockingQueue<Command> commandQueue, Socket socket, PeerPool peerPool) {
        return new PeerConnection(
                peer,
                commandQueue,
                socket,
                new PeerStateMachine(peer, commandQueue, peerPool),
                commandFactoryProvider.get()
        );
    }

    /**
     * Creates a peer connection when a socket connection still has to be made (outgoing connection)
     *
     * @param peer the peer to connect to
     * @param commandQueue the command queue that is used to listen for commands to need to be sent
     * @param peerPool the peer pool used when an announcement is received to further propagate the announcement
     * @return a peer connection
     */
    public PeerConnection create(Peer peer, BlockingQueue<Command> commandQueue, PeerPool peerPool) throws IOException {
        return new PeerConnection(
                peer,
                commandQueue,
                new Socket(peer.getIp(), peer.getPort()),
                new PeerStateMachine(peer, commandQueue, peerPool),
                commandFactoryProvider.get()
        );
    }
}

package nl.hanze.ec.node.network.peers.peer;

import com.google.inject.Inject;
import com.google.inject.Provider;
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

    public PeerConnection create(Peer peer, BlockingQueue<Command> commandQueue, Socket socket) {
        return new PeerConnection(
                peer,
                commandQueue,
                socket,
                new PeerStateMachine(peer, commandQueue),
                commandFactoryProvider.get()
        );
    }

    public PeerConnection create(Peer peer, BlockingQueue<Command> commandQueue) throws IOException {
        return new PeerConnection(
                peer,
                commandQueue,
                new Socket(peer.getIp(), peer.getPort()),
                new PeerStateMachine(peer, commandQueue),
                commandFactoryProvider.get()
        );
    }
}

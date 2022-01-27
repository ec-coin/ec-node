package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.modules.annotations.NodeAddress;
import nl.hanze.ec.node.modules.annotations.NodeKeyPair;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.security.KeyPair;
import java.util.concurrent.BlockingQueue;

public class ListenerFactory {
    private BlockingQueue<NodeState> nodeStateQueue;
    private final Provider<TransactionRepository> transactionRepositoryProvider;
    private final Provider<BlockRepository> blockRepositoryProvider;
    private final KeyPair keyPair;
    private final String nodeAddress;

    @Inject
    public ListenerFactory(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
                           @NodeAddress String address,
                           @NodeKeyPair KeyPair keyPair,
                           Provider<TransactionRepository> transactionRepositoryProvider,
                           Provider<BlockRepository> blockRepositoryProvider) {
        this.nodeStateQueue = nodeStateQueue;
        this.transactionRepositoryProvider = transactionRepositoryProvider;
        this.blockRepositoryProvider = blockRepositoryProvider;
        this.keyPair = keyPair;
        this.nodeAddress = address;
    }

    public Listener create(Class<? extends Listener> listener, PeerPool peerPool) {
        if (listener == Consensus.class) {
            return new Consensus(
                nodeStateQueue,
                nodeAddress,
                peerPool,
                transactionRepositoryProvider.get()
            );
        } else if (listener == BlockSyncer.class) {
            return new BlockSyncer(
                    nodeStateQueue,
                    peerPool,
                    blockRepositoryProvider.get(),
                    transactionRepositoryProvider.get()
            );
        } else if (listener == BlockCreator.class) {
            return new BlockCreator(
                    nodeStateQueue,
                    nodeAddress,
                    keyPair,
                    peerPool,
                    transactionRepositoryProvider.get(),
                    blockRepositoryProvider.get()
            );
        }

        throw new UnsupportedOperationException("Factory has not defined how the given class has to be created");
    }
}

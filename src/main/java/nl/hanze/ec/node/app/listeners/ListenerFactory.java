package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.concurrent.BlockingQueue;

public class ListenerFactory {
    BlockingQueue<NodeState> nodeStateQueue;
    private final Provider<TransactionRepository> transactionRepositoryProvider;
    private final Provider<NeighboursRepository> neighboursRepositoryProvider;
    private final Provider<BlockRepository> blockRepositoryProvider;

    @Inject
    public ListenerFactory(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
                           Provider<TransactionRepository> transactionRepositoryProvider,
                           Provider<NeighboursRepository> neighboursRepositoryProvider,
                           Provider<BlockRepository> blockRepositoryProvider) {
        this.nodeStateQueue = nodeStateQueue;
        this.transactionRepositoryProvider = transactionRepositoryProvider;
        this.neighboursRepositoryProvider = neighboursRepositoryProvider;
        this.blockRepositoryProvider = blockRepositoryProvider;
    }

    public Listener create(Class<? extends Listener> listener, PeerPool peerPool) {
        if (listener == Consensus.class) {
            return new Consensus(
                    nodeStateQueue,
                    peerPool,
                    transactionRepositoryProvider.get(),
                    neighboursRepositoryProvider.get(),
                    blockRepositoryProvider.get()
            );
        } else if (listener == BlockSyncer.class) {
            return new BlockSyncer(
                    nodeStateQueue,
                    peerPool,
                    blockRepositoryProvider.get(),
                    transactionRepositoryProvider.get()
            );
        }

        throw new UnsupportedOperationException("Factory has not defined how the given class has to be created");
    }
}

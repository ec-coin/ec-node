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
    private final Provider<NeighboursRepository> neighboursRepositoryProvider;
    private final Provider<BlockRepository> blockRepositoryProvider;
    private final KeyPair nodeKeyPair;
    private final String nodeAddress;

    @Inject
    public ListenerFactory(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
                           @NodeKeyPair KeyPair keyPair,
                           @NodeAddress String address,
                           Provider<TransactionRepository> transactionRepositoryProvider,
                           Provider<NeighboursRepository> neighboursRepositoryProvider,
                           Provider<BlockRepository> blockRepositoryProvider) {
        this.nodeStateQueue = nodeStateQueue;
        this.transactionRepositoryProvider = transactionRepositoryProvider;
        this.neighboursRepositoryProvider = neighboursRepositoryProvider;
        this.blockRepositoryProvider = blockRepositoryProvider;
        this.nodeAddress = address;
        this.nodeKeyPair = keyPair;
    }

    public Listener create(Class<? extends Listener> listener, PeerPool peerPool) {
        if (listener == Consensus.class) {
            return new Consensus(
                nodeStateQueue,
                nodeKeyPair,
                nodeAddress,
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

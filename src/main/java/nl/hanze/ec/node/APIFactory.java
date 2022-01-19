package nl.hanze.ec.node;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.network.peers.PeerPool;

public class APIFactory {
    private final Provider<NeighboursRepository> neighboursRepositoryProvider;
    private final Provider<BlockRepository> blockRepositoryProvider;
    private final Provider<BalancesCacheRepository> balancesCacheRepositoryProvider;
    private final Provider<TransactionRepository> transactionRepositoryProvider;

    @Inject
    public APIFactory(
            Provider<NeighboursRepository> neighboursRepositoryProvider,
            Provider<BalancesCacheRepository> balancesCacheRepositoryProvider,
            Provider<BlockRepository> blockRepositoryProvider,
            Provider<TransactionRepository> transactionRepositoryProvider
    ) {
        this.neighboursRepositoryProvider = neighboursRepositoryProvider;
        this.blockRepositoryProvider = blockRepositoryProvider;
        this.balancesCacheRepositoryProvider = balancesCacheRepositoryProvider;
        this.transactionRepositoryProvider = transactionRepositoryProvider;
    }

    public API create(PeerPool peerPool) {
        return new API(
                neighboursRepositoryProvider.get(),
                balancesCacheRepositoryProvider.get(),
                blockRepositoryProvider.get(),
                transactionRepositoryProvider.get(),
                peerPool
        );
    }
}

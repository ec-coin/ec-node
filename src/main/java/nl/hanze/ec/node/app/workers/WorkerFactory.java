package nl.hanze.ec.node.app.workers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.concurrent.BlockingQueue;

public class WorkerFactory {
    private final Provider<NeighboursRepository> neighboursRepositoryProvider;
    private final Provider<BlockRepository> blockRepositoryProvider;
    private final Provider<BalancesCacheRepository> balancesCacheRepositoryProvider;
    private final Provider<TransactionRepository> transactionRepositoryProvider;

    @Inject
    public WorkerFactory(
        Provider<NeighboursRepository> neighboursRepositoryProvider,
        Provider<BlockRepository> blockRepositoryProvider,
        Provider<BalancesCacheRepository> balancesCacheRepositoryProvider,
        Provider<TransactionRepository> transactionRepositoryProvider
    ) {
        this.neighboursRepositoryProvider = neighboursRepositoryProvider;
        this.blockRepositoryProvider = blockRepositoryProvider;
        this.balancesCacheRepositoryProvider = balancesCacheRepositoryProvider;
        this.transactionRepositoryProvider = transactionRepositoryProvider;
    }

    public Worker create(
        Class<? extends Worker> workerClass,
        Command receivedCommand,
        BlockingQueue<Command> peerCommandQueue
    ) {
        if (workerClass == NeighborResponseWorker.class) {
            return new NeighborResponseWorker(receivedCommand, peerCommandQueue, neighboursRepositoryProvider.get());
        } else if (workerClass == NeighborRequestWorker.class) {
            return new NeighborRequestWorker(receivedCommand, peerCommandQueue, neighboursRepositoryProvider.get());
        } else if (workerClass == HeadersRequestWorker.class) {
            return new HeadersRequestWorker(receivedCommand, peerCommandQueue, blockRepositoryProvider.get());
        } else if (workerClass == TransactionsRequestWorker.class) {
            return new TransactionsRequestWorker(receivedCommand, peerCommandQueue, blockRepositoryProvider.get());
        } else if (workerClass == NewBlockAnnouncementWorker.class) {
            return new NewBlockAnnouncementWorker(receivedCommand, peerCommandQueue, blockRepositoryProvider.get(), transactionRepositoryProvider.get(), balancesCacheRepositoryProvider.get());
        } else if (workerClass == PendingTransactionWorker.class) {
            return new PendingTransactionWorker(receivedCommand, peerCommandQueue, balancesCacheRepositoryProvider.get(), transactionRepositoryProvider.get());
        }

        throw new UnsupportedOperationException("Factory has not defined how the given worker class has to be created");
    }
}

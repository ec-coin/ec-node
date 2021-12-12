package nl.hanze.ec.node.app.workers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.requests.InventoryRequest;

import java.util.concurrent.BlockingQueue;

public class WorkerFactory {
    private final Provider<NeighboursRepository> neighboursRepositoryProvider;
    private final Provider<BlockRepository> blockRepositoryProvider;

    @Inject
    public WorkerFactory(
            Provider<NeighboursRepository> neighboursRepositoryProvider,
            Provider<BlockRepository> blockRepositoryProvider
    ) {
        this.neighboursRepositoryProvider = neighboursRepositoryProvider;
        this.blockRepositoryProvider = blockRepositoryProvider;
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
        } else if (workerClass == InventoryRequestWorker.class) {
            return new InventoryRequestWorker(receivedCommand, peerCommandQueue, blockRepositoryProvider.get());
        }

        throw new UnsupportedOperationException("Factory has not defined how the given worker class has to be created");
    }
}

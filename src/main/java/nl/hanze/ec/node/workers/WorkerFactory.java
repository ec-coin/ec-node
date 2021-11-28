package nl.hanze.ec.node.workers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.concurrent.BlockingQueue;

public class WorkerFactory {
    private final Provider<NeighboursRepository> neighboursRepositoryProvider;

    @Inject
    public WorkerFactory(Provider<NeighboursRepository> neighboursRepositoryProvider) {
        this.neighboursRepositoryProvider = neighboursRepositoryProvider;
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
        }

        throw new UnsupportedOperationException("Factory has not defined how the given worker class has to be created");
    }
}

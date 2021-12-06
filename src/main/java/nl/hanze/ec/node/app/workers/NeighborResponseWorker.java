package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.requests.NeighborsRequest;
import nl.hanze.ec.node.network.peers.commands.responses.NeighborsResponse;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class NeighborResponseWorker extends Worker {
    private final NeighboursRepository neighboursRepository;

    public NeighborResponseWorker(
            Command receivedCommand,
            BlockingQueue<Command> peerCommandQueue,
            NeighboursRepository neighboursRepositoryProvider
    ) {
        super(receivedCommand, peerCommandQueue);
        this.neighboursRepository = neighboursRepositoryProvider;
    }

    @Override
    public void run() {
        if (receivedCommand instanceof NeighborsResponse) {
            for (Object ip : ((NeighborsResponse) receivedCommand).getIps()) {
                try {
                    neighboursRepository.updateNeighbour((String) ip, 5000);
                } catch (ClassCastException ignore) {}
            }
        }
    }
}

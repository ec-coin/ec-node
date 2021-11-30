package nl.hanze.ec.node.workers;

import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
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
        JSONObject response = receivedCommand.getPayload();
        neighboursRepository.updateNeighbour(response.getString("ip"), response.getInt("port"));
    }
}

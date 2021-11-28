package nl.hanze.ec.node.workers;

import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.responses.NeighborsResponse;

import java.util.concurrent.BlockingQueue;

public class NeighborRequestWorker extends Worker {
    private final NeighboursRepository neighboursRepository;
    public NeighborRequestWorker(Command receivedCommand,
                                 BlockingQueue<Command> peerCommandQueue,
                                 NeighboursRepository neighboursRepository) {
        super(receivedCommand, peerCommandQueue);
        this.neighboursRepository = neighboursRepository;
    }

    @Override
    public void run() {
        for(Neighbour n : neighboursRepository.getAllNeighbours()) {
            Command rsp = new NeighborsResponse(n.getIp(), 5000, receivedCommand.getMessageNumber());
            peerCommandQueue.add(rsp);
        }
    }
}

package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.responses.NeighborsResponse;

import java.util.ArrayList;
import java.util.List;
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
        List<String> ips = new ArrayList<>();

        for(Neighbour n : neighboursRepository.getAllNeighbours()) {
            ips.add(n.getIp());
        }

        Command rsp = new NeighborsResponse(ips, receivedCommand.getMessageNumber());
        peerCommandQueue.add(rsp);
    }
}

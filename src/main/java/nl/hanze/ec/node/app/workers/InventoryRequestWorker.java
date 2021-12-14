package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.requests.InventoryRequest;
import nl.hanze.ec.node.network.peers.commands.responses.InventoryResponse;
import nl.hanze.ec.node.network.peers.commands.responses.NeighborsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class InventoryRequestWorker extends Worker {
    private final BlockRepository blockRepository;

    public InventoryRequestWorker(Command receivedCommand,
                                  BlockingQueue<Command> peerCommandQueue,
                                  BlockRepository blockRepository) {
        super(receivedCommand, peerCommandQueue);
        this.blockRepository = blockRepository;
    }

    @Override
    public void run() {
        InventoryRequest command = (InventoryRequest) receivedCommand;

        List<String> hashes = command.getBlockHashes();

        hashes.get(0);
        
        //peerCommandQueue.add(new InventoryResponse(toBeSentHashes, receivedCommand.getMessageNumber()));
    }
}

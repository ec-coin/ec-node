package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.requests.HeadersRequest;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class HeadersRequestWorker extends Worker {
    private final BlockRepository blockRepository;

    public HeadersRequestWorker(Command receivedCommand,
                                BlockingQueue<Command> peerCommandQueue,
                                BlockRepository blockRepository) {
        super(receivedCommand, peerCommandQueue);
        this.blockRepository = blockRepository;
    }

    @Override
    public void run() {
        HeadersRequest command = (HeadersRequest) receivedCommand;

        List<Object> hashes = command.getBlockHashes();

        hashes.get(0);
        
        //peerCommandQueue.add(new InventoryResponse(toBeSentHashes, receivedCommand.getMessageNumber()));
    }
}

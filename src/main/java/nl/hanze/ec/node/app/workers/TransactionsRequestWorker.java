package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.requests.TransactionsRequest;
import nl.hanze.ec.node.network.peers.commands.responses.TransactionsResponse;

import java.util.concurrent.BlockingQueue;

public class TransactionsRequestWorker extends Worker {
    private final BlockRepository blockRepository;

    public TransactionsRequestWorker(Command receivedCommand,
                                     BlockingQueue<Command> peerCommandQueue,
                                     BlockRepository blockRepository) {
        super(receivedCommand, peerCommandQueue);
        this.blockRepository = blockRepository;
    }

    @Override
    public void run() {
        TransactionsRequest command = (TransactionsRequest) receivedCommand;

        String hash = command.getBlockHash();

        Integer blockHeight = blockRepository.getBlockHeight(hash);

        // Requested block not present in database
        if (blockHeight == null) {
            // TODO
            blockHeight = 0;
        }

        Block block = blockRepository.getBlock(blockHeight);

        if (block != null) {
            peerCommandQueue.add(new TransactionsResponse(block.getTransactions(), receivedCommand.getMessageNumber()));
        }
    }
}

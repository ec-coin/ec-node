package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.requests.HeadersRequest;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;

import java.util.ArrayList;
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

        String hash = command.getBlockHash();

        Integer blockHeight = blockRepository.getBlockHeight(hash);

        List<Block> blocks = new ArrayList<>();

        for (int i = 1; i <= 50; i++) {
            Block block = blockRepository.getBlock(blockHeight + i);

            if (block == null) {
                break;
            }

            blocks.add(block);
        }

        if (blocks.size() > 0) {
            peerCommandQueue.add(new HeadersResponse(blocks, receivedCommand.getMessageNumber()));
        }
    }
}

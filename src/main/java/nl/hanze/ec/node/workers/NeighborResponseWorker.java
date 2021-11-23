package nl.hanze.ec.node.workers;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.responses.NeighborsResponse;

import java.util.concurrent.BlockingQueue;

public class NeighborResponseWorker extends Worker {
    public NeighborResponseWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {
        // TODO: add to database
    }
}

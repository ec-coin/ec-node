package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;

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

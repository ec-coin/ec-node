package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.concurrent.BlockingQueue;

public class InventoryResponseWorker extends Worker {
    public InventoryResponseWorker(Command receivedCommand,
                                   BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {

    }
}

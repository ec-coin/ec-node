package nl.hanze.ec.node.workers;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.responses.NeighborsResponse;

import java.util.concurrent.BlockingQueue;

public class NeighborRequestWorker extends Worker {
    public NeighborRequestWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {
        Command rsp = new NeighborsResponse("127.0.0.1", 5002, receivedCommand.getMessageNumber());

        peerCommandQueue.add(rsp);
    }
}

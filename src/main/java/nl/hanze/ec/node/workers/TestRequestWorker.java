package nl.hanze.ec.node.workers;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.responses.TestResponse;

import java.util.concurrent.BlockingQueue;

public class TestRequestWorker extends Worker {
    public TestRequestWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {
        peerCommandQueue.add(new TestResponse());
    }
}

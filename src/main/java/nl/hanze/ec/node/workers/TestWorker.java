package nl.hanze.ec.node.workers;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.TestAnnouncement;

import java.util.concurrent.BlockingQueue;

public class TestWorker extends Worker {
    public TestWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {
        System.out.println("TestWorker RECEIVED: " + receivedCommand.getPayload());

        // If this was a request a response could be sent like this.
        // peerCommandQueue.add(new TestResponse());
    }
}

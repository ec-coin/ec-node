package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.concurrent.BlockingQueue;

public class TestAnnouncementWorker extends Worker {
    public TestAnnouncementWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {
        // System.out.println("TestWorker RECEIVED: " + receivedCommand.getPayload());

        // If this was a request a response could be sent like this.
        // peerCommandQueue.add(new TestResponse());
    }
}

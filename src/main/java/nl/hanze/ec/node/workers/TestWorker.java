package nl.hanze.ec.node.workers;

import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.concurrent.BlockingQueue;

public class TestWorker extends Worker {
    public TestWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {
        System.out.println("TestWorker RECEIVED: " + receivedCommand.getPayload());
    }
}

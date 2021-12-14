package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.concurrent.BlockingQueue;

public class HeadersResponseWorker extends Worker {
    public HeadersResponseWorker(Command receivedCommand,
                                 BlockingQueue<Command> peerCommandQueue) {
        super(receivedCommand, peerCommandQueue);
    }

    @Override
    public void run() {

    }
}

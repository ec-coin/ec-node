package nl.hanze.ec.node.workers;

import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.concurrent.BlockingQueue;

public abstract class Worker implements Runnable{
    Command receivedCommand;
    BlockingQueue<Command> peerCommandQueue;

    public Worker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        this.receivedCommand = receivedCommand;
        this.peerCommandQueue = peerCommandQueue;
    }
}

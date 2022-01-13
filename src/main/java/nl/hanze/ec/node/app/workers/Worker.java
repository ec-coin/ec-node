package nl.hanze.ec.node.app.workers;

import com.google.gson.Gson;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;

public abstract class Worker implements Runnable {
    Command receivedCommand;
    BlockingQueue<Command> peerCommandQueue;

    public Worker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        this.receivedCommand = receivedCommand;
        this.peerCommandQueue = peerCommandQueue;
    }
}

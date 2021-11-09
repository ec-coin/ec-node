package nl.hanze.ec.node;

import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.Set;
import java.util.concurrent.BlockingQueue;

public abstract class CommandResponder implements Runnable {
    private BlockingQueue<Command> queue = null;

    @Override
    public void run() {
        if (queue == null) {
            throw new RuntimeException("Command Queue not set");
        }

        while (true) {
            Command command;

            // TODO: use take() instead of poll()
            while ((command = queue.poll()) != null) {
                if (filter().contains(command.getClass())) {
                    handle(command);
                }
            }
        }
    }

    public void setQueue(BlockingQueue<Command> queue) {
        this.queue = queue;
    }

    protected abstract void handle(Command command);

    protected abstract Set<Class<? extends Command>> filter();
}

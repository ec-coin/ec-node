package nl.hanze.ec.node;

import nl.hanze.ec.node.network.peers.commands.Command;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public abstract class CommandConsumer implements Runnable {
    public final Map<CommandConsumer, BlockingQueue<Command>> commandConsumerQueues;

    public CommandConsumer(Map<CommandConsumer, BlockingQueue<Command>> commandConsumerQueues) {
        this.commandConsumerQueues = commandConsumerQueues;
    }

    @Override
    public void run() {
        while (true) {
            Command command;

            while ((command = getQueue().poll()) != null) {
                if (filter().contains(command.getClass())) {
                    handle(command);
                }
            }
        }
    }

    public BlockingQueue<Command> getQueue() {
        return commandConsumerQueues.get(this);
    }

    protected abstract void handle(Command command);

    protected abstract Set<Class<? extends Command>> filter();
}

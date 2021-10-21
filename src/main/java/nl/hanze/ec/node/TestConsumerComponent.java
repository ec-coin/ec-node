package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.modules.annotations.CommandConsumerQueues;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.TestCommand;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class TestConsumerComponent extends CommandConsumer {
    @Override
    protected void handle(Command command) {
        System.out.println("TestObserveComponent RECEIVED: " + command.execute());
    }

    @Override
    protected Set<Class<? extends Command>> filter() {
        return new HashSet<>() {
            {
                add(TestCommand.class);
            }
        };
    }
}

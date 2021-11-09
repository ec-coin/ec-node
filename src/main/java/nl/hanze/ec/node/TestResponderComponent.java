package nl.hanze.ec.node;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.TestCommand;

import java.util.HashSet;
import java.util.Set;

public class TestResponderComponent extends CommandResponder {
    protected Set<Class<? extends Command>> commands = new HashSet<>() {
        {
            add(TestCommand.class);
        }
    };

    @Override
    protected void handle(Command command) {
        System.out.println("TestResponderComponent RECEIVED: " + command.execute());
    }

    @Override
    protected Set<Class<? extends Command>> filter() {
        return commands;
    }
}

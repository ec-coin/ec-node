package nl.hanze.ec.node.network.commands;

import nl.hanze.ec.node.network.Message;

public interface Command {
    Message execute();
}

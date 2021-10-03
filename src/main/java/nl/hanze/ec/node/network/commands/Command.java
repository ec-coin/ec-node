package nl.hanze.ec.node.network.commands;

import org.json.JSONObject;

public interface Command {
    JSONObject execute();

    String getCommandName();
}

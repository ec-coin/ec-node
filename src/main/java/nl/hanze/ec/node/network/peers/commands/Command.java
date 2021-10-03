package nl.hanze.ec.node.network.peers.commands;

import org.json.JSONObject;

public interface Command {
    JSONObject execute();

    String getCommandName();
}

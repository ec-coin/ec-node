package nl.hanze.ec.node.network.peers.commands;

import org.json.JSONObject;

public class VersionAckCommand implements Command {
    @Override
    public JSONObject execute() {
        JSONObject payload = new JSONObject();

        payload.put("command", getCommandName());

        return payload;
    }

    @Override
    public String getCommandName() {
        return "verack";
    }
}

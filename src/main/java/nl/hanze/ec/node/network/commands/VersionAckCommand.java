package nl.hanze.ec.node.network.commands;

import nl.hanze.ec.node.Application;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

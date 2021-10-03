package nl.hanze.ec.node.network.commands;

import nl.hanze.ec.node.Application;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VersionCommand implements Command {
    @Override
    public JSONObject execute() {
        JSONObject payload = new JSONObject();

        payload.put("command", getCommandName());

        payload.put("version", Application.VERSION);

        // TODO retrieve correct value
        payload.put("start_height", 0);

        return payload;
    }

    public String getCommandName() {
        return "version";
    }
}

package nl.hanze.ec.node.network.commands;

import nl.hanze.ec.node.exceptions.InvalidCommand;
import org.json.JSONObject;

public class CommandFactory {
    public static Command create(JSONObject payload) throws InvalidCommand {
        switch (payload.getString("command")) {
            case "verack":
                return new VersionAckCommand();
            default:
                throw new InvalidCommand("Invalid or no command found in payload");
        }
    }
}

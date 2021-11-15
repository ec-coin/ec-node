package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.exceptions.InvalidCommand;
import org.json.JSONObject;

public class CommandFactory {
    public static Command create(JSONObject payload) throws InvalidCommand {
        switch (payload.getString("command")) {
            case "version":
                return new VersionCommand(payload);
            case "verack":
                return new VersionAckCommand();
            case "test":
                return new TestAnnouncement(payload);
            default:
                throw new InvalidCommand("Invalid or no command found in payload");
        }
    }
}

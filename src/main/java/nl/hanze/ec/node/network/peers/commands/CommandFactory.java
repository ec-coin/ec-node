package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.exceptions.InvalidCommand;
import nl.hanze.ec.node.network.peers.commands.announcements.TestAnnouncement;
import nl.hanze.ec.node.network.peers.commands.requests.VersionCommand;
import nl.hanze.ec.node.network.peers.commands.responses.VersionAckCommand;
import org.json.JSONObject;

public class CommandFactory {
    public static Command create(JSONObject payload) throws InvalidCommand {
        switch (payload.getString("command")) {
            case "version":
                return new VersionCommand(payload);
            case "verack":
                return new VersionAckCommand();
            case "test-announcement":
                return new TestAnnouncement(payload);
            default:
                throw new InvalidCommand("Invalid or no command found in payload");
        }
    }
}

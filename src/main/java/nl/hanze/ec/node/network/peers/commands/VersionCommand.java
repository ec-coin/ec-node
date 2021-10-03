package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.Application;
import org.json.JSONObject;

public class VersionCommand implements Command {
    double version;
    int start_height;

    public VersionCommand(JSONObject payload) {
        this.version = payload.getDouble("version");
        this.start_height = payload.getInt("start_height");
    }

    public VersionCommand() {
        // TODO retrieve correct start height
        this.version = Application.VERSION;
        this.start_height = 0;
    }

    @Override
    public JSONObject execute() {
        JSONObject payload = new JSONObject();

        payload.put("command", getCommandName());

        payload.put("version", this.version);

        payload.put("start_height", this.start_height);

        return payload;
    }

    public String getCommandName() {
        return "version";
    }

    public double getVersion() { return version; }
}

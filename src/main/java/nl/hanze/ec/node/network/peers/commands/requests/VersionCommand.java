package nl.hanze.ec.node.network.peers.commands.requests;

import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.Handshake;
import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class VersionCommand implements Handshake {
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
    public JSONObject getPayload() {
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

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        throw new UnsupportedOperationException("Workers not supported for Handshake commands.");
    }
}

package nl.hanze.ec.node.network.peers.commands.handshake;

import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class VersionCommand extends Command implements Handshake {
    double version;
    int start_height;

    public VersionCommand() {
        this.version = Application.VERSION;

        // TODO retrieve correct start height
        this.start_height = 0;
    }

    public VersionCommand(JSONObject payload) {
        super(payload);
        this.version = payload.getDouble("version");
        this.start_height = payload.getInt("start_height");
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("version", this.version);

        payload.put("start_height", this.start_height);

        return payload;
    }

    public double getVersion() { return version; }

    @Override
    public String getCommandName() {
        return "version";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        throw new UnsupportedOperationException("Workers not supported for Handshake commands.");
    }
}

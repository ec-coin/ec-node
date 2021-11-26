package nl.hanze.ec.node.network.peers.commands.handshake;

import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class VersionAckCommand extends AbstractCommand implements Handshake {
    public VersionAckCommand() {
    }

    public VersionAckCommand(JSONObject payload) {
        super(payload);
    }

    @Override
    public String getCommandName() {
        return "verack";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        throw new UnsupportedOperationException("Workers not supported for Handshake commands.");
    }
}

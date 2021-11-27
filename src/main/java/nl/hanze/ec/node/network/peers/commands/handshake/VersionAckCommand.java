package nl.hanze.ec.node.network.peers.commands.handshake;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.Worker;
import nl.hanze.ec.node.workers.WorkerFactory;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class VersionAckCommand extends Command implements Handshake {
    public VersionAckCommand() {
    }

    public VersionAckCommand(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
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

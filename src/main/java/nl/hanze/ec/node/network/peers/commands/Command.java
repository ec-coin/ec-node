package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public interface Command {
    JSONObject getPayload();

    String getCommandName();

    Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue);
}

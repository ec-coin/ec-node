package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.app.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public interface Command {
    JSONObject getPayload();

    int getMessageNumber();

    void setMessageNumber(int messageNumber);

    Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue);
}

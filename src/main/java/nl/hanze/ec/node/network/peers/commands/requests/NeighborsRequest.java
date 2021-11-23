package nl.hanze.ec.node.network.peers.commands.requests;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.NeighborRequestWorker;
import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class NeighborsRequest extends Command implements Request {
    public NeighborsRequest() {}

    public NeighborsRequest(JSONObject payload) {
        super(payload);
    }

    @Override
    public String getCommandName() {
        return "neighbors-request";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return new NeighborRequestWorker(receivedCommand, peerCommandQueue);
    }
}

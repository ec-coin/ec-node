package nl.hanze.ec.node.network.peers.commands.requests;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.NeighborRequestWorker;
import nl.hanze.ec.node.workers.Worker;
import nl.hanze.ec.node.workers.WorkerFactory;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class NeighborsRequest extends Command implements Request {
    public NeighborsRequest() {}

    public NeighborsRequest(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
    }

    @Override
    public String getCommandName() {
        return "neighbors-request";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(NeighborRequestWorker.class, receivedCommand, peerCommandQueue);
    }
}

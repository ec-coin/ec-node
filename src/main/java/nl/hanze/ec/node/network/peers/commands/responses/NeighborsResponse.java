package nl.hanze.ec.node.network.peers.commands.responses;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.NeighborRequestWorker;
import nl.hanze.ec.node.workers.NeighborResponseWorker;
import nl.hanze.ec.node.workers.Worker;
import nl.hanze.ec.node.workers.WorkerFactory;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class NeighborsResponse extends Command implements Response {
    String ip;
    int port;
    int responseTo;

    public NeighborsResponse(String ip, int port, int responseTo) {
        this.ip = ip;
        this.port = port;
        this.responseTo = responseTo;
    }

    public NeighborsResponse(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
        this.ip = payload.getString("ip");
        this.port = payload.getInt("port");
        this.responseTo = payload.getInt("responseTo");
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("ip", this.ip);
        payload.put("port", this.port);
        payload.put("responseTo", this.responseTo);

        return payload;
    }

    @Override
    public String getCommandName() {
        return "neighbors-response";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(NeighborResponseWorker.class, receivedCommand, peerCommandQueue);
    }

    @Override
    public Integer inResponseTo() {
        return this.responseTo;
    }
}

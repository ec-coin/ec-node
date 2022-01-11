package nl.hanze.ec.node.network.peers.commands.requests;

import nl.hanze.ec.node.app.workers.HeadersRequestWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class HeadersRequest extends AbstractCommand implements Request {
    private String blockHash;

    public HeadersRequest(String blockHash) {
        this.blockHash = blockHash;
    }

    public HeadersRequest(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
        this.blockHash = payload.getString("blockHash");
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("blockHash", this.blockHash);

        return payload;
    }

    public String getBlockHash() {
        return blockHash;
    }

    @Override
    public String getCommandName() {
        return "headers-request";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(HeadersRequestWorker.class, receivedCommand, peerCommandQueue);
    }
}

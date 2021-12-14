package nl.hanze.ec.node.network.peers.commands.requests;

import nl.hanze.ec.node.app.workers.HeadersRequestWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class BlocksRequest extends AbstractCommand implements Request {
    List<Object> blockHashes;

    public BlocksRequest(List<Object> blockHashes) {
        this.blockHashes = blockHashes;
    }

    public BlocksRequest(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
        this.blockHashes = payload.getJSONArray("blockHashes").toList();
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("blockHashes", this.blockHashes);

        return payload;
    }

    public List<Object> getBlockHashes() {
        return blockHashes;
    }

    @Override
    public String getCommandName() {
        return "block-request";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(HeadersRequestWorker.class, receivedCommand, peerCommandQueue);
    }
}

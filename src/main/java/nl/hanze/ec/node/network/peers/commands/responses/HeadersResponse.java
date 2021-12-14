package nl.hanze.ec.node.network.peers.commands.responses;

import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class HeadersResponse extends AbstractCommand implements Response {
    List<Object> blockHashes;
    int responseTo;

    public HeadersResponse(List<Object> blockHashes, int responseTo) {
        this.blockHashes = blockHashes;
        this.responseTo = responseTo;
    }

    public HeadersResponse(JSONObject payload, WorkerFactory workerFactory) {
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
        return "headers-response";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return null;
    }

    @Override
    public Integer inResponseTo() {
        return this.responseTo;
    }
}

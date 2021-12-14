package nl.hanze.ec.node.network.peers.commands.requests;

import nl.hanze.ec.node.app.workers.InventoryRequestWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.utils.CollectionMappingUtils;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class InventoryRequest extends AbstractCommand implements Request {
    private List<String> blockHashes;

    public InventoryRequest(List<String> blockHashes) {
        this.blockHashes = blockHashes;
    }

    public InventoryRequest(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
        this.blockHashes = CollectionMappingUtils.mapToStringList(payload.getJSONArray("blockHashes").toList());
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("blockHashes", this.blockHashes);

        return payload;
    }

    public List<String> getBlockHashes() {
        return blockHashes;
    }

    @Override
    public String getCommandName() {
        return "inventory-request";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(InventoryRequestWorker.class, receivedCommand, peerCommandQueue);
    }
}

package nl.hanze.ec.node.network.peers.commands.requests;

import nl.hanze.ec.node.app.workers.TransactionsRequestWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class TransactionsRequest extends AbstractCommand implements Request {
    String blockHash;

    public TransactionsRequest(String blockHash) {
        this.blockHash = blockHash;
    }

    public TransactionsRequest(JSONObject payload, WorkerFactory workerFactory) {
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
        return "tx-request";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(TransactionsRequestWorker.class, receivedCommand, peerCommandQueue);
    }
}

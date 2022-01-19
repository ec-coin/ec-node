package nl.hanze.ec.node.network.peers.commands.announcements;

import nl.hanze.ec.node.app.workers.NewBlockAnnouncementWorker;
import nl.hanze.ec.node.app.workers.PendingTransactionWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class PendingTransactionAnnouncement extends AbstractCommand implements Announcement {
    JSONObject transaction;
    boolean validated = false;

    public PendingTransactionAnnouncement(JSONObject transaction) {
        this.transaction = transaction;
    }

    public PendingTransactionAnnouncement(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
        this.transaction = payload.getJSONObject("transaction");
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("transaction", this.transaction);

        return payload;
    }

    @Override
    public String getCommandName() {
        return "new-transaction";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(PendingTransactionWorker.class, receivedCommand, peerCommandQueue);
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }
}

package nl.hanze.ec.node.network.peers.commands.announcements;

import nl.hanze.ec.node.app.workers.NewBlockAnnouncementWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class BlockConfirmationAnnouncement extends AbstractCommand implements Announcement {
    JSONObject block;

    public BlockConfirmationAnnouncement(JSONObject block) {
        this.block = block;
    }

    public BlockConfirmationAnnouncement(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
        this.block = payload.getJSONObject("block");
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("block", this.block);

        return payload;
    }

    @Override
    public String getCommandName() {
        return "new-block";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return workerFactory.create(NewBlockAnnouncementWorker.class, receivedCommand, peerCommandQueue);
    }
}

package nl.hanze.ec.node.network.peers.commands.announcements;

import nl.hanze.ec.node.app.workers.NewBlockAnnouncementWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class NewBlockAnnouncement extends AbstractCommand implements Announcement {
    JSONObject block;

    public NewBlockAnnouncement(JSONObject block) {
        this.block = block;
    }

    public NewBlockAnnouncement(JSONObject payload, WorkerFactory workerFactory) {
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
        return "new-block-announcement";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return new NewBlockAnnouncementWorker(receivedCommand, peerCommandQueue);
    }
}

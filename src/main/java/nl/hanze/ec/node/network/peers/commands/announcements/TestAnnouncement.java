package nl.hanze.ec.node.network.peers.commands.announcements;

import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.app.workers.TestAnnouncementWorker;
import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class TestAnnouncement extends AbstractCommand implements Announcement {
    String msg;

    public TestAnnouncement(String msg) {
        this.msg = msg;
    }

    public TestAnnouncement(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);
        this.msg = payload.getString("msg");
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("msg", this.msg);

        return payload;
    }

    @Override
    public String getCommandName() {
        return "test-announcement";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return new TestAnnouncementWorker(receivedCommand, peerCommandQueue);
    }

    @Override
    public boolean validated() {
        return false;
    }
}

package nl.hanze.ec.node.network.peers.commands.announcements;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.TestAnnouncementWorker;
import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class TestAnnouncement extends Command {
    String msg;

    public TestAnnouncement(String msg) {
        this.msg = msg;
    }

    public TestAnnouncement(JSONObject payload) {
        super(payload);
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
}

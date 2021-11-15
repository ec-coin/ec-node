package nl.hanze.ec.node.network.peers.commands.announcements;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.TestAnnouncementWorker;
import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class TestAnnouncement implements Command {
    String msg;

    public TestAnnouncement(JSONObject payload) {
        this.msg = payload.getString("msg");
    }

    public TestAnnouncement() {
        this.msg = "Hello world";
    }

    @Override
    public JSONObject getPayload() {
        JSONObject payload = new JSONObject();

        payload.put("command", getCommandName());

        payload.put("msg", this.msg);

        return payload;
    }

    public String getCommandName() {
        return "test-announcement";
    }

    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return new TestAnnouncementWorker(receivedCommand, peerCommandQueue);
    }
}

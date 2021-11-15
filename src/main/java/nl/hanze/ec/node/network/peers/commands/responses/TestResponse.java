package nl.hanze.ec.node.network.peers.commands.responses;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.TestAnnouncementWorker;
import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class TestResponse implements Command {
    String msg;

    public TestResponse(JSONObject payload) {
        this.msg = payload.getString("msg");
    }

    public TestResponse() {
        this.msg = "Test response command message";

    }

    @Override
    public JSONObject getPayload() {
        JSONObject payload = new JSONObject();

        payload.put("command", getCommandName());

        payload.put("msg", this.msg);

        return payload;
    }

    public String getCommandName() {
        return "test-response";
    }

    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return new TestAnnouncementWorker(receivedCommand, peerCommandQueue);
    }
}

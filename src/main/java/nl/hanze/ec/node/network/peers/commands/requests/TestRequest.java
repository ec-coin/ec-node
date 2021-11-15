package nl.hanze.ec.node.network.peers.commands.requests;

import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class TestRequest implements Command {
    String msg;

    public TestRequest(JSONObject payload) {
        this.msg = payload.getString("msg");
    }

    public TestRequest() {
        this.msg = "Test request command message";
    }

    @Override
    public JSONObject getPayload() {
        JSONObject payload = new JSONObject();

        payload.put("command", getCommandName());

        payload.put("msg", this.msg);

        return payload;
    }

    @Override
    public String getCommandName() {
        return "test-request";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return null;
    }
}

package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.workers.TestWorker;
import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class TestCommand implements Command {
    String msg;

    public TestCommand(JSONObject payload) {
        this.msg = payload.getString("msg");
    }

    public TestCommand() {
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
        return "test";
    }

    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return new TestWorker(receivedCommand, peerCommandQueue);
    }
}

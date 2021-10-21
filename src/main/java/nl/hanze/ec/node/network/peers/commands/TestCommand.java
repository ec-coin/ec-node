package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.Application;
import org.json.JSONObject;

public class TestCommand implements Command {
    String msg;

    public TestCommand(JSONObject payload) {
        this.msg = payload.getString("msg");
    }

    public TestCommand() {
        // TODO retrieve correct start height
        this.msg = "Hello world";
    }

    @Override
    public JSONObject execute() {
        JSONObject payload = new JSONObject();

        payload.put("command", getCommandName());

        payload.put("msg", this.msg);

        return payload;
    }

    public String getCommandName() {
        return "test";
    }
}

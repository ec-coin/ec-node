package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.app.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public abstract class AbstractCommand implements Command {
    protected int messageNumber;

    public AbstractCommand() {}

    public AbstractCommand(JSONObject payload) {
        this.messageNumber = payload.getInt("number");
    }

    public JSONObject getPayload() {
        JSONObject payload = new JSONObject();

        payload.put("command", getCommandName());
        payload.put("number", this.messageNumber);

        payload = getData(payload);

        return payload;
    }

    protected JSONObject getData(JSONObject payload) {
        return payload;
    }

    public int getMessageNumber() {
        return this.messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    protected abstract String getCommandName();

    public abstract Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue);
}

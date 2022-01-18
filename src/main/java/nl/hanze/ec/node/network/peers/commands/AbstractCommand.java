package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractCommand implements Command {
    protected int messageNumber;
    protected WorkerFactory workerFactory;

    public AbstractCommand() {}

    public AbstractCommand(JSONObject payload, WorkerFactory workerFactory) throws ValidationException {
        this.messageNumber = payload.getInt("number");
        this.workerFactory = workerFactory;

        // Validate if json is well formatted
        InputStream inputStream = getClass().getResourceAsStream("/json-schemas/" + getCommandName() + ".json");
        JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
        Schema schema = SchemaLoader.load(rawSchema);
        schema.validate(payload);
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

    /**
     * Two commands are said to be equal when their command name is the equal
     * and their getData() return values are the same.
     *
     * @param o object to compare against
     * @return boolean indicating if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCommand)) return false;
        AbstractCommand that = (AbstractCommand) o;
        return Objects.equals(getData(new JSONObject()).toString(), that.getData(new JSONObject()).toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandName());
    }
}

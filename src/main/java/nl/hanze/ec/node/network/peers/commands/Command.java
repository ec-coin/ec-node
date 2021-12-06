package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.app.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public interface Command {
    JSONObject getPayload();

    int getMessageNumber();

    void setMessageNumber(int messageNumber);

    Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue);

    /**
     * Two commands are said to be equal when their command name is the equal
     * and their getData() return values are the same.
     *
     * @param o object to compare against
     * @return boolean indicating if the objects are equal
     */
    boolean equals(Object o);
}

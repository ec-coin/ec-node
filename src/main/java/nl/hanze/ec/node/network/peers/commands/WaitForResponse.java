package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.network.peers.commands.requests.Request;
import nl.hanze.ec.node.network.peers.commands.responses.Response;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WaitForResponse implements Command {
    protected Request command;
    protected Response response;
    private long timeout;
    private CountDownLatch latch = new CountDownLatch(1);

    public WaitForResponse(Request command) {
        this.command = command;
        this.timeout = 60L;
    }

    public WaitForResponse(Request command, long timeout) {
        this.command = command;
        this.timeout = timeout;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return this.response;
    }

    @Override
    public JSONObject getPayload() {
        return command.getPayload();
    }

    @Override
    public int getMessageNumber() {
        return command.getMessageNumber();
    }

    @Override
    public void setMessageNumber(int messageNumber) {
        command.setMessageNumber(messageNumber);
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return command.getWorker(receivedCommand, peerCommandQueue);
    }

    /**
     * @return true if the response received and false if the waiting time elapsed before response was received
     */
    public boolean await() {
        try {
            return latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Notify the awaiting threads that the response has been handled
     */
    public void resolve() {
        latch.countDown();
    }
}

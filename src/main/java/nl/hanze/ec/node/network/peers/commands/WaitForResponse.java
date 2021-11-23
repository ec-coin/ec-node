package nl.hanze.ec.node.network.peers.commands;

import nl.hanze.ec.node.workers.Worker;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WaitForResponse extends Command {
    protected Command command;
    protected long timeout;
    CountDownLatch latch = new CountDownLatch(1);

    public WaitForResponse(Command command) {
        this.command = command;
        this.timeout = 60L;
    }

    public WaitForResponse(Command command, long timeout) {
        this.command = command;
        this.timeout = timeout;
    }

    @Override
    public JSONObject getPayload() {
        return command.getPayload();
    }

    @Override
    public String getCommandName() {
        return command.getCommandName();
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

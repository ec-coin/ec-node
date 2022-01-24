package nl.hanze.ec.node.app.listeners;

import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class StateListener implements Listener {
    protected final BlockingQueue<NodeState> nodeStateQueue;
    protected final PeerPool peerPool;
    protected CountDownLatch latch;

    /**
     * Is the Listener running?
     */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Constructs listener
     *
     * @param nodeStateQueue queue that holds node state changes.
     * @param peerPool the peer pool used to communicate with peers.
     */
    public StateListener(BlockingQueue<NodeState> nodeStateQueue, PeerPool peerPool) {
        this.nodeStateQueue = nodeStateQueue;
        this.peerPool = peerPool;
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void run() {
        while (running.get()) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            waitIfStateIncorrect();

            iteration();
        }
    }

    public void stateChanged(NodeState state) {
        if (state == NodeState.CLOSING) {
            running.set(false);
        }

        if (latch.getCount() == 0) {
            this.latch = new CountDownLatch(1);
        }

        if (listenFor().contains(state)) {
            this.latch.countDown();
        }
    }

    /**
     * Sleep thread when current node state is not in listenFor() (i.e. latch != 1)
     */
    protected void waitIfStateIncorrect() {
        try {
            if (this.latch.getCount() != 0) {
                beforeSleep();
            }

            this.latch.await();

            beforeWakeup();
        } catch (InterruptedException ignore) {}
    }

    protected void beforeSleep() {}

    protected void beforeWakeup() {}

    protected abstract void iteration();
}

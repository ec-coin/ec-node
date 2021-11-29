package nl.hanze.ec.node.app.listeners;

import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public abstract class StateListener implements Listener {
    protected final BlockingQueue<NodeState> nodeStateQueue;
    protected final PeerPool peerPool;
    private CountDownLatch latch;

    public StateListener(BlockingQueue<NodeState> nodeStateQueue, PeerPool peerPool) {
        this.nodeStateQueue = nodeStateQueue;
        this.peerPool = peerPool;
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void run() {
        while (true) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            doWork();
        }
    }

    public void stateChanged(NodeState state) {
        if (latch.getCount() == 0) {
            this.latch = new CountDownLatch(1);
        }

        if (listenFor().contains(state)) {
            this.latch.countDown();
        }

    }

    protected abstract void doWork();
}

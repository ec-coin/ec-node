package nl.hanze.ec.node.app.listeners;

import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.concurrent.BlockingQueue;

public abstract class StateListener implements Listener {
    protected final BlockingQueue<NodeState> nodeStateQueue;
    protected final PeerPool peerPool;

    public StateListener(BlockingQueue<NodeState> nodeStateQueue, PeerPool peerPool) {
        this.nodeStateQueue = nodeStateQueue;
        this.peerPool = peerPool;
    }

    @Override
    public void run() {
        while (true) {
            try {
                NodeState state = nodeStateQueue.take();
                NodeState incomingState;

                while (listenFor().contains(state)) {
                    doWork();

                    if ((incomingState = nodeStateQueue.poll()) != null) {
                        state = incomingState;
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }

    protected abstract void doWork();
}

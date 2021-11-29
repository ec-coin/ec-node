package nl.hanze.ec.node.app.listeners;

import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.concurrent.BlockingQueue;

public abstract class StateListener implements Listener {
    protected final BlockingQueue<NodeState> nodeStateQueue;
    protected final PeerPool peerPool;
    private boolean stateChange;

    public StateListener(BlockingQueue<NodeState> nodeStateQueue, PeerPool peerPool) {
        this.nodeStateQueue = nodeStateQueue;
        this.peerPool = peerPool;
        this.stateChange = true;
    }

    @Override
    public void run() {
        while (true) {
            NodeState state = Application.getState();
            NodeState incomingState;

            while (listenFor().contains(state)) {
                doWork();

                if (stateChange) {
                    stateChange = false;
                    if ((incomingState = Application.getState()) != state) {
                        state = incomingState;
                    }
                }
            }
        }
    }

    public void stateChanged() {
        stateChange = true;
    }

    protected abstract void doWork();
}

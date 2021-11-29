package nl.hanze.ec.node.app.handlers;

import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.app.listeners.Listener;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class StateHandler implements Handler {
    protected static BlockingQueue<NodeState> nodeStateQueue;
    protected static List<Listener> listeners;
    protected final PeerPool peerPool;

    public StateHandler(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue1, PeerPool peerPool) {
        nodeStateQueue = nodeStateQueue1;
        listeners = new ArrayList<>();
        this.peerPool = peerPool;
    }

    public void addObserver(Listener listener) {
        listeners.add(listener);
    }

    public void removeObserver(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void run() {
        NodeState nodeState;
        while (true) {
            try {
                nodeState = nodeStateQueue.take();
                Application.setState(nodeState);
                for (Listener listener : listeners) {
                    listener.stateChanged();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

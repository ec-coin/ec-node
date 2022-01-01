package nl.hanze.ec.node.app.handlers;

import com.google.inject.Inject;
import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.app.listeners.Listener;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class StateHandler implements Handler {
    private final BlockingQueue<NodeState> nodeStateQueue;
    private final List<Listener> listeners;

    @Inject
    public StateHandler(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue) {
        this.nodeStateQueue = nodeStateQueue;
        this.listeners = Collections.synchronizedList(new ArrayList<>());
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
                    listener.stateChanged(nodeState);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

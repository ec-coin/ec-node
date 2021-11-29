package nl.hanze.ec.node.app.listeners;

import nl.hanze.ec.node.app.NodeState;

import java.util.List;

public interface Listener extends Runnable {
    void run();

    void stateChanged();

    List<NodeState> listenFor();
}

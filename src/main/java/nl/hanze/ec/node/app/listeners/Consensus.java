package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Consensus extends StateListener {
    public static int i = 0;

    private final List<NodeState> listenFor = new ArrayList<>() {
        {
            add(NodeState.PARTICIPATING);
        }
    };

    @Inject
    public Consensus(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue, PeerPool peerPool) {
        super(nodeStateQueue, peerPool);
    }

    protected void doWork() {
        System.out.println("Hello, world!");
        i++;

        if (i == 5) {
            nodeStateQueue.add(NodeState.INIT);
            System.out.println("---------------");
        }
    }

    public List<NodeState> listenFor() {
        return listenFor;
    }
}

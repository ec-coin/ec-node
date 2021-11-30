package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class BlockSyncer extends StateListener {
    private final List<NodeState> listenFor = new ArrayList<>() {
        {
            add(NodeState.SYNCING);
        }
    };

    @Inject
    public BlockSyncer(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue, PeerPool peerPool) {
        super(nodeStateQueue, peerPool);
    }

    protected void doWork() {

    }

    public List<NodeState> listenFor() {
        return listenFor;
    }
}
package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.concurrent.BlockingQueue;

public class ListenerFactory {
    BlockingQueue<NodeState> nodeStateQueue;

    @Inject
    public ListenerFactory(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue) {
        this.nodeStateQueue = nodeStateQueue;
    }

    public Listener create(Class<? extends Listener> listener, PeerPool peerPool) {
        if (listener == Consensus.class) {
            return new Consensus(nodeStateQueue, peerPool);
        } else if (listener == BlockSyncer.class) {
            return new BlockSyncer(nodeStateQueue, peerPool);
        }

        throw new UnsupportedOperationException("Factory has not defined how the given class has to be created");
    }
}

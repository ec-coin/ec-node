package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.exceptions.InvalidCommand;
import nl.hanze.ec.node.network.peers.PeerPool;

import java.util.concurrent.BlockingQueue;

public class ListenerFactory {
    @Inject
    public ListenerFactory() {
    }

    public Listener create(Class<? extends Listener> listener, PeerPool peerPool, BlockingQueue<NodeState> nodeStateQueue) {
        if (listener == Consensus.class) {
            return new Consensus(nodeStateQueue, peerPool);
        } else if (listener == BlockSyncer.class) {
            return new BlockSyncer(nodeStateQueue, peerPool);
        }

        throw new UnsupportedOperationException("Factory has not defined how the given class has to be created");
    }
}

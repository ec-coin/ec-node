package nl.hanze.ec.node;

import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.network.peers.commands.Command;

public abstract class CommandProducer implements Runnable {
    protected PeerPool peerPool;

    public void setPeerPool(PeerPool peerPool) {
        this.peerPool = peerPool;
    }

    public void run() {
        if (peerPool == null) {
            throw new RuntimeException("Peer Pool not set");
        }
    }

    protected abstract void handle();
}

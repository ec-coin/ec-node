package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import nl.hanze.ec.node.network.Server;
import nl.hanze.ec.node.network.peers.peer.Peer;
import nl.hanze.ec.node.network.peers.PeerPool;

import javax.inject.Named;

public class NetworkModule extends AbstractModule  {
    @Provides
    Server providesServer(@Named("Port") int port) {
        return new Server(port);
    }

    @Provides
    PeerPool providesPeerPool(@Named("MaxPeers") int maxPeers, @Named("Port") int port) {
        return new PeerPool(maxPeers, new Peer[] {
            new Peer("127.0.0.1", port + 1)
        });
    }
}

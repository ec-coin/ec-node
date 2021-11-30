package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.modules.annotations.IncomingConnectionsQueue;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadCommunicationModule extends AbstractModule {
    @Provides
    @Singleton
    @IncomingConnectionsQueue
    BlockingQueue<Socket> providesIncomingConnectionsQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Provides
    @Singleton
    @NodeStateQueue
    BlockingQueue<NodeState> providesNodeStateQueue() {
        return new LinkedBlockingQueue<>();
    }
}

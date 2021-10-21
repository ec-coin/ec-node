package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import nl.hanze.ec.node.CommandConsumer;
import nl.hanze.ec.node.modules.annotations.CommandConsumerQueues;
import nl.hanze.ec.node.modules.annotations.IncomingConnectionsQueue;
import nl.hanze.ec.node.network.peers.commands.Command;

import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadCommunicationModule extends AbstractModule {
    @Provides
    @Singleton
    @CommandConsumerQueues
    Map<CommandConsumer, BlockingQueue<Command>> providesCommandConsumerQueues() {
        return Collections.synchronizedMap(new HashMap<>());
    }

    @Provides
    @Singleton
    @IncomingConnectionsQueue
    BlockingQueue<Socket> providesIncomingConnectionsQueue() {
        return new LinkedBlockingQueue<>();
    }
}

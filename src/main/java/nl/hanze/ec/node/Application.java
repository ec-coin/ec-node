package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.modules.annotations.CommandResponderQueues;
import nl.hanze.ec.node.network.ConnectionManager;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.utils.FileUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Application {
    public static final double VERSION = 1.0;

    private final ConnectionManager connectionManager;
    private final List<CommandResponder> responders;
    private final List<CommandProducer> producers;
    public final Map<CommandResponder, BlockingQueue<Command>> commandResponderQueues;

    @Inject
    public Application(
            ConnectionManager connectionManager,
            ArrayList<CommandResponder> responders,
            ArrayList<CommandProducer> producers,
            @CommandResponderQueues Map<CommandResponder, BlockingQueue<Command>> commandResponderQueues,
            TestResponderComponent component1,
            TestProducerComponent component2
    ) {
        this.connectionManager = connectionManager;
        this.responders = responders;
        this.producers = producers;
        this.commandResponderQueues = commandResponderQueues;

        this.addCommandResponder(component1);

        this.addCommandProducer(component2);
    }

    private void addCommandResponder(CommandResponder responder) {
        responders.add(responder);

        commandResponderQueues.put(responder, new LinkedBlockingQueue<>());

        responder.setQueue(commandResponderQueues.get(responder));
    }

    private void addCommandProducer(CommandProducer producer) {
        this.producers.add(producer);

        producer.setPeerPool(connectionManager.getPeerPool());
    }

    /**
     * Launches the application
     */
    public void run() {
        // Prints welcome message to console
        System.out.println(FileUtils.readFromResources("welcome.txt"));

        // Sets up the connection manager
        this.connectionManager.setup();

        // Start all command responders
        for (CommandResponder responder : responders) {
            (new Thread(responder)).start();
        }

        // Start all command producers
        for (CommandProducer producer : producers) {
            (new Thread(producer)).start();
        }
    }
}

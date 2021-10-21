package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.modules.annotations.CommandConsumerQueues;
import nl.hanze.ec.node.network.ConnectionManager;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.utils.FileUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Application {
    public static final double VERSION = 1.0;

    private final ConnectionManager connectionManager;
    private final List<CommandConsumer> consumers;
    private final List<CommandProducer> producers;
    public final Map<CommandConsumer, BlockingQueue<Command>> commandConsumerQueues;

    @Inject
    public Application(
            ConnectionManager connectionManager,
            ArrayList<CommandConsumer> consumers,
            ArrayList<CommandProducer> producers,
            @CommandConsumerQueues Map<CommandConsumer, BlockingQueue<Command>> commandConsumerQueues,
            TestConsumerComponent component1,
            TestProduceComponent component2
    ) {
        this.connectionManager = connectionManager;
        this.consumers = consumers;
        this.producers = producers;
        this.commandConsumerQueues = commandConsumerQueues;

        this.addCommandConsumer(component1);

        this.addCommandProducer(component2);
    }

    private void addCommandConsumer(CommandConsumer consumer) {
        consumers.add(consumer);

        commandConsumerQueues.put(consumer, new LinkedBlockingQueue<>());

        consumer.setQueue(commandConsumerQueues.get(consumer));
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

        // Start all command consumer
        for (CommandConsumer consumer : consumers) {
            (new Thread(consumer)).start();
        }

        // Start all command producers
        for (CommandProducer producer : producers) {
            (new Thread(producer)).start();
        }
    }
}

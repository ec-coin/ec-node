package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.app.handlers.Handler;
import nl.hanze.ec.node.app.handlers.StateHandler;
import nl.hanze.ec.node.app.listeners.BlockSyncer;
import nl.hanze.ec.node.app.listeners.Consensus;
import nl.hanze.ec.node.app.listeners.Listener;
import nl.hanze.ec.node.app.listeners.ListenerFactory;
import nl.hanze.ec.node.network.Server;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.utils.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sun.misc.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Application {
    public static final double VERSION = 1.0;

    private static final Logger logger = LogManager.getLogger(Application.class);
    private final Server server;
    private final PeerPool peerPool;
    private final List<Class<? extends Listener>> listeners = new ArrayList<>() {
        {
            add(Consensus.class);
            add(BlockSyncer.class);
        }
    };
    private final Handler stateHandler;
    private final ListenerFactory listenerFactory;
    private static final AtomicReference<NodeState> state = new AtomicReference<>(NodeState.INIT);

    @Inject
    public Application(Server server,
                       PeerPool peerPool,
                       StateHandler stateHandler,
                       ListenerFactory listenerFactory
    ) {
        this.server = server;
        this.peerPool = peerPool;
        this.listenerFactory = listenerFactory;
        this.stateHandler = stateHandler;
    }

    /**
     * Launches the application
     */
    public void run() {
        //  Prints welcome message to console
        String motd = FileUtils.readFromResources("welcome.txt");
        if (!motd.equals("")) {
            System.out.println(motd);
        }

        // Sets up server and client communication
        Thread serverThread = new Thread(this.server);
        Thread peersThread = new Thread(this.peerPool);

        serverThread.start();
        peersThread.start();

        // Initialize handler(s)
        Thread stateHandlerThread = new Thread(stateHandler);
        stateHandlerThread.start();

        // Initialize and start all listeners.
        for (Class<? extends Listener> listener : listeners) {
            Listener concreteListener = listenerFactory.create(listener, peerPool);
            stateHandler.addObserver(concreteListener);
            new Thread(concreteListener).start();
        }

        // Callback when application is closing (NOT GUARANTEED)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application is closing");
            peerPool.closeAll();
            setState(NodeState.CLOSING);
        }));
    }

    public static NodeState getState() {
        return state.get();
    }

    public static void setState(NodeState nodeState) {
        state.set(nodeState);
    }
}

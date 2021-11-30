package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.app.handlers.Handler;
import nl.hanze.ec.node.app.handlers.StateHandler;
import nl.hanze.ec.node.app.listeners.BlockSyncer;
import nl.hanze.ec.node.app.listeners.Consensus;
import nl.hanze.ec.node.app.listeners.Listener;
import nl.hanze.ec.node.app.listeners.ListenerFactory;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.modules.annotations.Delay;
import nl.hanze.ec.node.network.Server;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.utils.FileUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Application {
    public static final double VERSION = 1.0;

    private final Server server;
    private final PeerPool peerPool;
    private final int delay;
    private final List<Class<? extends Listener>> listeners = new ArrayList<>() {
        {
            add(Consensus.class);
            add(BlockSyncer.class);
        }
    };
    private final BlockingQueue<NodeState> nodeStateQueue;
    private final ListenerFactory listenerFactory;
    private static final AtomicReference<NodeState> state = new AtomicReference<>(NodeState.INIT);

    @Inject
    public Application(Server server, PeerPool peerPool,
                       @Delay int delay,
                       @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
                       ListenerFactory listenerFactory
    ) {
        this.server = server;
        this.peerPool = peerPool;
        this.nodeStateQueue = nodeStateQueue;
        this.delay = delay;
        this.listenerFactory = listenerFactory;
    }

    /**
     * Launches the application
     */
    public void run() {
        if (delay != 99999) {
            try {
                Thread.sleep(delay * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //  Prints welcome message to console
        System.out.println(FileUtils.readFromResources("welcome.txt"));


        // Sets up server and client communication
        Thread serverThread = new Thread(this.server);
        Thread peersThread = new Thread(this.peerPool);

        serverThread.start();
        peersThread.start();

        // Initialize handler(s)
        Handler stateHandler = new StateHandler(nodeStateQueue);
        Thread stateHandlerThread = new Thread(stateHandler);
        stateHandlerThread.start();

        // Initialize and start all listeners.
        for (Class<? extends Listener> listener : listeners) {
            Listener concreteListener = listenerFactory.create(listener, peerPool, nodeStateQueue);
            stateHandler.addObserver(concreteListener);
            new Thread(concreteListener).start();
        }
    }

    public static NodeState getState() {
        return state.get();
    }

    public static void setState(NodeState nodeState) {
        state.set(nodeState);
    }
}

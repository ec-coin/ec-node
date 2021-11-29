package nl.hanze.ec.node;

import com.google.inject.Inject;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.app.handlers.Handler;
import nl.hanze.ec.node.app.handlers.StateHandler;
import nl.hanze.ec.node.app.listeners.BlockSyncer;
import nl.hanze.ec.node.app.listeners.Consensus;
import nl.hanze.ec.node.app.listeners.Listener;
import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.modules.annotations.DatabaseConnection;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.Server;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.utils.FileUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Application {
    public static final double VERSION = 1.0;

    private final Server server;
    private final PeerPool peerPool;
    private final List<Class<? extends Listener>> listeners = new ArrayList<>() {
        {
            add(Consensus.class);
            add(BlockSyncer.class);
        }
    };
    private final ConnectionSource databaseConnection;
    private final BlockingQueue<NodeState> nodeStateQueue;
    private static final AtomicReference<NodeState> state = new AtomicReference<>(NodeState.INIT);

    @Inject
    public Application(Server server,
                       PeerPool peerPool,
                       @DatabaseConnection ConnectionSource databaseConnection,
                       @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue) {
        this.databaseConnection = databaseConnection;
        this.server = server;
        this.peerPool = peerPool;
        this.nodeStateQueue = nodeStateQueue;
    }

    /**
     * Launches the application
     */
    public void run() {
        //  Prints welcome message to console.
        System.out.println(FileUtils.readFromResources("welcome.txt"));

        // Initialize and start server / peerPool.
        Thread serverThread = new Thread(this.server);
        Thread peersThread = new Thread(this.peerPool);
        serverThread.start();
        peersThread.start();

        // Setup database
        setupDatabase();

        // Initialize handler(s)
        Handler stateHandler = new StateHandler(nodeStateQueue, peerPool);
        Thread stateHandlerThread = new Thread(stateHandler);
        stateHandlerThread.start();

        // Initialize and start all listeners.
        for (Class<? extends Listener> listener : listeners) {
            try {
                Constructor<?> constructor = listener.getConstructor(BlockingQueue.class, PeerPool.class);
                Listener concreteListener = (Listener) constructor.newInstance(nodeStateQueue, peerPool);
                stateHandler.addObserver(concreteListener);
                new Thread(concreteListener).start();
            } catch (NoSuchMethodException |
                    InstantiationException |
                    IllegalAccessException |
                    InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupDatabase() {
        try {
            TableUtils.createTableIfNotExists(databaseConnection, Neighbour.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static NodeState getState() {
        return state.get();
    }

    public static void setState(NodeState nodeState) {
        state.set(nodeState);
    }
}

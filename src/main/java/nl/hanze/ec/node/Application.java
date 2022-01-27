package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.app.handlers.Handler;
import nl.hanze.ec.node.app.handlers.StateHandler;
import nl.hanze.ec.node.app.listeners.*;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.modules.annotations.DbSeeding;
import nl.hanze.ec.node.modules.annotations.NodeAddress;
import nl.hanze.ec.node.modules.annotations.NodeKeyPair;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.Server;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.utils.BaseNUtils;
import nl.hanze.ec.node.utils.FileUtils;
import nl.hanze.ec.node.utils.HashingUtils;
import nl.hanze.ec.node.utils.SignatureUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.util.encoders.Hex;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Application {
    public static final double VERSION = 1.0;
    private static final Logger logger = LogManager.getLogger(Application.class);
    private static final AtomicReference<NodeState> state = new AtomicReference<>(NodeState.INIT);

    private final API api;
    private final Server server;
    private final PeerPool peerPool;
    private final List<Class<? extends Listener>> listeners = new ArrayList<>() {
        {
            add(Consensus.class);
            add(BlockSyncer.class);
            add(BlockCreator.class);
        }
    };
    private final Handler stateHandler;
    private final ListenerFactory listenerFactory;
    private final BlockingQueue<NodeState> nodeStateQueue;
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final NeighboursRepository neighboursRepository;
    private final boolean shouldSeedDatabase;
    private final KeyPair keyPair;
    private final String nodeAddress;

    @Inject
    public Application(
            APIFactory apiFactory,
            Server server,
            PeerPool peerPool,
            StateHandler stateHandler,
            ListenerFactory listenerFactory,
            BlockRepository blockRepository,
            @NodeAddress String nodeAddress,
            TransactionRepository transactionRepository,
            NeighboursRepository neighboursRepository,
            @NodeKeyPair KeyPair keyPair,
            @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
            @DbSeeding boolean shouldSeedDatabase
    ) {
        this.api = apiFactory.create(peerPool);
        this.server = server;
        this.peerPool = peerPool;
        this.listenerFactory = listenerFactory;
        this.stateHandler = stateHandler;
        this.nodeStateQueue = nodeStateQueue;
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
        this.neighboursRepository = neighboursRepository;
        this.shouldSeedDatabase = shouldSeedDatabase;
        this.keyPair = keyPair;
        this.nodeAddress = nodeAddress;
    }

    /**
     * Launches the application
     */
    public void run() {
        if (blockRepository.getNumberOfBlocks() == 0) {
            createGenesisBlock();
        }

        printMessageOfTheDay();

        // Sets up server and client communication
        Thread APIThread = new Thread(this.api);
        Thread serverThread = new Thread(this.server);
        Thread peersThread = new Thread(this.peerPool);

        APIThread.start();
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
            nodeStateQueue.add(NodeState.CLOSING);
            server.close();
        }));

        // All threads have been started.
        nodeStateQueue.add(NodeState.COM_SETUP);

        while (true) {
            printMessageOfTheDay();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printMessageOfTheDay() {
        String motd = FileUtils.readFromResources("welcome.txt");
        if (!motd.equals("")) {
            System.out.println(motd);
        }

        System.out.println("------Properties------");
        System.out.println("blockHeight        : " + blockRepository.getCurrentBlockHeight());
        System.out.println("# of old neighbors : " + neighboursRepository.getNumberOfNeighbors());
        System.out.println("node's address     : " + nodeAddress);
        System.out.println("public key         : " + SignatureUtils.encodePublicKey(keyPair.getPublic()));
        System.out.println("# of pending tx's  : " + transactionRepository.getPendingTransactions().size());
        System.out.println("# of validated tx's: " + transactionRepository.getValidatedTransactions().size());
        System.out.println("# of blocks        : " + blockRepository.getAllBlocks().size());
        System.out.println("----------------------");
    }

    private void createGenesisBlock() {
        DateTime timestamp = DateTime.parse("2022-01-01T00:00:00.556Z");
        Block genesisBlock = blockRepository.createBlock("GENESIS", "NULL", "GENESIS", 0, "block", timestamp);

        String transactionHash = HashingUtils.generateTransactionHash("minter", "6oMokioyFBRWa3ozvJBN8mnbkS14qsefYL2cgoX5Zzog", 1000000, "");
        transactionRepository.createTransaction(transactionHash, genesisBlock, "minter", "6oMokioyFBRWa3ozvJBN8mnbkS14qsefYL2cgoX5Zzog", 1000000, "", "validated", "node", "", timestamp);

        transactionHash = HashingUtils.generateTransactionHash("minter", "3hLz5b6ztVQaYBAC9k4JvkJAk6vP1E3PvFHnPe4h1cjr", 1000000, "");
        transactionRepository.createTransaction(transactionHash, genesisBlock, "minter", "3hLz5b6ztVQaYBAC9k4JvkJAk6vP1E3PvFHnPe4h1cjr", 1000000, "", "validated", "node", "", timestamp);

        transactionHash = HashingUtils.generateTransactionHash("minter", "eGgM89aqjucuPybGqLPB3ASwrjpcVcE5iDEDpf4Ksxv", 1000000, "");
        transactionRepository.createTransaction(transactionHash, genesisBlock, "minter", "eGgM89aqjucuPybGqLPB3ASwrjpcVcE5iDEDpf4Ksxv", 1000000, "", "validated", "node", "", timestamp);

        // Stake registry
        transactionHash = HashingUtils.generateTransactionHash("6oMokioyFBRWa3ozvJBN8mnbkS14qsefYL2cgoX5Zzog", "stake_register", 0, "");
        transactionRepository.createTransaction(transactionHash, genesisBlock, "6oMokioyFBRWa3ozvJBN8mnbkS14qsefYL2cgoX5Zzog", "stake_register", 0, "", "validated", "node", "", timestamp);

        transactionHash = HashingUtils.generateTransactionHash("3hLz5b6ztVQaYBAC9k4JvkJAk6vP1E3PvFHnPe4h1cjr", "stake_register", 0, "");
        transactionRepository.createTransaction(transactionHash, genesisBlock, "3hLz5b6ztVQaYBAC9k4JvkJAk6vP1E3PvFHnPe4h1cjr", "stake_register", 0, "", "validated", "node", "", timestamp);

        transactionHash = HashingUtils.generateTransactionHash("eGgM89aqjucuPybGqLPB3ASwrjpcVcE5iDEDpf4Ksxv", "stake_register", 0, "");
        transactionRepository.createTransaction(transactionHash, genesisBlock, "eGgM89aqjucuPybGqLPB3ASwrjpcVcE5iDEDpf4Ksxv", "stake_register", 0, "", "validated", "node", "", timestamp);
    }

    public static NodeState getState() {
        return state.get();
    }

    public static void setState(NodeState nodeState) {
        state.set(nodeState);
    }
}

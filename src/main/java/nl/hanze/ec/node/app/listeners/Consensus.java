package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.modules.annotations.NodeAddress;
import nl.hanze.ec.node.modules.annotations.NodeKeyPair;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.network.peers.commands.announcements.NewBlockAnnouncement;
import nl.hanze.ec.node.utils.HashingUtils;
import nl.hanze.ec.node.utils.SignatureUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Consensus extends StateListener {
    private static final Logger logger = LogManager.getLogger(Consensus.class);
    private final TransactionRepository transactionRepository;
    private final String nodeAddress;

    private final List<NodeState> listenFor = new ArrayList<>() {
        {
            add(NodeState.PARTICIPATING);
        }
    };

    @Inject
    public Consensus(
        @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
        @NodeAddress String address,
        PeerPool peerPool,
        TransactionRepository transactionRepository
    ) {
        super(nodeStateQueue, peerPool);
        this.transactionRepository = transactionRepository;
        this.nodeAddress = address;
    }

    protected void iteration() {
        if (!transactionRepository.transactionThresholdReached()) {
            return;
        }

        List<String> addresses = transactionRepository.getStakeAddresses();

        float highestStake = 0;
        float currentStake;
        String leader = "";
        for (String address : addresses) {
            currentStake = transactionRepository.getBalance(address);

            if (currentStake > highestStake) {
                highestStake = currentStake;
                leader = address;
            }
        }

        System.out.println("Expecting block from: " + leader);

        if (leader.equals(this.nodeAddress)) {
            logger.info("This node has been chosen to be the leader, moving to validating");
            nodeStateQueue.add(NodeState.VALIDATING);
        }
    }

    public List<NodeState> listenFor() {
        return listenFor;
    }
}

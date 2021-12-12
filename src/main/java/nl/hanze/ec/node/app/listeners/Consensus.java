package nl.hanze.ec.node.app.listeners;

import com.google.inject.Inject;
import nl.hanze.ec.node.app.NodeState;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.modules.annotations.NodeStateQueue;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.services.HashingService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Consensus extends StateListener {
    public static int i = 0;
    private final TransactionRepository transactionRepository;
    private final NeighboursRepository neighboursRepository;
    private final BlockRepository blockRepository;
    private final String ownAddress = "333333333333333333333333333333333333333333333333333333333333";

    private final List<NodeState> listenFor = new ArrayList<>() {
        {
            add(NodeState.PARTICIPATING);
        }
    };

    public Consensus(@NodeStateQueue BlockingQueue<NodeState> nodeStateQueue, PeerPool peerPool,
                     TransactionRepository transactionRepository,
                     NeighboursRepository neighboursRepository,
                     BlockRepository blockRepository) {
        super(nodeStateQueue, peerPool);
        this.transactionRepository = transactionRepository;
        this.neighboursRepository = neighboursRepository;
        this.blockRepository = blockRepository;
    }

    protected void doWork() {
        System.out.println("Hello");
//        nodeStateQueue.add(NodeState.INIT);
//
//        // 1. Get all node addresses from DB
//        List<String> nodes = transactionRepository.getAllNodeAddresses();
//
//        // 2. Set nodes as validating nodes by paying a transaction fee.
//        for (String node : nodes) {
//            String signature = "temporary signature";
//            transactionRepository.addNodeAsValidatingNode(HashingService.hash(node), null, node, signature);
//        }
//
//        // 3. Check which node's nodeState == validating.
//        List<String> validatingNodes = transactionRepository.getAllValidatingNodes();
//
//        // 4. Determine if transaction threshold has been reached
//        if (transactionRepository.transactionThresholdReached()) {
//            // 5. Determine leader
//            String leader = getLeader(validatingNodes);
//
//            // 6. Check whether you are the leader
//            if (leader.equals(this.ownAddress)) {
//                // 7. Validate block
//                createBlock();
//            }
//        }
    }

    private String getLeader(List<String> participatingNodes) {
        String currentLeader = "";
        float highestStake = 0;
        for (String node : participatingNodes) {
            if (transactionRepository.getStake(node) > highestStake) {
                highestStake = transactionRepository.getStake(node);
                currentLeader = node;
            }
        }

        return currentLeader;
    }

    public List<NodeState> listenFor() {
        return listenFor;
    }

    private void createBlock() {
        List<Transaction> pendingTransactions = transactionRepository.getFiniteNumberOfPendingTransactions();
        int blockHeight = blockRepository.getCurrentBlockHeight();
        String prevHash = blockRepository.getCurrentBlockHash(blockHeight);
        String merkleRootHash = blockRepository.getRootMerkleHash();

        StringBuilder hashInput = new StringBuilder();
        for(Transaction transaction : pendingTransactions) {
            hashInput.append(transaction.getHash());
        }
        String blockHash = HashingService.hash(hashInput + prevHash);

        Block block = blockRepository.createBlock(blockHash, prevHash, merkleRootHash, blockHeight + 1);

        for(Transaction transaction : pendingTransactions) {
            transactionRepository.setTransactionAsValidated(transaction, block);
        }
    }
}

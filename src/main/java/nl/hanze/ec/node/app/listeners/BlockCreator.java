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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class BlockCreator extends StateListener {
    private static final Logger logger = LogManager.getLogger(BlockCreator.class);
    private final TransactionRepository transactionRepository;
    private final BlockRepository blockRepository;
    private final String nodeAddress;

    private final List<NodeState> listenFor = new ArrayList<>() {
        {
            add(NodeState.VALIDATING);
        }
    };

    @Inject
    public BlockCreator(
        @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
        @NodeAddress String address,
        PeerPool peerPool,
        TransactionRepository transactionRepository,
        BlockRepository blockRepository
    ) {
        super(nodeStateQueue, peerPool);
        this.transactionRepository = transactionRepository;
        this.blockRepository = blockRepository;
        this.nodeAddress = address;
    }

    protected void iteration() {
        List<Transaction> pendingTransactions = transactionRepository.getPendingTransactions();

        System.out.println(pendingTransactions);
        System.out.println("Pending txs:" + pendingTransactions.size());

        int blockHeight = blockRepository.getCurrentBlockHeight();
        String prevHash = blockRepository.getCurrentBlockHash(blockHeight);
        String merkleRootHash = HashingUtils.generateMerkleRootHash(pendingTransactions);
        DateTime createdAt = new DateTime();
        String blockHash = HashingUtils.generateBlockHash(merkleRootHash, prevHash, createdAt);
        Block block = blockRepository.createBlock(blockHash, prevHash, merkleRootHash, blockHeight + 1, "full", createdAt);

        int i = 0;
        for(Transaction transaction : pendingTransactions) {
            transaction.setBlock(block);
            transaction.setStatus("validated");
            transaction.setOrderInBlock(i++);
            transactionRepository.update(transaction);
        }

        peerPool.sendBroadcast(new NewBlockAnnouncement((blockRepository.getBlock(blockHash)).toJSONObject()));

        nodeStateQueue.add(NodeState.PARTICIPATING);
    }

    public List<NodeState> listenFor() {
        return listenFor;
    }
}

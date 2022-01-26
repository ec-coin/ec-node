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
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.joda.time.DateTime;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class BlockCreator extends StateListener {
    private static final Logger logger = LogManager.getLogger(BlockCreator.class);
    private final TransactionRepository transactionRepository;
    private final BlockRepository blockRepository;
    private final KeyPair keyPair;
    private final String nodeAddress;
    public static final float blockReward = 1.0f;

    private final List<NodeState> listenFor = new ArrayList<>() {
        {
            add(NodeState.VALIDATING);
        }
    };

    @Inject
    public BlockCreator(
        @NodeStateQueue BlockingQueue<NodeState> nodeStateQueue,
        @NodeAddress String address,
        @NodeKeyPair KeyPair keyPair,
        PeerPool peerPool,
        TransactionRepository transactionRepository,
        BlockRepository blockRepository
    ) {
        super(nodeStateQueue, peerPool);
        this.transactionRepository = transactionRepository;
        this.blockRepository = blockRepository;
        this.keyPair = keyPair;
        this.nodeAddress = address;
    }

    protected void iteration() {
        List<Transaction> pendingTransactions = transactionRepository.getPendingTransactions();

        // Block reward transaction
        String encodedPublicKey = SignatureUtils.encodePublicKey(keyPair.getPublic());
        DateTime timestamp = DateTime.now();
        String signature = SignatureUtils.sign(keyPair, "minter" + nodeAddress + timestamp.getMillis() + blockReward);
        System.out.println("payload BlockCreator: " + "minter" + nodeAddress + timestamp.getMillis() + blockReward);
        Transaction blockRewardTx = transactionRepository.createTransaction(null, "minter", nodeAddress, blockReward, signature, "node", encodedPublicKey, timestamp);
        pendingTransactions.add(blockRewardTx);

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
            transactionRepository.createOrUpdate(transaction);
        }

        peerPool.sendBroadcast(new NewBlockAnnouncement((blockRepository.getBlock(blockHash)).toJSONObject()));

        nodeStateQueue.add(NodeState.PARTICIPATING);
    }

    public List<NodeState> listenFor() {
        return listenFor;
    }
}

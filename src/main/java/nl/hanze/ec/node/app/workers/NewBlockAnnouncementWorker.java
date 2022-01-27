package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.app.listeners.BlockCreator;
import nl.hanze.ec.node.app.listeners.BlockSyncer;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;

import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.exceptions.InvalidTransaction;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.WaitForResponse;
import nl.hanze.ec.node.network.peers.commands.announcements.Announcement;
import nl.hanze.ec.node.network.peers.commands.requests.BlockRequest;
import nl.hanze.ec.node.utils.FloatUtils;
import nl.hanze.ec.node.utils.HashingUtils;
import nl.hanze.ec.node.utils.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class NewBlockAnnouncementWorker extends Worker {
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final BalancesCacheRepository balanceCacheRepository;

    public NewBlockAnnouncementWorker(
        Command receivedCommand,
        BlockingQueue<Command> peerCommandQueue,
        BlockRepository blockRepository,
        TransactionRepository transactionRepository,
        BalancesCacheRepository balancesCacheRepositoryProvider
    ) {
        super(receivedCommand, peerCommandQueue);

        this.transactionRepository = transactionRepository;
        this.blockRepository = blockRepository;
        this.balanceCacheRepository = balancesCacheRepositoryProvider;
    }

    @Override
    public void run() {
        JSONObject payload = receivedCommand.getPayload();
        Block block = fromJSONToObject((JSONObject) payload.get("block"));

        blockRepository.getBlock(block.getHash());

        // Check if block already exists in DB.
        if (blockRepository.getBlock(block.getHash()) != null) {
            return;
        }

        if (validateAndSaveBlock(block, ((JSONObject) payload.get("block")).getJSONArray("transactions").toList())) {
            Announcement announcement = (Announcement) receivedCommand;
            announcement.setValidated(true);
        }
    }

    private boolean validateAndSaveBlock(Block block, List<Object> transactionObjects) {
        String previousHash = blockRepository.getCurrentBlockHash(blockRepository.getCurrentBlockHeight());
        Block previousBlock = blockRepository.getBlock(previousHash);

        // Validate previous hash.
        if (!previousHash.equals(block.getPreviousBlockHash()) || (previousBlock.getBlockHeight() + 1) != block.getBlockHeight()) {
            System.out.println("INVALID BLOCK FOUND block_height: " + block.getBlockHeight() + " hash: " + block.getHash());
            System.out.println("Expected previousHash: " + previousHash + " received blockHash" + block.getPreviousBlockHash());
            System.out.println("Expected blockHeight: " + (previousBlock.getBlockHeight() + 1) + " received blockHeight: " + block.getBlockHeight());
            return false;
        }

        // Validate hash.
        String blockHash = HashingUtils.generateBlockHash(block.getMerkleRootHash(), block.getPreviousBlockHash(), block.getTimestamp());
        if (!blockHash.equals(block.getHash())) {
            System.out.println("INVALID HASH FOUND");
            return false;
        }

        List<String> transactionHashes = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction;
        int blockRewardTransactions = 0;
        for (Object obj : transactionObjects) {
            if (obj instanceof HashMap) {
                HashMap<?, ?> tx = (HashMap<?, ?>) obj;

                transaction = transactionRepository.getTransaction(tx.get("hash").toString());

                if (transaction == null) {
                    transaction = new Transaction(
                            tx.get("hash").toString(),
                            null,
                            tx.get("from").toString(),
                            tx.get("to").toString(),
                            FloatUtils.parse(tx, "amount"),
                            tx.get("signature").toString(),
                            "pending",
                            tx.get("addressType").toString(),
                            tx.get("publicKey").toString(),
                            DateTime.parse(tx.get("timestamp").toString())
                    );
                }

                if (transaction.getFrom().equals("minter")) {
                    if (transaction.getAmount() > BlockCreator.blockReward) {
                        System.out.println("Block reward set to high");

                        return false;
                    }

                    blockRewardTransactions++;
                }

                if (!this.balanceCacheRepository.hasValidBalance(transaction.getFrom(), transaction.getAmount()) && !transaction.getFrom().equals("minter")) {
                    System.out.println("Invalid balance");
                    return false;
                }

                try {
                    ValidationUtils.validateTransaction(transaction);
                } catch (InvalidTransaction e) {
                    System.out.println("Invalid transaction: " + e.getMessage());
                    return false;
                }

                transactionRepository.createOrUpdate(transaction);
                transactions.add(transaction);
                transactionHashes.add(transaction.getHash());
            }
        }

        if (!HashingUtils.validateMerkleRootHash(block.getMerkleRootHash(), transactionHashes)) {
            System.out.println("Merkle Root Hash not valid");
            return false;
        }

        if (blockRewardTransactions > 1) {
            System.out.println("To many reward transactions");
            return false;
        }

        // Save in database
        blockRepository.createBlock(block);
        for (Transaction tx : transactions) {
            tx.setBlock(block);
            tx.setStatus("validated");
            transactionRepository.createOrUpdate(tx);
        }

        return true;
    }

    private Block fromJSONToObject(@NotNull JSONObject block) {
        return new Block(
                block.getString("hash"),
                block.getString("previousBlockHash"),
                block.getString("merkleRootHash"),
                block.getInt("blockHeight"),
                "block",
                DateTime.parse(block.get("timestamp").toString())
        );
    }
}

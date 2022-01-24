package nl.hanze.ec.node.app.workers;

import com.google.gson.Gson;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;

import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.exceptions.InvalidTransaction;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.announcements.Announcement;
import nl.hanze.ec.node.network.peers.commands.announcements.NewBlockAnnouncement;
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
        System.out.println("New block received");
        JSONObject payload = receivedCommand.getPayload();
        Object blockObject = payload.get("block");
        Block block = fromJSONToObject((JSONObject) blockObject);

        String previousHash = blockRepository.getCurrentBlockHash(blockRepository.getCurrentBlockHeight());
        Block previousBlock = blockRepository.getBlock(previousHash);

        // TODO: currentblock height = 100
        //       received block height = 102
        //       request block 101

        List<Object> jArray = payload.getJSONArray("transactions").toList();
        for (Object obj : jArray) {
            if (obj instanceof HashMap) {
                HashMap<?, ?> tx = (HashMap<?, ?>) obj;

                transactionRepository.update(new Transaction(
                        tx.get("hash").toString(),
                        null,
                        tx.get("from").toString(),
                        tx.get("to").toString(),
                        FloatUtils.parse(tx, "amount"),
                        tx.get("signature").toString(),
                        "pending",
                        tx.get("addressType").toString(),
                        tx.get("publicKey").toString(),
                        new DateTime(Long.parseLong(tx.get("timestamp").toString()))
                ));
            }
        }

        // Validate previous hash.
        if (!previousHash.equals(block.getPreviousBlockHash()) || (previousBlock.getBlockHeight() + 1) != block.getBlockHeight()) {
            System.out.println("INVALID BLOCK FOUND");
            return;
        }

        // Validate hash.
        String blockHash = HashingUtils.generateBlockHash(block.getMerkleRootHash(), block.getPreviousBlockHash(), block.getTimestamp());
        if (!blockHash.equals(block.getHash())) {
            System.out.println("INVALID HASH FOUND");
            return;
        }

        Boolean validTransaction = true;
        List<String> transactionHashes = new ArrayList<>();
        for (Transaction transaction : block.getTransactions()) {
            validTransaction = this.balanceCacheRepository.hasValidBalance(transaction.getFrom(), transaction.getAmount());

            if (!validTransaction) {
                break;
            }

            try {
                ValidationUtils.validateTransaction(transaction);
            } catch (InvalidTransaction e) {
                e.printStackTrace();
                validTransaction = false;
                break;
            }

            transactionHashes.add(transaction.getHash());
        }

        if (!validTransaction) {
            return;
        }

        if (!HashingUtils.validateMerkleRootHash(block.getMerkleRootHash(), transactionHashes)) {
            System.out.println("Merkle Root Hash not valid");
        }

        // Save in database

        Announcement announcement = (Announcement) receivedCommand;
        announcement.setValidated(true);
    }

    private Block fromJSONToObject(@NotNull JSONObject block) {
        return new Block(
                block.getString("hash"),
                block.getString("previousBlockHash"),
                block.getString("merkleRootHash"),
                block.getInt("blockHeight"),
                "block",
                new DateTime(Long.parseLong(block.get("timestamp").toString()))
        );
    }
}

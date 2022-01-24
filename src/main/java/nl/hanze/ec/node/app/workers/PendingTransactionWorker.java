package nl.hanze.ec.node.app.workers;

import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.exceptions.InvalidTransaction;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.announcements.Announcement;
import nl.hanze.ec.node.utils.HashingUtils;
import nl.hanze.ec.node.utils.ValidationUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;

public class PendingTransactionWorker extends Worker {
    private final BalancesCacheRepository balanceCacheRepository;
    private final TransactionRepository transactionRepository;

    public PendingTransactionWorker(
        Command receivedCommand,
        BlockingQueue<Command> peerCommandQueue,
        BalancesCacheRepository balancesCacheRepository,
        TransactionRepository transactionRepository
    ) {
        super(receivedCommand, peerCommandQueue);

        this.balanceCacheRepository = balancesCacheRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run() {
        JSONObject payload = receivedCommand.getPayload();
        Object transactionObject = payload.get("transaction");
        Transaction transaction = fromJSONToObject((JSONObject) transactionObject);

        Boolean validTransaction = this.balanceCacheRepository.hasValidBalance(transaction.getFrom(), transaction.getAmount());

        if (!validTransaction) {
            System.out.println("Invalid balance");
            return;
        }

        try {
            ValidationUtils.validateTransaction(transaction);
        } catch (InvalidTransaction e) {
            System.out.println("Invalid signature");
            return;
        }

        Announcement announcement = (Announcement) receivedCommand;
        announcement.setValidated(true);

        transactionRepository.createTransaction(
                transaction.getHash(),
                null,
                transaction.getFrom(),
                transaction.getTo(),
                transaction.getAmount(),
                transaction.getSignature(),
                "pending",
                transaction.getAddressType(),
                transaction.getPublicKey(),
                transaction.getTimestamp()
        );
    }

    private Transaction fromJSONToObject(@NotNull JSONObject tx) {
        float amount;
        if (tx.get("amount") instanceof Integer) {
            amount = ((Integer) tx.get("amount")).floatValue();
        } else {
            amount = ((BigDecimal) tx.get("amount")).floatValue();
        }

        return new Transaction(
                HashingUtils.generateTransactionHash(
                        tx.get("from").toString(),
                        tx.get("to").toString(),
                        amount,
                        tx.get("signature").toString()
                ),
                null,
                tx.get("from").toString(),
                tx.get("to").toString(),
                amount,
                tx.get("signature").toString(),
                "pending",
                tx.get("address_type").toString(),
                tx.get("public_key").toString(),
                new DateTime(Long.parseLong(tx.get("timestamp").toString()))
        );
    }
}

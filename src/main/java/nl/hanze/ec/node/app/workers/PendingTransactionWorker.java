package nl.hanze.ec.node.app.workers;

import com.google.gson.Gson;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.announcements.NewBlockAnnouncement;
import nl.hanze.ec.node.network.peers.commands.announcements.PendingTransactionAnnouncement;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.BlockingQueue;

public class PendingTransactionWorker extends Worker {
    private final BalancesCacheRepository balanceCacheRepository;

    public PendingTransactionWorker(
        Command receivedCommand,
        BlockingQueue<Command> peerCommandQueue,
        BalancesCacheRepository balancesCacheRepositoryProvider
    ) {
        super(receivedCommand, peerCommandQueue);

        this.balanceCacheRepository = balancesCacheRepositoryProvider;
    }

    @Override
    public void run() {
        System.out.println("New pending transaction received");
        JSONObject payload = receivedCommand.getPayload();
        Object transactionObject = payload.get("transaction");
        Transaction transaction = fromJSONToObject(transactionObject);

        // If this was a request a response could be sent like this.
        // peerCommandQueue.add(new TestResponse());

        Boolean validTransaction = this.balanceCacheRepository.hasValidBalance(transaction.getFrom(), transaction.getAmount());

        if (validTransaction) {
            PendingTransactionAnnouncement announcement = new PendingTransactionAnnouncement(receivedCommand.getPayload());
            announcement.notifyAll();
            peerCommandQueue.

        }
    }

    private Transaction fromJSONToObject(@NotNull Object transaction) {
        Gson gson= new Gson();
        return gson.fromJson(transaction.toString(), Transaction.class);
    }
}

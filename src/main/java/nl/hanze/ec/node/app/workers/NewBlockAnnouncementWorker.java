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

public class NewBlockAnnouncementWorker extends Worker {
    private final BalancesCacheRepository balanceCacheRepository;

    public NewBlockAnnouncementWorker(
        Command receivedCommand,
        BlockingQueue<Command> peerCommandQueue,
        BalancesCacheRepository balancesCacheRepositoryProvider
    ) {
        super(receivedCommand, peerCommandQueue);

        this.balanceCacheRepository = balancesCacheRepositoryProvider;
    }

    @Override
    public void run() {
        System.out.println("New block received");
        JSONObject payload = receivedCommand.getPayload();
        Object blockObject = payload.get("block");
        Block block = fromJSONToObject(blockObject);

//         If this was a request a response could be sent like this.
//         peerCommandQueue.add(new TestResponse());

        Boolean validTransaction = true;

        for (Transaction transaction : block.getTransactions()) {
            validTransaction = this.balanceCacheRepository.hasValidBalance(transaction.getFrom(), transaction.getAmount());

            if (!validTransaction) {
                break;
            }
        }

        if (validTransaction) {
            NewBlockAnnouncement announcement = new NewBlockAnnouncement(receivedCommand.getPayload());
            announcement.notifyAll();
        }
    }

    private Block fromJSONToObject(@NotNull Object block) {
        Gson gson= new Gson();
        return gson.fromJson(block.toString(), Block.class);
    }
}

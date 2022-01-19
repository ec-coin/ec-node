package nl.hanze.ec.node.app.workers;

import com.google.gson.Gson;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;

import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.exceptions.InvalidTransaction;
import nl.hanze.ec.node.network.peers.commands.Command;
import nl.hanze.ec.node.network.peers.commands.announcements.Announcement;
import nl.hanze.ec.node.network.peers.commands.announcements.NewBlockAnnouncement;
import nl.hanze.ec.node.utils.ValidationUtils;
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

        Boolean validTransaction = true;

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
        }

        if (validTransaction) {
            Announcement announcement = (Announcement) receivedCommand;
            announcement.setValidated(true);
        }
    }

    private Block fromJSONToObject(@NotNull Object block) {
        Gson gson= new Gson();
        return gson.fromJson(block.toString(), Block.class);
    }
}

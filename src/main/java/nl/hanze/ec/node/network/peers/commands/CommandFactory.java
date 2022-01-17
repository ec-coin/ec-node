package nl.hanze.ec.node.network.peers.commands;

import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.exceptions.InvalidCommand;
import nl.hanze.ec.node.network.peers.commands.announcements.NewBlockAnnouncement;
import nl.hanze.ec.node.network.peers.commands.announcements.PendingTransactionAnnouncement;
import nl.hanze.ec.node.network.peers.commands.announcements.TestAnnouncement;
import nl.hanze.ec.node.network.peers.commands.requests.TransactionsRequest;
import nl.hanze.ec.node.network.peers.commands.requests.HeadersRequest;
import nl.hanze.ec.node.network.peers.commands.requests.NeighborsRequest;
import nl.hanze.ec.node.network.peers.commands.handshake.VersionCommand;
import nl.hanze.ec.node.network.peers.commands.responses.HeadersResponse;
import nl.hanze.ec.node.network.peers.commands.responses.NeighborsResponse;
import nl.hanze.ec.node.network.peers.commands.handshake.VersionAckCommand;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.network.peers.commands.responses.TransactionsResponse;
import org.json.JSONObject;

public class CommandFactory {
    private final Provider<WorkerFactory> workerFactoryProvider;

    @Inject
    public CommandFactory(Provider<WorkerFactory> workerFactoryProvider) {
        this.workerFactoryProvider = workerFactoryProvider;
    }

    public Command create(JSONObject payload) throws InvalidCommand {
        switch (payload.getString("command")) {
            case "version":
                return new VersionCommand(payload, workerFactoryProvider.get());
            case "verack":
                return new VersionAckCommand(payload, workerFactoryProvider.get());
            case "test-announcement":
                return new TestAnnouncement(payload, workerFactoryProvider.get());
            case "neighbors-request":
                return new NeighborsRequest(payload, workerFactoryProvider.get());
            case "neighbors-response":
                return new NeighborsResponse(payload, workerFactoryProvider.get());
            case "headers-request":
                return new HeadersRequest(payload, workerFactoryProvider.get());
            case "headers-response":
                return new HeadersResponse(payload, workerFactoryProvider.get());
            case "tx-request":
                return new TransactionsRequest(payload, workerFactoryProvider.get());
            case "tx-response":
                return new TransactionsResponse(payload, workerFactoryProvider.get());
            case "new-block":
                return new NewBlockAnnouncement(payload, workerFactoryProvider.get());
            case "new-transaction":
                return new PendingTransactionAnnouncement(payload, workerFactoryProvider.get());
            default:
                throw new InvalidCommand("Invalid or no command found in payload");
        }
    }
}

package nl.hanze.ec.node;

import com.google.gson.Gson;
import com.google.inject.Inject;
import nl.hanze.ec.node.responses.StandardResponse;
import nl.hanze.ec.node.responses.StatusResponse;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;

import static spark.Spark.*;

public class API implements Runnable {
    private final NeighboursRepository neighboursRepository;
    private final BalancesCacheRepository balancesCacheRepository;
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;

    @Inject
    public API (NeighboursRepository neighboursRepository,
                BalancesCacheRepository balancesCacheRepository,
                BlockRepository blockRepository,
                TransactionRepository transactionRepository) {
        this.neighboursRepository = neighboursRepository;
        this.balancesCacheRepository = balancesCacheRepository;
        this.blockRepository = blockRepository;
        this.transactionRepository = transactionRepository;
    }

    public void run() {
        setupTransactionEndPoints();
        setupBlockEndPoints();
        setupNeighbourEndPoints();
        setupBalancesCacheEndPoints();
    }

    public void setupTransactionEndPoints() {
        get("/transactions", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson()
                            .toJsonTree(transactionRepository.getAllTransactions())));
        });

        post("/transactions", (request, response) -> {
            response.type("application/json");
            Transaction transaction = new Gson().fromJson(request.body(), Transaction.class);
            transactionRepository.createTransaction(transaction);
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        get("/transactions", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson()
                            .toJsonTree(transactionRepository.getAllTransactions())));
        });

        get("/transactions/:hash", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson()
                            .toJsonTree(transactionRepository.getTransaction(request.params(":hash")))));
        });
    }

    public void setupBlockEndPoints() {
        get("/blocks", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson()
                            .toJsonTree(blockRepository.getAllBlocks())));
        });

        post("/blocks", (request, response) -> {
            response.type("application/json");
            Block block = new Gson().fromJson(request.body(), Block.class);
            blockRepository.createBlock(block);
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        get("/blocks/:hash", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson()
                            .toJsonTree(blockRepository.getBlock(request.params(":hash")))));
        });
    }

    public void setupNeighbourEndPoints() {
        get("/neighbours", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson()
                            .toJsonTree(neighboursRepository.getAllNeighbours())));
        });
    }

    public void setupBalancesCacheEndPoints() {
        get("/balances", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson()
                            .toJsonTree(balancesCacheRepository.getAllBalancesInCache())));
        });
    }
}

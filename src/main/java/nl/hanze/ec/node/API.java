package nl.hanze.ec.node;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.responses.StandardResponse;
import nl.hanze.ec.node.responses.StatusResponse;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class API implements Runnable {
    private final NeighboursRepository neighboursRepository;
    private final BalancesCacheRepository balancesCacheRepository;
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;

    @Inject
    public API (Provider<NeighboursRepository> neighboursRepositoryProvider,
                Provider<BalancesCacheRepository> balancesCacheRepositoryProvider,
                Provider<BlockRepository> blockRepositoryProvider,
                Provider<TransactionRepository> transactionRepositoryProvider) {
        this.neighboursRepository = neighboursRepositoryProvider.get();
        this.balancesCacheRepository = balancesCacheRepositoryProvider.get();
        this.blockRepository = blockRepositoryProvider.get();
        this.transactionRepository = transactionRepositoryProvider.get();
    }

    public void run() {
        APISetup();
        setupTransactionEndPoints();
        setupBlockEndPoints();
        setupNeighbourEndPoints();
        setupBalancesCacheEndPoints();
    }

    public void APISetup() {
        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET");
        });
    }

    public void setupTransactionEndPoints() {
        post("/transactions", (request, response) -> {
            response.type("application/json");
            Transaction transaction = new Gson().fromJson(request.body(), Transaction.class);
            transactionRepository.createTransaction(transaction);
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        get("/transactions", (request, response) -> {
            response.type("application/json");

            List<Transaction> transactions = new ArrayList<>();
            if (request.queryParams().size() == 0) {
                transactions = transactionRepository.getAllTransactions();
            }
            else {
                String parameter = (String) request.queryParams().toArray()[0];
                String parameterValue = request.queryParamsValues(parameter)[0];

                if (parameter.equals("hash")) {
                    return new Gson().toJson(
                            new StandardResponse(StatusResponse.SUCCESS,
                                    new Gson().toJsonTree(transactionRepository.getTransaction(parameterValue)))
                    );
                }
                else if (parameter.equals("from") || parameter.equals("to")) {
                    transactions = transactionRepository.getTransactionsByAddress(parameterValue);
                }
            }
            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(transactions))
            );
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

package nl.hanze.ec.node;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import nl.hanze.ec.node.exceptions.InvalidTransaction;
import nl.hanze.ec.node.responses.StandardResponse;
import nl.hanze.ec.node.responses.StatusResponse;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.database.repositories.NeighboursRepository;
import nl.hanze.ec.node.database.repositories.TransactionRepository;
import nl.hanze.ec.node.utils.SignatureUtils;
import nl.hanze.ec.node.utils.ValidationUtils;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import spark.Route;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static spark.Spark.*;
import static spark.Spark.post;

public class API implements Runnable {
    private final NeighboursRepository neighboursRepository;
    private final BalancesCacheRepository balancesCacheRepository;
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final JSONParser parser = new JSONParser();

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
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH");
            response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
        });
    }

    public void postCORS(String path, Route route) {
        options(path, (request, response) -> response);
        post(path, route);
    }

    public void getCORS(String path, Route route) {
        options(path, (request, response) -> response);
        get(path, route);
    }

    public void setupTransactionEndPoints() {
        postCORS("/transactions", (request, response) -> {
            response.type("application/json");
            System.out.println(request.body());
            JSONObject transactionObject = (JSONObject) parser.parse(request.body());
            createTransaction(transactionObject);
            response.status(200);
            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        getCORS("/transactions", (request, response) -> {
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
        postCORS("/blocks", (request, response) -> {
            response.type("application/json");

            Block block = new Gson().fromJson(request.body(), Block.class);
            blockRepository.createBlock(block);

            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        get("/blocks", (request, response) -> {
            response.type("application/json");

            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson()
                            .toJsonTree(blockRepository.getAllBlocks())));
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
        postCORS("/stake", (request, response) -> {
            response.type("application/json");

            Block block = new Gson().fromJson(request.body(), Block.class);
            blockRepository.createBlock(block);

            return new Gson().toJson(new StandardResponse(StatusResponse.SUCCESS));
        });

        getCORS("/balances", (request, response) -> {
            response.type("application/json");

            float amount = 0;
            if (request.queryParams().size() == 0) {
                return new Gson().toJson(
                        new StandardResponse(StatusResponse.SUCCESS, new Gson()
                                .toJsonTree(balancesCacheRepository.getAllBalancesInCache())));
            }
            else {
                String parameter = (String) request.queryParams().toArray()[0];
                String parameterValue = request.queryParamsValues(parameter)[0];

                if (parameter.equals("stake")) {
                    amount = transactionRepository.getStake(parameterValue);
                }
                else if (parameter.equals("balance")) {
                    amount = transactionRepository.getBalance(parameterValue);
                }
            }

            return new Gson().toJson(
                    new StandardResponse(StatusResponse.SUCCESS, new Gson().toJsonTree(amount))
            );
        });
    }

    private void createTransaction(JSONObject transactionObject) {
        String from = (String) transactionObject.get("from");
        String to = (String) transactionObject.get("to");
        //float amount = Float.parseFloat((String) transactionObject.get("amount"));
        String signature = (String) transactionObject.get("signature");
        String publicKeyString = (String) transactionObject.get("public_key");
        DateTime transactionTimestamp = new DateTime((long) transactionObject.get("timestamp"));

        int amount = Integer.parseInt((String) transactionObject.get("amount"));

        String payload = from + to + transactionObject.get("timestamp") + amount;

        PublicKey publicKey = null;
        try {
            publicKey = SignatureUtils.decodeWalletPublicKey(publicKeyString);
            System.out.println(SignatureUtils.verify(publicKey, signature, payload));
            System.out.println(SignatureUtils.verify(publicKey, signature,  payload + "something-extra"));
            ValidationUtils.validateWalletTransaction(from, publicKeyString, publicKey, signature, payload);
        }
        catch (InvalidTransaction e) {
            e.printStackTrace();
        }

        String encodedPublicKey = SignatureUtils.encodePublicKey(publicKey);
        transactionRepository.createTransaction(null, from, to, amount, signature, "wallet", encodedPublicKey);
    }
}

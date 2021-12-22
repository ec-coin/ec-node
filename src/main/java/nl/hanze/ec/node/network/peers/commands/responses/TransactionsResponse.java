package nl.hanze.ec.node.network.peers.commands.responses;

import nl.hanze.ec.node.app.workers.Worker;
import nl.hanze.ec.node.app.workers.WorkerFactory;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.network.peers.commands.AbstractCommand;
import nl.hanze.ec.node.network.peers.commands.Command;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class TransactionsResponse extends AbstractCommand implements Response {
    public static class Tx {
        public final String hash;
        public final String from;
        public final String to;
        public final float amount;
        public final String signature;
        public final String status;
        public final String addressType;
        // public DateTime timestamp;

        public Tx(String hash, String from, String to, float amount, String signature, String status, String addressType) {
            this.hash = hash;
            this.from = from;
            this.to = to;
            this.amount = amount;
            this.signature = signature;
            this.status = status;
            this.addressType = addressType;
        }

        public Map<String, Object> toMap() {
            return new HashMap<>() {{
                put("hash", hash);
                put("from", from);
                put("to", to);
                put("amount", amount);
                put("signature", signature);
                put("status", status);
                put("addressType", addressType);
            }};
        }

        @Override
        public String toString() {
            return "Tx{" + "hash='" + hash + '\'' + ", from='" + from + '\'' + ", to='" + to + '\'' +
                    ", amount=" + amount + ", signature='" + signature + '\'' + ", status='" + status + '\'' +
                    ", addressType='" + addressType + '\'' + '}';
        }
    }

    List<Tx> transactions;
    int responseTo;

    public TransactionsResponse(Collection<Transaction> transactions, int responseTo) {
        this.transactions = transactions.stream()
                .map(tx -> new Tx(
                        tx.getHash(),
                        tx.getFrom(),
                        tx.getTo(),
                        tx.getAmount(),
                        tx.getSignature(),
                        tx.getStatus(),
                        tx.getAddressType()
                )).collect(Collectors.toList());

        this.responseTo = responseTo;
    }

    public TransactionsResponse(JSONObject payload, WorkerFactory workerFactory) {
        super(payload, workerFactory);

        this.responseTo = payload.getInt("responseTo");

        List<Object> jArray = payload.getJSONArray("transactions").toList();
        this.transactions = new ArrayList<>();
        for (Object obj : jArray) {
            if (obj instanceof HashMap) {
                HashMap<?, ?> tx = (HashMap<?, ?>) obj;

                if (tx.get("hash") instanceof String &&
                        tx.get("from") instanceof String &&
                        tx.get("to") instanceof String &&
                        (tx.get("amount") instanceof BigDecimal || tx.get("amount") instanceof Integer) &&
                        tx.get("signature") instanceof String &&
                        tx.get("status") instanceof String &&
                        tx.get("addressType") instanceof String) {
                    float amount;
                    if (tx.get("amount") instanceof Integer) {
                        amount = ((Integer) tx.get("amount")).floatValue();
                    } else {
                        amount = ((BigDecimal) tx.get("amount")).floatValue();
                    }

                    this.transactions.add(new Tx(
                            tx.get("hash").toString(),
                            tx.get("from").toString(),
                            tx.get("to").toString(),
                            amount,
                            tx.get("signature").toString(),
                            tx.get("status").toString(),
                            tx.get("addressType").toString()
                    ));
                }
            }
        }
    }

    @Override
    protected JSONObject getData(JSONObject payload) {
        payload.put("transactions", this.transactions.stream().map(Tx::toMap).collect(Collectors.toList()));
        payload.put("responseTo", this.responseTo);

        return payload;
    }

    public List<Tx> getTransactions() {
        return transactions;
    }

    @Override
    public String getCommandName() {
        return "tx-response";
    }

    @Override
    public Worker getWorker(Command receivedCommand, BlockingQueue<Command> peerCommandQueue) {
        return null;
    }

    @Override
    public Integer inResponseTo() {
        return this.responseTo;
    }
}

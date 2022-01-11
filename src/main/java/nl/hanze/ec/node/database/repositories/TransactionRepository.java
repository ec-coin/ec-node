package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.modules.annotations.TransactionDAO;
import nl.hanze.ec.node.network.peers.PeerPool;
import nl.hanze.ec.node.utils.HashingUtils;
import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionRepository {
    private final Dao<Transaction, String> transactionDAO;
    private final float transactionFee = 1;

    @Inject
    public TransactionRepository(@TransactionDAO Dao<Transaction, String> blockDAO) {
        this.transactionDAO = blockDAO;
    }

    public synchronized List<Transaction> getAllTransactions() {
        try {
            return transactionDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized Transaction createTransaction(Block block, String from, String to, float amount, String signature, String addressType, String publicKey) {
        String hash = HashingUtils.generateTransactionHash(from, to, transactionFee, signature);
        return createTransaction(new Transaction(hash, block, from, to, amount, signature, "pending", addressType, publicKey));
    }

    public synchronized Transaction createTransaction(String hash, Block block, String from, String to, float amount, String signature, String status, String addressType, String publicKey, DateTime dateTime) {
        return createTransaction(new Transaction(hash, block, from, to, amount, signature, status, addressType, publicKey, dateTime));
    }

    public synchronized Transaction createTransaction(Transaction transaction) {
        try {
            transactionDAO.createOrUpdate(transaction);
            return transaction;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized float getStake(String address) {
        return getAmount(address, "node");
    }

    public synchronized float getBalance(String address) {
        return getAmount(address, "wallet");
    }

    public synchronized float getAmount(String address, String address_type) {
        int amount = 0;
        try {
            Where<Transaction, String> where = transactionDAO.queryBuilder()
                .where().eq("status", "validated")
                .and().eq("from", address)
                .or().eq("to", address);

            List<Transaction> transactions = where.and().eq("address_type", address_type).query();

            for(Transaction transaction : transactions) {
                if (transaction.getFrom().equals(transaction.getTo())) {
                    continue;
                }

                if (transaction.getFrom().equals(address)) {
                    amount -= transaction.getAmount();
                } else {
                    amount += transaction.getAmount();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return amount;
    }

    public synchronized List<String> getAllNodeAddresses() {
        Set<String> addresses = new HashSet<>();
        try {
            List<Transaction> query = transactionDAO.queryBuilder()
                    .where().eq("address_type", "node").query();

            for (Transaction transaction : query) {
                addresses.add(transaction.getFrom());
                addresses.add(transaction.getTo());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(addresses);
    }

    public synchronized boolean transactionThresholdReached() {
        try {
            int numberOfPendingTransactions = transactionDAO.queryBuilder()
                    .where().eq("status", "pending").query().size();

            if (numberOfPendingTransactions >= PeerPool.getTransactionThreshold()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public synchronized List<Transaction> getFiniteNumberOfPendingTransactions() {
        List<Transaction> pendingTransactions = new ArrayList<>();
        try {
            pendingTransactions = transactionDAO.queryBuilder()
                    .limit((long) PeerPool.getTransactionThreshold())
                    .where().eq("status", "pending")
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pendingTransactions;
    }

    public synchronized void setTransactionAsValidated(Transaction t, Block block) {
        try {
            transactionDAO.createOrUpdate(
                    new Transaction(t.getHash(), block, t.getFrom(), t.getTo(), t.getAmount(), t.getSignature(), "validated", t.getAddressType(), t.getPublicKey(), t.getTimestamp())
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<String> getAllValidatingNodes() {
        Set<String> addresses = new HashSet<>();
        try {
            List<Transaction> transactions = transactionDAO.queryBuilder()
                    .where().eq("to", "-1").query();

            for (Transaction transaction : transactions) {
                addresses.add(transaction.getFrom());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(addresses);
    }

    public synchronized void addNodeAsValidatingNode(Block block, String from, String signature, String publicKey) {
        String to = "-1";
        try {
            String hash = HashingUtils.generateTransactionHash(from, to, transactionFee, signature);
            Transaction transactionWithFee = new Transaction(hash, block, from, to, transactionFee, signature, "validated", "node", publicKey);
            transactionDAO.createOrUpdate(transactionWithFee);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Transaction getTransaction(String hash) {
        Transaction transaction = null;
        try {
            List<Transaction> query = transactionDAO.queryBuilder()
                    .where().eq("hash", hash).query();

            if (query != null && query.size() > 0) {
                transaction = query.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transaction;
    }

    public synchronized List<Transaction> getTransactionsByAddress(String address) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            transactions = transactionDAO.queryBuilder()
                    .where().eq("from", address)
                    .or().eq("to", address).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }
}

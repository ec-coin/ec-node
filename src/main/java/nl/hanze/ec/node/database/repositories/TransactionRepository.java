package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.modules.annotations.TransactionDAO;
import nl.hanze.ec.node.network.peers.PeerPool;

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

    public synchronized void createTransaction(String hash, Block block, String from, String to, float amount, String signature, String addressType) {
        try {
            Transaction transaction = new Transaction(hash, block, from, to, amount, signature, "pending", addressType);
            transactionDAO.createOrUpdate(transaction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void createTransaction(Transaction transaction) {
        try {
            transactionDAO.createOrUpdate(transaction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized int getStake(String address) {
        return getAmount(address, "node");
    }

    public synchronized int getBalance(String address) {
        return getAmount(address, "wallet");
    }

    public synchronized int getAmount(String address, String address_type) {
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
        List<Transaction> transactionsToBeValidated = new ArrayList<>();
        try {
            List<Transaction> pendingTransactions = transactionDAO.queryBuilder()
                    .where().eq("status", "pending").query();

            int threshold = PeerPool.getTransactionThreshold();
            for (int i = 0; i < threshold; i++) {
                transactionsToBeValidated.add(pendingTransactions.get(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactionsToBeValidated;
    }

    public synchronized void setTransactionAsValidated(Transaction t, Block block) {
        try {
            transactionDAO.createOrUpdate(
                    new Transaction(t.getHash(), block, t.getFrom(), t.getTo(), t.getAmount(), t.getSignature(), "validated", t.getAddressType())
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

    public synchronized void addNodeAsValidatingNode(String hash, Block block, String from, String signature) {
        try {
            Transaction transactionWithFee = new Transaction(hash, block, from, "-1", transactionFee, signature, "validated", "node");
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

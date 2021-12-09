package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
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

    public List<Transaction> getAllTransactions() {
        try {
            return transactionDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void createTransaction(String hash, Block block, String from, String to, float amount, String signature) {
        try {
            Transaction transaction = new Transaction(hash, block, from, to, amount, signature, "pending");
            transactionDAO.createOrUpdate(transaction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getBalance(String address) {
        int balance = 0;
        try {
            List<Transaction> query = transactionDAO.queryBuilder()
                .where().eq("status", "validated")
                .and().eq("from", address)
                .or().eq("to", address).query();

            for(Transaction transaction : query) {
                if (transaction.getFrom().equals(transaction.getTo())) {
                    continue;
                }

                if (transaction.getFrom().equals(address)) {
                    balance -= transaction.getAmount();
                } else {
                    balance += transaction.getAmount();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return balance;
    }

    public List<String> getAllAddresses() {
        Set<String> addresses = new HashSet<>();
        try {
            List<Transaction> query = transactionDAO.queryBuilder().query();

            for (Transaction transaction : query) {
                addresses.add(transaction.getFrom());
                addresses.add(transaction.getTo());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(addresses);
    }

    public boolean transactionThresholdReached() {
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

    public List<Transaction> getFiniteNumberOfPendingTransactions() {
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

    public void setTransactionAsValidated(Transaction t, Block block) {
        try {
            transactionDAO.createOrUpdate(
                    new Transaction(t.getHash(), block, t.getFrom(), t.getTo(), t.getAmount(), t.getSignature(), "validated")
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllValidatingNodes() {
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

    public void addNodeAsValidatingNode(String hash, Block block, String from, String signature) {
        try {
            Transaction transactionWithFee = new Transaction(hash, block, from, "-1", transactionFee, signature, "validated");
            transactionDAO.createOrUpdate(transactionWithFee);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
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
    private final BlockRepository blockRepository;

    @Inject
    public TransactionRepository(
            @TransactionDAO Dao<Transaction, String> transactionDAO,
            BlockRepository blockRepository
            ) {
        this.transactionDAO = transactionDAO;
        this.blockRepository = blockRepository;
    }

    public synchronized List<Transaction> getAllTransactions() {
        try {
            return transactionDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized List<Transaction> getAllPendingTransactions() {
        try {
            return transactionDAO.queryBuilder().where().eq("status", "pending").query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public synchronized Transaction createTransaction(Block block, String from, String to, float amount, String signature, String addressType, String publicKey, DateTime dateTime) {
        String hash = HashingUtils.generateTransactionHash(from, to, amount, signature);
        return createTransaction(new Transaction(hash, block, from, to, amount, signature, "pending", addressType, publicKey, dateTime));
    }

    public synchronized Transaction createTransaction(String hash, Block block, String from, String to, float amount, String signature, String status, String addressType, String publicKey, DateTime dateTime) {
        return createTransaction(new Transaction(hash, block, from, to, amount, signature, status, addressType, publicKey, dateTime));
    }

    public synchronized Transaction createTransaction(Transaction transaction) {
        //System.out.println("Create transaction: " + transaction.toString());
        try {
            transactionDAO.createOrUpdate(transaction);
            return transaction;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized List<Transaction> getNumberOfTransactions(long offset, long limit) {
        try {
            return transactionDAO.queryBuilder().offset(offset).limit(limit).orderBy("timestamp", false).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized List<Transaction> getNumberOfPendingTransactions(long offset, long limit) {
        try {
            return transactionDAO.queryBuilder().offset(offset).limit(limit).orderBy("timestamp", false)
                    .where().eq("status", "pending").query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized float getBalance(String address) {
        int amount = 0;
        try {
            Where<Transaction, String> where = transactionDAO.queryBuilder()
                    .where().eq("from", address)
                    .or().eq("to", address)
                    .and().eq("status", "validated");
            List<Transaction> transactions = where.query();

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

    public synchronized List<String> getStakeAddresses() {
        Set<String> addresses = new HashSet<>();
        try {
            List<Transaction> query = transactionDAO.queryBuilder()
                    .where().eq("to", "stake_register")
                    .query();

            for (Transaction transaction : query) {
                addresses.add(transaction.getFrom());
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

    public synchronized List<Transaction> getPendingTransactions() {
        List<Transaction> pendingTransactions = new ArrayList<>();
        try {
            pendingTransactions = transactionDAO.queryBuilder()
                    .where().eq("status", "pending")
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pendingTransactions;
    }

    public synchronized List<Transaction> getValidatedTransactions() {
        List<Transaction> validatedTransactions = new ArrayList<>();
        try {
            validatedTransactions = transactionDAO.queryBuilder()
                    .where().eq("status", "validated")
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return validatedTransactions;
    }

    public synchronized void update(Transaction t) {
        //System.out.println("Update transaction: " + t.toString());
        try {
            transactionDAO.update(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Transaction createOrUpdate(Transaction t) {
        //System.out.println("Create or update transaction: " + t.toString());
        try {
            transactionDAO.createOrUpdate(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return t;
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

    public synchronized boolean stakeNodeIsInRegistry(String from) {
        try {
            List<Transaction> transactions = transactionDAO.queryBuilder()
                    .where().eq("from", from)
                    .and().eq("to", "stake_register")
                    .query();
            if (transactions.size() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized void addStakeNodeToRegistry(String from, String signature, String publicKey) {
        String to = "stake_register";
        Block genesisBlock = blockRepository.getBlock(0);
        try {
            String hash = HashingUtils.generateTransactionHash(from, to, 0, signature);
            Transaction transaction = new Transaction(hash, genesisBlock, from, to, 0, signature, "validated", "node", publicKey);
            transactionDAO.createOrUpdate(transaction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import nl.hanze.ec.node.database.models.Block;
import nl.hanze.ec.node.database.models.Transaction;
import nl.hanze.ec.node.modules.annotations.TransactionDAO;

import java.sql.SQLException;
import java.util.List;

public class TransactionRepository {
    private final Dao<Transaction, String> transactionDAO;

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

    public void createTransaction(String hash, Block block, String from, String to, int size, String signature, String status) {
        try {
            Transaction transaction = new Transaction(hash, block, from, to, size, signature, status);
            transactionDAO.createOrUpdate(transaction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

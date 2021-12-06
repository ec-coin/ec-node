package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import nl.hanze.ec.node.database.models.PendingTransaction;
import nl.hanze.ec.node.modules.annotations.PendingTransactionDAO;

import java.sql.SQLException;
import java.util.List;

public class PendingTransactionRepository {
    private final Dao<PendingTransaction, String> pendingTransactionDAO;

    @Inject
    public PendingTransactionRepository(@PendingTransactionDAO Dao<PendingTransaction, String> pendingTransactionDAO) {
        this.pendingTransactionDAO = pendingTransactionDAO;
    }

    public List<PendingTransaction> getAllPendingTransactions() {
        try {
            return pendingTransactionDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void createPendingTransaction(String hash, String from, String to, int size, String signature) {
        try {
            PendingTransaction pendingTransaction = new PendingTransaction(hash, from, to, size, signature);
            pendingTransactionDAO.createOrUpdate(pendingTransaction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

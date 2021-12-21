package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import nl.hanze.ec.node.database.models.BalancesCache;
import nl.hanze.ec.node.modules.annotations.BalancesCacheDAO;
import java.sql.SQLException;
import java.util.List;

public class BalancesCacheRepository {
    private final Dao<BalancesCache, String> balancesCacheDAO;

    @Inject
    public BalancesCacheRepository(@BalancesCacheDAO Dao<BalancesCache, String> balancesCacheDAO) {
        this.balancesCacheDAO = balancesCacheDAO;
    }

    public synchronized List<BalancesCache> getAllBalancesInCache() {
        try {
            return balancesCacheDAO.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized Boolean hasValidBalance(
        String hash,
        float amount
    ) {
        try {
            List<BalancesCache> balances = balancesCacheDAO.queryBuilder().where().eq("address", hash).query();
            if (balances.get(0).getBalance() > amount) {
                return true;
            };
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public synchronized void updateBalanceCache(String address, float balance) {
        try {
            BalancesCache balancesCache = new BalancesCache(address, balance);
            balancesCacheDAO.createOrUpdate(balancesCache);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

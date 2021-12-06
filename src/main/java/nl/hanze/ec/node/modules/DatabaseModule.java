package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import com.google.inject.TypeLiteral;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import nl.hanze.ec.node.database.models.*;
import nl.hanze.ec.node.modules.annotations.*;

import java.sql.SQLException;

public class DatabaseModule extends AbstractModule {
    private ConnectionSource connectionSource;

    public DatabaseModule() {
        super();

        try {
            String databaseUrl = "jdbc:sqlite:database.db";
            connectionSource = new JdbcConnectionSource(databaseUrl);

            TableUtils.createTableIfNotExists(connectionSource, Neighbour.class);
            TableUtils.createTableIfNotExists(connectionSource, Block.class);
            TableUtils.createTableIfNotExists(connectionSource, Transaction.class);
            TableUtils.createTableIfNotExists(connectionSource, BalancesCache.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Provides
    @DatabaseConnection
    ConnectionSource providesDatabaseConnection() throws SQLException {
       return connectionSource;
    }

    protected void configure() {
        try {
            // we need to define a TypeLiteral because you cannot bind to interface with generics without it
            TypeLiteral<Dao<Neighbour, String>> neighbourDAOType = new TypeLiteral<>() {};
            TypeLiteral<Dao<BalancesCache, String>> balancesCacheDAOType = new TypeLiteral<>() {};
            TypeLiteral<Dao<Block, String>> blockDAOType = new TypeLiteral<>() {};
            TypeLiteral<Dao<Transaction, String>> transactionDAOType = new TypeLiteral<>() {};

            bind(neighbourDAOType).annotatedWith(NeighbourDAO.class).toInstance(DaoManager.createDao(connectionSource, Neighbour.class));
            bind(balancesCacheDAOType).annotatedWith(BalancesCacheDAO.class).toInstance(DaoManager.createDao(connectionSource, BalancesCache.class));
            bind(blockDAOType).annotatedWith(BlockDAO.class).toInstance(DaoManager.createDao(connectionSource, Block.class));
            bind(transactionDAOType).annotatedWith(TransactionDAO.class).toInstance(DaoManager.createDao(connectionSource, Transaction.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

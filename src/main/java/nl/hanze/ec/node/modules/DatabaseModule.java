package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import com.google.inject.TypeLiteral;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import nl.hanze.ec.node.database.models.Neighbour;
import nl.hanze.ec.node.modules.annotations.DatabaseConnection;
import nl.hanze.ec.node.modules.annotations.NeighbourDAO;

import java.sql.SQLException;

public class DatabaseModule extends AbstractModule {
    private ConnectionSource connectionSource;

    public DatabaseModule() {
        super();

        try {
            String databaseUrl = "jdbc:sqlite:database.db";
            connectionSource = new JdbcConnectionSource(databaseUrl);
            TableUtils.createTableIfNotExists(connectionSource, Neighbour.class);
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
            bind(neighbourDAOType).annotatedWith(NeighbourDAO.class).toInstance(DaoManager.createDao(connectionSource, Neighbour.class));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

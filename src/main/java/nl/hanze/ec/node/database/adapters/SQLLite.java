package nl.hanze.ec.node.database.adapters;

import nl.hanze.ec.node.network.ConnectionManager;
import nl.hanze.ec.node.utils.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLLite implements Database {

    private static final Logger logger = LogManager.getLogger(SQLLite.class);
    private Connection connection;

    public SQLLite() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
            createScheme();
            logger.info("Database scheme created");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createScheme() throws SQLException {
        String schemeSQL = FileUtils.readFromResources("database_scheme.sql");
        Statement statement = connection.createStatement();
        statement.execute(schemeSQL);
    }
}

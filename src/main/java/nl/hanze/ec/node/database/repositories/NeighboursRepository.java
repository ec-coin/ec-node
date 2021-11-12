package nl.hanze.ec.node.database.repositories;

import com.google.inject.Inject;
import nl.hanze.ec.node.database.adapters.Database;
import nl.hanze.ec.node.modules.annotations.DatabaseConnection;

public class NeighboursRepository {

    private Database database;

    @Inject
    public NeighboursRepository(@DatabaseConnection Database databaseConnection) {
        this.database = databaseConnection;
    }

    public void test() {

    }
}

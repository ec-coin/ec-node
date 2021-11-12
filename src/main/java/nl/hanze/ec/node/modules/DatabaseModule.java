package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import nl.hanze.ec.node.database.adapters.Database;
import nl.hanze.ec.node.database.adapters.SQLLite;
import nl.hanze.ec.node.database.repositories.BlockRepository;
import nl.hanze.ec.node.modules.annotations.DatabaseConnection;

public class DatabaseModule extends AbstractModule {
    @Provides
    @Singleton
    @DatabaseConnection
    Database providesDatabaseConnection() {
        return new SQLLite();
    }
}

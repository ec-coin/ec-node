package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.hanze.ec.node.modules.annotations.DbSeeding;
import nl.hanze.ec.node.modules.annotations.MaxPeers;
import nl.hanze.ec.node.modules.annotations.MinPeers;
import nl.hanze.ec.node.modules.annotations.Port;

public class ConfigModule extends AbstractModule  {
    private final int minPeers;
    private final int maxPeers;
    private final int port;
    private final boolean dbSeeding;

    public ConfigModule(Namespace ns) {
        this.minPeers = ns.getInt("min-peers");
        this.maxPeers = ns.getInt("max-peers");
        this.port = ns.getInt("port");
        this.dbSeeding = ns.getBoolean("db-seeding");
    }

    protected void configure() {
        bind(Integer.class).annotatedWith(Port.class).toInstance(this.port);
        bind(Integer.class).annotatedWith(MaxPeers.class).toInstance(this.maxPeers);
        bind(Integer.class).annotatedWith(MinPeers.class).toInstance(this.minPeers);
        bind(Boolean.class).annotatedWith(DbSeeding.class).toInstance(this.dbSeeding);
    }
}

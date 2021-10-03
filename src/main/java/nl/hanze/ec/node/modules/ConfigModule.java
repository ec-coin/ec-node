package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import net.sourceforge.argparse4j.inf.Namespace;

public class ConfigModule extends AbstractModule  {
    private final int maxPeers;
    private final int port;

    public ConfigModule(Namespace ns) {
        this.maxPeers = ns.getInt("max-peers");
        this.port = ns.getInt("port");
    }

    protected void configure() {
        bind(Integer.class).annotatedWith(Names.named("Port")).toInstance(this.port);
        bind(Integer.class).annotatedWith(Names.named("MaxPeers")).toInstance(this.maxPeers);
    }
}

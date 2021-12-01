package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.hanze.ec.node.modules.annotations.MaxPeers;
import nl.hanze.ec.node.modules.annotations.Port;

public class ConfigModule extends AbstractModule  {
    private final int maxPeers;
    private final int port;

    public ConfigModule(Namespace ns) {
        this.maxPeers = ns.getInt("max-peers");
        this.port = ns.getInt("port");
    }

    protected void configure() {
        bind(Integer.class).annotatedWith(Port.class).toInstance(this.port);
        bind(Integer.class).annotatedWith(MaxPeers.class).toInstance(this.maxPeers);
    }
}

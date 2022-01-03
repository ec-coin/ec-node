package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.hanze.ec.node.modules.annotations.*;
import nl.hanze.ec.node.utils.HashingUtils;
import nl.hanze.ec.node.utils.SignatureUtils;
import java.security.KeyPair;

public class ConfigModule extends AbstractModule {
    private final KeyPair keyPair;
    private final String address;
    private final int minPeers;
    private final int maxPeers;
    private final int port;
    private final boolean dbSeeding;

    public ConfigModule(Namespace ns) {
        this.keyPair = SignatureUtils.generateKeyPair();
        this.address = HashingUtils.getAddress(keyPair.getPublic());
        this.minPeers = ns.getInt("min-peers");
        this.maxPeers = ns.getInt("max-peers");
        this.port = ns.getInt("port");
        this.dbSeeding = ns.getBoolean("db-seeding");
    }

    protected void configure() {
        bind(String.class).annotatedWith(NodeAddress.class).toInstance(this.address);
        bind(Integer.class).annotatedWith(Port.class).toInstance(this.port);
        bind(Integer.class).annotatedWith(MaxPeers.class).toInstance(this.maxPeers);
        bind(Integer.class).annotatedWith(MinPeers.class).toInstance(this.minPeers);
        bind(Boolean.class).annotatedWith(DbSeeding.class).toInstance(this.dbSeeding);
    }

    @Provides
    @Singleton
    @NodeKeyPair
    KeyPair providesNodeKeyPair() {
        return this.keyPair;
    }
}

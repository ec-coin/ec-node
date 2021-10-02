package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import net.sourceforge.argparse4j.inf.Namespace;

public class ConfigModule extends AbstractModule  {
    private final Namespace namespace;

    public ConfigModule(Namespace ns) {
        this.namespace = ns;
    }

    @Provides
    public Namespace providesNamespace() {
        return this.namespace;
    }
}

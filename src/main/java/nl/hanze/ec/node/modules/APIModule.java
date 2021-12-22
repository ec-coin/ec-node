package nl.hanze.ec.node.modules;

import com.google.inject.AbstractModule;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class APIModule extends AbstractModule {

    public APIModule() { }

    protected void configure() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
    }
}

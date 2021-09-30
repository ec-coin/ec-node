package nl.hanze.ec.node.runner;

import com.google.inject.Guice;
import com.google.inject.Injector;
import nl.hanze.ec.node.Application;

/**
 * Hanzehogeschool Groningen University of Applied Sciences HBO-ICT
 * Project monetair systeem
 * EC Network Node
 *
 * @author Roy Voetman
 * @author Dylan Hiemstra
 * @author Wouter Folkertsma
 * @author Jordi Mellema
 *
 * @version 1.0
 */
public class ECNetworkNodeRunner {
    public static void main(String[] args) {
        Injector injection = Guice.createInjector();
        injection.getInstance(Application.class).run();
    }
}

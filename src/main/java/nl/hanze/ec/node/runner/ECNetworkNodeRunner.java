package nl.hanze.ec.node.runner;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.modules.ConfigModule;

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
        ArgumentParser parser = ArgumentParsers.newFor("ECNode").build()
                .defaultHelp(true)
                .description("ECNode is the core product of EC Blockchain");

        parser.addArgument("-p", "--port")
                .type(Integer.class)
                .setDefault(5000)
                .help("Server port");

        parser.addArgument("--max-peers")
                .type(Integer.class)
                .dest("max-peers")
                .setDefault(10)
                .help("Maximum peers to connect to");


        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        //################################
        //  Launch the application
        //################################
        Injector injection = Guice.createInjector(
                new ConfigModule(ns)
        );
        injection.getInstance(Application.class).run();
    }
}

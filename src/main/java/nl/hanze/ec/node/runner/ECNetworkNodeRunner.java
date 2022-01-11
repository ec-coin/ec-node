package nl.hanze.ec.node.runner;

import com.google.inject.Guice;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.hanze.ec.node.Application;
import nl.hanze.ec.node.modules.*;

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
        //################################
        //  Define command line arguments
        //################################
        ArgumentParser parser = ArgumentParsers.newFor("ECNode").build()
                .defaultHelp(true)
                .description("ECNode is the core product of EC Blockchain");

        parser.addArgument("-p", "--port")
                .type(Integer.class)
                .setDefault(5000)
                .help("Server port");

        parser.addArgument("--min-peers")
                .type(Integer.class)
                .dest("min-peers")
                .setDefault(4)
                .help("Minimum peers to connect to");

        parser.addArgument("--max-peers")
                .type(Integer.class)
                .dest("max-peers")
                .setDefault(50)
                .help("Maximum peers to connect to");

        parser.addArgument("--debug-db-seeding")
                .type(Boolean.class)
                .dest("db-seeding")
                .setDefault(false)
                .help("Seed blockchain with mock data for debug purposes");

        //################################
        //  Parse command line arguments
        //################################
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        //################################
        //  Create IoC Container and launch application
        //################################
        Guice.createInjector(
                new ConfigModule(ns),
                new ThreadCommunicationModule(),
                new DatabaseModule(ns.getBoolean("db-seeding"))
        ).getInstance(Application.class).run();
    }
}

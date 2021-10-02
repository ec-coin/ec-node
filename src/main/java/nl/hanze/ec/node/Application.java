package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.network.Message;
import nl.hanze.ec.node.utils.FileUtils;

public class Application {
    public void run() {
        printWelcome();
    }

    private void printWelcome() {
        System.out.println(FileUtils.readFromResources("welcome.txt"));
    }
}

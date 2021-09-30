package nl.hanze.ec.node;

import com.google.inject.Inject;
import nl.hanze.ec.node.network.Message;

public class Application {
    @Inject
    Application(Message message) {
        System.out.println(message.t);
    }

    public String run() {
        System.out.println("test");
        return "Hello world!";
    }
}

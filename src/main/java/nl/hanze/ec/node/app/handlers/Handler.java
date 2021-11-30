package nl.hanze.ec.node.app.handlers;

import nl.hanze.ec.node.app.listeners.Listener;

public interface Handler extends Runnable {
    void run();

    void addObserver(Listener listener);

    void removeObserver(Listener listener);
}

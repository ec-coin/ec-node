package nl.hanze.ec.node.network.peers.commands.announcements;

import nl.hanze.ec.node.network.peers.commands.Command;

public interface Announcement extends Command {
    public boolean isValidated();

    public void setValidated(boolean validated);
}

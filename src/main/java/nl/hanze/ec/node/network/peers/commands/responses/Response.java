package nl.hanze.ec.node.network.peers.commands.responses;

import nl.hanze.ec.node.network.peers.commands.Command;

public interface Response extends Command {
    public Integer inResponseTo();
}

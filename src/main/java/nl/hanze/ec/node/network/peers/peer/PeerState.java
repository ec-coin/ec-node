package nl.hanze.ec.node.network.peers.peer;

public enum PeerState {
    CLOSED,
    CONNECTING,
    VERSION_RCVD,
    VERSION_ACK,
    ESTABLISHED,
    CLOSING
}

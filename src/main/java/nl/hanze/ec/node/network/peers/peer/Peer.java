package nl.hanze.ec.node.network.peers.peer;

import java.util.concurrent.atomic.AtomicReference;

public class Peer {
    private final String ip;
    private final int port;
    private final AtomicReference<PeerState> state;
    private double version;

    public Peer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.state = new AtomicReference<>(PeerState.UNCONNECTED);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public PeerState getState() {
        return state.get();
    }

    public void setState(PeerState state) {
        this.state.set(state);
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Peer{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", state=" + getState() +
                '}';
    }
}

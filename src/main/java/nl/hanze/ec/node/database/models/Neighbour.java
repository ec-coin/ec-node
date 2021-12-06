package nl.hanze.ec.node.database.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;

@DatabaseTable(tableName = "neighbours")
public class Neighbour {
    @DatabaseField(canBeNull = false, id = true, columnName = "ip")
    private String ip;

    @DatabaseField(canBeNull = false, columnName = "port")
    private int port;

    @DatabaseField(canBeNull = false, columnName = "last_connected_at")
    private DateTime lastConnectedAt;

    @DatabaseField(defaultValue = "true", columnName = "trustable")
    private boolean trustable;

    // ORMLite requires a no-arg constructor.
    public Neighbour() {}

    public Neighbour(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.lastConnectedAt = new DateTime();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public DateTime getLastConnectedAt() {
        return lastConnectedAt;
    }

    public boolean isTrustable() {
        return trustable;
    }
}

package nl.hanze.ec.node.database.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;

@DatabaseTable(tableName = "neighbours")
public class Neighbour {
    @DatabaseField(canBeNull = false, id = true)
    private String ip;

    @DatabaseField(canBeNull = false)
    private int port;

    @DatabaseField(canBeNull = false)
    private DateTime lastConnectedAt;

    @DatabaseField(defaultValue = "true")
    private boolean trustable;

    public Neighbour() {} // is needed

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

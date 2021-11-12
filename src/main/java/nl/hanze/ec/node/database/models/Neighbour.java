package nl.hanze.ec.node.database.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;

@DatabaseTable(tableName = "neighbours")
public class Neighbour {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String ip;

    @DatabaseField(canBeNull = false)
    private int port;

    @DatabaseField(canBeNull = false)
    private DateTime discoveredAt;

    @DatabaseField(canBeNull = false)
    private DateTime lastConnectedAt;

    @DatabaseField(defaultValue = "true")
    private boolean trustable;
}

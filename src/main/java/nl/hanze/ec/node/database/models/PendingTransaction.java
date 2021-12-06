package nl.hanze.ec.node.database.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;

@DatabaseTable(tableName = "pending_transactions")
public class PendingTransaction {
    @DatabaseField(canBeNull = false, id = true, columnName = "hash")
    private String hash;

    @DatabaseField(canBeNull = false, columnName = "from")
    private String from;

    @DatabaseField(canBeNull = false, columnName = "to")
    private String to;

    @DatabaseField(canBeNull = false, columnName = "size")
    private int size;

    @DatabaseField(canBeNull = false, columnName = "signature")
    private String signature;

    @DatabaseField(canBeNull = false, columnName = "timestamp")
    private DateTime timestamp;

    // ORMLite requires a no-arg constructor.
    public PendingTransaction() {}

    public PendingTransaction(String hash, String from, String to, int size, String signature) {
        this.hash = hash;
        this.from = from;
        this.to = to;
        this.size = size;
        this.signature = signature;
        this.timestamp = new DateTime();
    }

    public String getHash() {
        return hash;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getSize() {
        return size;
    }

    public String getSignature() {
        return signature;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }
}

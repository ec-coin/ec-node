package nl.hanze.ec.node.database.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;

@DatabaseTable(tableName = "transactions")
public class Transaction {
    @DatabaseField(canBeNull = false, id = true, columnName = "hash")
    private String hash;

    @DatabaseField(canBeNull = false, columnName = "block_hash", foreign = true)
    private Block block;

    @DatabaseField(canBeNull = false, columnName = "from")
    private String from;

    @DatabaseField(canBeNull = false, columnName = "to")
    private String to;

    @DatabaseField(canBeNull = false, columnName = "size")
    private int size;

    @DatabaseField(canBeNull = false, columnName = "signature")
    private String signature;

    @DatabaseField(canBeNull = false, columnName = "status")
    private String status;

    @DatabaseField(canBeNull = false, columnName = "timestamp")
    private DateTime timestamp;

    // ORMLite requires a no-arg constructor.
    public Transaction() {}

    public Transaction(String hash, Block block, String from, String to, int size, String signature, String status) {
        this.hash = hash;
        this.block = block;
        this.from = from;
        this.to = to;
        this.size = size;
        this.signature = signature;
        this.status = status;
        this.timestamp = new DateTime();
    }

    public String getHash() {
        return hash;
    }

    public String getBlockHash() {
        return this.block.getHash();
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

    public String getStatus() {
        return status;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }
}

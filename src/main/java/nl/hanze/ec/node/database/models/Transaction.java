package nl.hanze.ec.node.database.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nl.hanze.ec.node.database.repositories.BalancesCacheRepository;
import org.joda.time.DateTime;

@DatabaseTable(tableName = "transactions")
public class Transaction {
    @DatabaseField(canBeNull = false, id = true, columnName = "hash")
    private String hash;

    @DatabaseField(canBeNull = true, columnName = "block_hash", foreign = true)
    private Block block;

    @DatabaseField(canBeNull = false, columnName = "from")
    private String from;

    @DatabaseField(canBeNull = false, columnName = "to")
    private String to;

    @DatabaseField(canBeNull = false, columnName = "amount")
    private float amount;

    @DatabaseField(canBeNull = false, columnName = "signature")
    private String signature;

    @DatabaseField(canBeNull = false, columnName = "status")
    private String status;

    @DatabaseField(canBeNull = false, columnName = "timestamp")
    private DateTime timestamp;

    @DatabaseField(canBeNull = false, columnName = "address_type")
    private String addressType;

    @DatabaseField(canBeNull = false, columnName = "public_key")
    private String publicKey;

    // ORMLite requires a no-arg constructor.
    public Transaction() {}

    public Transaction(
            String hash,
            Block block,
            String from,
            String to,
            float amount,
            String signature,
            String status,
            String addressType,
            String publicKey,
            DateTime... dateTime
    ) {
        this.hash = hash;
        this.block = block;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.signature = signature;
        this.status = status;
        this.addressType = addressType;
        this.publicKey = publicKey;
        this.timestamp = (dateTime.length == 1) ? dateTime[0] : new DateTime();
    }

    public String getHash() {
        return this.hash;
    }

    public String getBlockHash() {
        return this.block.getHash();
    }

    public String getFrom() {
        return this.from;
    }

    public String getTo() {
        return this.to;
    }

    public float getAmount() {
        return this.amount;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getStatus() {
        return this.status;
    }

    public DateTime getTimestamp() {
        return this.timestamp;
    }

    public String getAddressType() {
        return this.addressType;
    }

    public String getPublicKey() {
        return this.publicKey;
    }
}

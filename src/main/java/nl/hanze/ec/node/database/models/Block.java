package nl.hanze.ec.node.database.models;

import com.google.gson.Gson;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import nl.hanze.ec.node.network.peers.commands.responses.TransactionsResponse;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.stream.Collectors;

@DatabaseTable(tableName = "blocks")
public class Block {
    @DatabaseField(canBeNull = false, id = true, columnName = "hash")
    private String hash;

    @DatabaseField(canBeNull = false, columnName = "previous_block_hash")
    private String previousBlockHash;

    @DatabaseField(canBeNull = false, columnName = "merkle_root_hash")
    private String merkleRootHash;

    @DatabaseField(canBeNull = false, columnName = "block_height")
    private int blockHeight;

    @DatabaseField(canBeNull = false, columnName = "timestamp")
    private DateTime timestamp;

    @DatabaseField(canBeNull = false, columnName = "type")
    private String type;

    @ForeignCollectionField(eager = false)
    private transient ForeignCollection<Transaction> transactions;

    // ORMLite requires a no-arg constructor.
    public Block() {}

    public Block(
            String hash,
            String previousBlockHash,
            String merkleRootHash,
            int blockHeight,
            String type
    ) {
        this.hash = hash;
        this.previousBlockHash = previousBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.blockHeight = blockHeight;
        this.type = type;
        this.timestamp = new DateTime();
    }

    public Block(
            String hash,
            String previousBlockHash,
            String merkleRootHash,
            int blockHeight,
            String type,
            DateTime timestamp
    ) {
        this(hash, previousBlockHash, merkleRootHash, blockHeight, type);
        this.timestamp = timestamp;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public String getMerkleRootHash() {
        return merkleRootHash;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ForeignCollection<Transaction> getTransactions() {
        return transactions;
    }

    public JSONObject toJSONObject() {
        Gson gson = new Gson();
        JSONObject object = null;
        try {
            object = new JSONObject(gson.toJson(this));
            object.put("transactions", this.getTransactions().stream()
                    .map(tx -> new TransactionsResponse.Tx(
                            tx.getHash(),
                            tx.getFrom(),
                            tx.getTo(),
                            tx.getAmount(),
                            tx.getSignature(),
                            tx.getStatus(),
                            tx.getAddressType(),
                            tx.getPublicKey(),
                            tx.getTimestamp()
                    ).toMap())
                    .collect(Collectors.toList()));
            object.put("timestamp", this.timestamp.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        return object;
    }

    @Override
    public String toString() {
        return "Block{" +
                "hash='" + hash + '\'' +
                ", previousBlockHash='" + previousBlockHash + '\'' +
                ", merkleRootHash='" + merkleRootHash + '\'' +
                ", blockHeight=" + blockHeight +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                '}';
    }
}

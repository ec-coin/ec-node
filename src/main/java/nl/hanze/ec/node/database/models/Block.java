package nl.hanze.ec.node.database.models;

import com.google.gson.Gson;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

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

    @ForeignCollectionField(eager = false)
    private ForeignCollection<Transaction> transactions;

    // ORMLite requires a no-arg constructor.
    public Block() {}

    public Block(String hash, String previousBlockHash, String merkleRootHash, int blockHeight) {
        this.hash = hash;
        this.previousBlockHash = previousBlockHash;
        this.merkleRootHash = merkleRootHash;
        this.blockHeight = blockHeight;
        this.timestamp = new DateTime();
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

    public ForeignCollection<Transaction> getTransactions() {
        return transactions;
    }

    public JSONObject toJSONObject() {
        Gson gson = new Gson();
        JSONObject object = null;
        try {
            object = new JSONObject(gson.toJson(this));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        return object;
    }
}

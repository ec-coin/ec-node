package nl.hanze.ec.node.database.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "balances_cache")
public class BalancesCache {
    @DatabaseField(canBeNull = false, id = true, columnName = "address")
    private String address;

    @DatabaseField(canBeNull = false, columnName = "balance")
    private float balance;

    // ORMLite requires a no-arg constructor.
    public BalancesCache() {}

    public BalancesCache(String address, float balance) {
        this.address = address;
        this.balance = balance;
    }

    public String getAddress() {
        return address;
    }

    public float getBalance() {
        return balance;
    }
}

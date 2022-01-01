package nl.hanze.ec.node.utils;

import nl.hanze.ec.node.database.models.Transaction;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HashingUtils {

    private synchronized static String hash(String value) {
        String hash = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            BigInteger noHash = new BigInteger(1, bytes);
            hash = noHash.toString(16);
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    public synchronized static String generateTransactionHash(String from, String to, float amount, String signature) {
        return hash(from + to + amount + signature);
    }

    public synchronized static String generateMerkleRootHash(List<Transaction> transactions) {
        StringBuilder input = new StringBuilder();
        for (Transaction transaction : transactions) {
            input.append(transaction.getHash());
        }
        return hash(input.toString());
    }

    public synchronized static String generateBlockHash(String merkleRootHash, String previousHash, DateTime timestamp) {
        return hash(merkleRootHash + previousHash + timestamp.toString());
    }
}

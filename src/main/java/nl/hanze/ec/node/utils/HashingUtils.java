package nl.hanze.ec.node.utils;

import nl.hanze.ec.node.database.models.Transaction;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.List;

public class HashingUtils {

    private synchronized static String hash(String value) {
        String hash = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // value = diversifier() + value + diversifier();
            byte[] bytes = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            BigInteger noHash = new BigInteger(1, bytes);
            hash = noHash.toString(16);
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    private static String diversifier() throws NoSuchAlgorithmException {
        return SecureRandom.getInstanceStrong().ints(48, 123)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(16)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public synchronized static String generateTransactionHash(String from, String to, float amount, String signature) {
        return hash(from + to + amount + signature);
    }

    public synchronized static String generateMerkleRootHash(List<Transaction> transactions) {
        StringBuilder input = new StringBuilder();
        int order = 1;
        for (Transaction transaction : transactions) {
            input.append(transaction.getHash());
            transaction.setOrderInBlock(order++);
        }
        return hash(input.toString());
    }

    public synchronized static String generateBlockHash(String merkleRootHash, String previousHash, DateTime timestamp) {
        return hash(merkleRootHash + previousHash + timestamp.toString());
    }

    public synchronized static String getAddress(PublicKey publicKey) {
        return hash(publicKey.toString());
    }

    public synchronized static boolean validateMerkleRootHash(String merkleRootHash, List<String> transactionHashes) {
        StringBuilder input = new StringBuilder();
        for (String hash : transactionHashes) {
            input.append(hash);
        }
        return merkleRootHash.equals(hash(input.toString()));
    }
}

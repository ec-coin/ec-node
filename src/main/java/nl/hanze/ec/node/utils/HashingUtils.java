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

    public synchronized static byte[] hash(String value) {
        byte[] hash = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // value = diversifier() + value + diversifier();
            hash = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
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
        return new BigInteger(1, hash(from + to + amount + signature)).toString(16);
    }

    public synchronized static String generateMerkleRootHash(List<Transaction> transactions) {
        StringBuilder input = new StringBuilder();
        int order = 1;
        for (Transaction transaction : transactions) {
            input.append(transaction.getHash());
            transaction.setOrderInBlock(order++);
        }
        return new BigInteger(1, hash(input.toString())).toString(16);
    }

    public synchronized static String generateBlockHash(String merkleRootHash, String previousHash, DateTime timestamp) {
        return new BigInteger(1, hash(merkleRootHash + previousHash + timestamp.toString())).toString(16);
    }

    public synchronized static String getAddress(PublicKey publicKey) {
        return BaseNUtils.Base58Encode(new BigInteger(1, hash(publicKey.toString())).toString(16), 16);
    }

    public synchronized static boolean validateMerkleRootHash(String merkleRootHash, List<String> transactionHashes) {
        StringBuilder input = new StringBuilder();
        for (String hash : transactionHashes) {
            input.append(hash);
        }
        String inputHash = new BigInteger(1, hash(input.toString())).toString(16);
        return merkleRootHash.equals(inputHash);
    }
}

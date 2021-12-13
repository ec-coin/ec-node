package nl.hanze.ec.node.services;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class HashingService {

    public synchronized static String hash(String value) {
        String hash = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            value = diversifier() + value + diversifier();
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
}

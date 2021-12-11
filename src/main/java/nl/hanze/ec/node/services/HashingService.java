package nl.hanze.ec.node.services;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class HashingService {

    public static String hash(String value) {
        //SecureRandom.getInstance()
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            value = diversifier() + value + diversifier();
            messageDigest.update(value.getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new BigInteger(1, messageDigest.digest()).toString(16);
    }

    private static String diversifier() {
        return new Random().ints(48, 123)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(16)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}

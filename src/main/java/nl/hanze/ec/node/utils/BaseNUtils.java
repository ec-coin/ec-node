package nl.hanze.ec.node.utils;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;


/**
 * Base58 inspired by: https://datatracker.ietf.org/doc/html/draft-msporny-base58
 * Base64 inspired by: https://www.rfc-editor.org/rfc/rfc4648.
 */
public class BaseNUtils {
    private static final Map<Integer, String> alphabetMapping = Map.of(
            2,  "01",
            10, "0123456789",
            16, "0123456789abcdef",
            58, "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz",
            64, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    );

    public synchronized static String Base64Encode(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    public synchronized static byte[] Base64Decode(String input) {
        return Base64.getDecoder().decode(input);
    }

    public synchronized static String Base58Encode(String input, int baseInput) {
        return encode(input, baseInput, 58, alphabetMapping.get(58).toCharArray());
    }

    public synchronized static String Base58Decode(String input, int baseOutput) {
        return decode(input, 58, baseOutput, alphabetMapping.get(58));
    }

    private synchronized static String encode(String input, int baseInput, int baseOutput, char[] characterSet) {
        if (baseInput == baseOutput) {
            return input;
        }

        BigInteger baseNInt = new BigInteger(String.valueOf(baseOutput));
        BigInteger bigInt = new BigInteger(input, baseInput);
        StringBuilder stringBuilder = new StringBuilder();
        BigInteger remainder;

        while (bigInt.intValue() != 0) {
            remainder = bigInt.remainder(baseNInt);
            bigInt = bigInt.divide(baseNInt);
            stringBuilder.append(characterSet[remainder.intValue()]);
        }

        return stringBuilder.reverse().toString();
    }

    private synchronized static String decode(String input, int baseInput, int baseOutput, String characterSet) {
        if (baseInput == baseOutput) {
            return input;
        }

        int length = input.length() - 1;
        int indexAt;
        int sum = 0;

        for (char character : input.toCharArray()) {
            indexAt = characterSet.indexOf(character);
            sum += indexAt * Math.pow(baseInput, length--);
        }

        return encode(String.valueOf(sum), 10, baseOutput, alphabetMapping.get(baseOutput).toCharArray());
    }
}

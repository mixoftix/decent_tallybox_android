package net.mixoftix.tallybox;

import java.math.BigInteger;
import java.util.Arrays;

public class Base58 {

    // Base58 character set
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final BigInteger BASE = BigInteger.valueOf(ALPHABET.length);
    private static final int[] INDEXES = new int[128];

    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

    // Encode a BigInteger to a Base58 string
    public static String encode(BigInteger value) {
        StringBuilder sb = new StringBuilder();
        while (value.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = value.divideAndRemainder(BASE);
            sb.append(ALPHABET[divmod[1].intValue()]);
            value = divmod[0];
        }
        return sb.reverse().toString();
    }

    // Decode a Base58 string to a BigInteger
    public static BigInteger decode(String input) {
        BigInteger value = BigInteger.ZERO;
        for (char c : input.toCharArray()) {
            value = value.multiply(BASE).add(BigInteger.valueOf(INDEXES[c]));
        }
        return value;
    }

    // Test the encoding and decoding methods
    public static void main(String[] args) {
        // Example BigInteger value
        BigInteger bigInt = new BigInteger("1234567890123456789012345678901234567890");

        // Encode the BigInteger to Base58
        String encoded = encode(bigInt);
        System.out.println("Encoded: " + encoded);

        // Decode the Base58 string back to BigInteger
        BigInteger decoded = decode(encoded);
        System.out.println("Decoded: " + decoded.toString());
    }
}

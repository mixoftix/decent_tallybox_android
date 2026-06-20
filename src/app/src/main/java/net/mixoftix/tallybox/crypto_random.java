package net.mixoftix.tallybox;

import java.util.Random;
import java.security.SecureRandom;
import java.nio.ByteBuffer;

public class crypto_random {

    public static int simple_rnd (int min, int max) {
        int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }

    // class update: 08/15/2023

    // chars_numeric dataset suits for "digit" - "OTP"
    private static final char[] CHARS_NUMERIC = "1234567890".toCharArray();

    // chars_sha256 dataset suits for "difficulty"
    private static final char[] CHARS_SHA256 = "abcdef1234567890".toCharArray();

    // chars_alphabet dataset suits for "key"
    private static final char[] CHARS_ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();

    public static String rnd_advanced(int size, String myDataset) {
        byte[] data = new byte[4 * size];
        long idx = 0;

        // Using SecureRandom instead of RNGCryptoServiceProvider
        SecureRandom crypto = new SecureRandom();
        crypto.nextBytes(data);

        StringBuilder result = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            // Convert 4 bytes to unsigned int
            ByteBuffer bb = ByteBuffer.wrap(data, i * 4, 4);
            long rnd = bb.getInt() & 0xFFFFFFFFL;

            if ("digit".equals(myDataset)) {
                idx = rnd % CHARS_NUMERIC.length;
                result.append(CHARS_NUMERIC[(int)idx]);
            }
            if ("difficulty".equals(myDataset)) {
                idx = rnd % CHARS_SHA256.length;
                result.append(CHARS_SHA256[(int)idx]);
            }
            if ("key".equals(myDataset)) {
                idx = rnd % CHARS_ALPHABET.length;
                result.append(CHARS_ALPHABET[(int)idx]);
            }
        }

        return result.toString();
    }

}

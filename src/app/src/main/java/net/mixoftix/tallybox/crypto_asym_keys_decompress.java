package net.mixoftix.tallybox;
import java.math.BigInteger;

public class crypto_asym_keys_decompress {

    // secp256r1 curve parameters
    private static final BigInteger secp256r1_P = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private static final BigInteger secp256r1_A = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16);
    private static final BigInteger secp256r1_B = new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16);

    // secp256k1 curve parameters
    private static final BigInteger secp256k1_P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
    private static final BigInteger secp256k1_A = BigInteger.ZERO;
    private static final BigInteger secp256k1_B = BigInteger.valueOf(7);

    /**
     * Decompresses a secp256r1 compressed public key
     * @param compressedKey String in format "hexValue*1" (odd y) or "hexValue*2" (even y)
     * @return String in format "xHex*yHex"
     */
    public static String decompressSecp256r1(String compressedKey) {
        String[] parts = compressedKey.split("\\*");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid compressed key format");
        }

        String xHex = parts[0];
        boolean isYEven = parts[1].equals("1");  // *1 = odd, *2 = even, so isYEven is true for odd

        BigInteger x = new BigInteger(xHex, 16);
        BigInteger ySquared = x.modPow(BigInteger.valueOf(3), secp256r1_P)
                .add(secp256r1_A.multiply(x).mod(secp256r1_P))
                .add(secp256r1_B)
                .mod(secp256r1_P);

        BigInteger y = ySquared.modPow(secp256r1_P.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), secp256r1_P);
        boolean yIsEven = y.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO);

        if (yIsEven == isYEven) {
            y = secp256r1_P.subtract(y);
        }

        return x.toString(16) + "*" + y.toString(16);
    }

    /**
     * Decompresses a secp256k1 compressed public key
     * @param compressedKey String in format "hexValue*1" (odd y) or "hexValue*2" (even y)
     * @return String in format "xHex*yHex"
     */
    public static String decompressSecp256k1(String compressedKey) {
        String[] parts = compressedKey.split("\\*");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid compressed key format");
        }

        String xHex = parts[0];
        boolean isYEven = parts[1].equals("1");  // *1 = odd, *2 = even, so isYEven is true for odd

        BigInteger x = new BigInteger(xHex, 16);
        BigInteger ySquared = x.modPow(BigInteger.valueOf(3), secp256k1_P)
                .add(secp256k1_B)
                .mod(secp256k1_P);

        BigInteger y = ySquared.modPow(secp256k1_P.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), secp256k1_P);
        boolean yIsEven = y.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO);

        if (yIsEven == isYEven) {
            y = secp256k1_P.subtract(y);
        }

        return x.toString(16) + "*" + y.toString(16);
    }


}
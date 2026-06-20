package net.mixoftix.tallybox;

import java.math.BigInteger;
import java.security.spec.ECPoint;

public class crypto_asym_keys_compress {

    public static String PublicKeyCompression(String publicKey_xHex, String publicKey_yHex, String output_format) {

        // Example public key (x, y) for secp256r1
        //BigInteger x = new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16); // Generator x
        //BigInteger y = new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16); // Generator y
        BigInteger x = new BigInteger(publicKey_xHex, 16); // Generator x
        BigInteger y = new BigInteger(publicKey_yHex, 16); // Generator y

        ECPoint publicKey = new ECPoint(x, y);

        // Compress
        String compressed = compressPublicKey(publicKey);
        //System.out.println("Compressed: " + compressed);

        // split publicKey_xy

        String[] split_publicKey_xy;
        split_publicKey_xy = compressed.split("\\*");
        String publicKey_x = split_publicKey_xy[0];
        String publicKey_y = split_publicKey_xy[1];

        // default output_format is HEX
        if (output_format.equals("B58"))
        {
            BigInteger publicKey_x_bigInt = new BigInteger(publicKey_x, 16);
            String publicKey_x_b58 = Base58.encode(publicKey_x_bigInt);
            compressed = publicKey_x_b58;
        }
        return compressed + "*" + publicKey_y;
    }

    // Compress public key to your custom format: x + "~" + (y even ? "2" : "1")
    public static String compressPublicKey(ECPoint publicKey) {
        BigInteger x = publicKey.getAffineX();
        BigInteger y = publicKey.getAffineY();
        String xHex = x.toString(16); // Convert x to hex
        String suffix = y.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO) ? "2" : "1"; // Even = 2, Odd = 1
        return xHex + "*" + suffix;
    }

}



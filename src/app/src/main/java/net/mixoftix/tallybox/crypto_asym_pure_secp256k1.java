package net.mixoftix.tallybox;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.util.Base64;


public class crypto_asym_pure_secp256k1 {

    public static KeyPair createKeyPair() throws Exception {
        // Generate the key pair for secp256k1
        KeyPair keyPair = generateSecp256k1KeyPair();

        return keyPair;
    }
    public static String extract_d_param(KeyPair KeyPair) {

        String privateKey_d = "-";
        PrivateKey privateKey = KeyPair.getPrivate();

        if (privateKey instanceof ECPrivateKey)
        {
            ECPrivateKey ecPrivateKey = (ECPrivateKey) privateKey;
            BigInteger d = ecPrivateKey.getS();
            privateKey_d = d.toString(16);
        }

        return privateKey_d;
    }
    public static String extract_xy_param(KeyPair KeyPair) {

        String publicKey_x = "-";
        String publicKey_y = "-";
        PublicKey publicKey = KeyPair.getPublic();

        if (publicKey instanceof ECPublicKey)
        {
            ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
            ECPoint ecPoint = ecPublicKey.getW();
            BigInteger x = ecPoint.getAffineX();
            BigInteger y = ecPoint.getAffineY();

            publicKey_x = x.toString(16);
            publicKey_y = y.toString(16);
        }

        return publicKey_x + "~" + publicKey_y;
    }

    public static PrivateKey rebuild_by_d_param(String dHex) throws Exception {
        // Convert d from hex to BigInteger
        BigInteger d = new BigInteger(dHex, 16);

        // Get the secp256k1 curve parameters
        ECParameterSpec secp256k1Spec = getECParameterSpecForSecp256k1();

        // Create the private key spec
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, secp256k1Spec);

        // Generate the private key from the spec
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(ecPrivateKeySpec);
    }
    public static PublicKey rebuild_by_xy_param(String xHex, String yHex) throws Exception {
        // Convert x and y from hex to BigInteger
        BigInteger x = new BigInteger(xHex, 16);
        BigInteger y = new BigInteger(yHex, 16);
        ECPoint ecPoint = new ECPoint(x, y);

        // Get the secp256k1 curve parameters
        ECParameterSpec secp256k1Spec = getECParameterSpecForSecp256k1();

        // Create the public key spec
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPoint, secp256k1Spec);

        // Generate the public key from the spec
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(ecPublicKeySpec);
    }


    // Method to sign a message using a private key
    public static String signMessage(String message, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));

        String my_signature = Base64.getEncoder().encodeToString(signature.sign());
        return my_signature;
    }


    // Define secp256k1 curve parameters
    public static KeyPair generateSecp256k1KeyPair() throws Exception {
        // Define secp256k1 curve parameters
        ECParameterSpec secp256k1Spec = getECParameterSpecForSecp256k1();

        // Create the KeyPairGenerator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");

        // Initialize with custom secp256k1 parameters
        keyPairGenerator.initialize(secp256k1Spec, new SecureRandom());

        // Generate the KeyPair
        return keyPairGenerator.generateKeyPair();
    }
    // Define secp256k1 curve parameters
    private static ECParameterSpec getECParameterSpecForSecp256k1() {
        BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
        BigInteger a = BigInteger.ZERO; // a = 0 for secp256k1
        BigInteger b = BigInteger.valueOf(7); // b = 7 for secp256k1
        BigInteger order = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
        BigInteger cofactor = BigInteger.ONE;
        ECFieldFp field = new ECFieldFp(p);
        EllipticCurve curve = new EllipticCurve(field, a, b);
        ECPoint generator = new ECPoint(
                new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
                new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
        );
        return new ECParameterSpec(curve, generator, order, cofactor.intValue());
    }
    public static void Secp256k1PrivateKeyRebuilder() throws Exception {
        // Example private key d value (replace with your actual d)
        String dHex = "18E14A7B6A307F426A94F8114701E7C8E774E7F9A47E2C2035DB29A206321725";

        // Rebuild the private key
        PrivateKey privateKey = rebuild_by_d_param(dHex);

        // Output the result
        System.out.println("Rebuilt Private Key: " + privateKey);
    }
    public static void Secp256k1PublicKeyRebuilder () throws Exception {
        // Example public key x and y values (replace with your actual values)
        String xHex = "50863AD64A87AE8A2FE83C1AF1A8403CB53F53E486D8511DAD8A04887E5B2352";
        String yHex = "2CD470243453A299FA9E77237716103ABC11A1DF38855ED6F2EE187E9C582BA6";

        // Rebuild the public key
        PublicKey publicKey = rebuild_by_xy_param(xHex, yHex);

        // Output the result
        System.out.println("Rebuilt Public Key: " + publicKey);
    }

}
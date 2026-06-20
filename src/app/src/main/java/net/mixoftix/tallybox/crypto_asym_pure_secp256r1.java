package net.mixoftix.tallybox;

import android.util.Log;
import android.util.Base64;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class crypto_asym_pure_secp256r1 {

    public static KeyPair createKeyPair() {

        KeyPair keyPair = null;

        try {
            // Create the KeyPairGenerator
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyPairGenerator.initialize(ecSpec, new SecureRandom());

            // Generate the KeyPair
            keyPair = keyPairGenerator.generateKeyPair();

            /*
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // Print the keys
            Log.i("shahin","Private Key: " + privateKey.toString());
            Log.i("shahin","Public Key: " + publicKey.toString());

            saveKey("privateKey_pem", keyPair.getPrivate());
            saveKey("publicKey_pem", keyPair.getPublic());
            */

        } catch (NoSuchAlgorithmException e) {
            Log.i("shahin", "keyPair - " + e);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            Log.i("shahin", "keyPair - " + e);
            e.printStackTrace();
        }
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

    public static PrivateKey rebuild_by_d_param(String privateKey_d) {

        PrivateKey privateKey_reloaded = null;

        try {
            // private key d value
            String dHex = privateKey_d;

            BigInteger d = new BigInteger(dHex, 16);

            // Get the EC parameter spec for the curve (secp256r1)
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

            // Create the private key spec
            ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, ecParameterSpec);

            // Generate the private key from the spec
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            privateKey_reloaded = keyFactory.generatePrivate(ecPrivateKeySpec);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return privateKey_reloaded;

    }
    public static PublicKey rebuild_by_xy_param(String publicKey_xy) {

        PublicKey publicKey_reloaded = null;

        // split publicKey_xy
        String[] split_publicKey_xy;
        split_publicKey_xy = publicKey_xy.split("~");
        String publicKey_x = split_publicKey_xy[0];
        String publicKey_y = split_publicKey_xy[1];

        try {
            // public-key x and y values
            String xHex = publicKey_x;
            String yHex = publicKey_y;

            BigInteger x = new BigInteger(xHex, 16);
            BigInteger y = new BigInteger(yHex, 16);
            ECPoint ecPoint = new ECPoint(x, y);

            // Get the EC parameter spec for the curve (secp256r1)
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

            // Create the public key spec
            ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);

            // Generate the public key from the spec
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            publicKey_reloaded = keyFactory.generatePublic(ecPublicKeySpec);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return publicKey_reloaded;
    }


    // Method to sign a message using a private key
    public static String signMessage(String message, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);

        //signature.update(message.getBytes(StandardCharsets.UTF_8));
        //byte[] message_data = message.getBytes();
        byte[] message_data = message.getBytes(StandardCharsets.UTF_8);
        signature.update(message_data);

        //String my_signature = Base64.getEncoder().encodeToString(signature.sign());
        byte[] signatureBytes = signature.sign();
        String my_signature = Base64.encodeToString(signatureBytes, Base64.DEFAULT);

        return my_signature;
    }

    // Method to verify a signature using a public key
    public static boolean verifySignature(String message, String signatureStr, PublicKey publicKey) throws Exception {

        // Convert the signature to byte array
        //byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);
        byte[] signatureBytes = Base64.decode(signatureStr, Base64.DEFAULT);

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);

        //signature.update(message.getBytes(StandardCharsets.UTF_8));
        //byte[] message_data = message.getBytes();
        byte[] message_data = message.getBytes(StandardCharsets.UTF_8);
        signature.update(message_data);

        return signature.verify(signatureBytes);
    }

}

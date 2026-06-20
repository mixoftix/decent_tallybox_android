package net.mixoftix.tallybox;

import android.util.Log;

import java.math.BigInteger;
        import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
        import java.security.PublicKey;
        import java.security.spec.ECPrivateKeySpec;
        import java.security.spec.ECPublicKeySpec;
        import java.security.spec.ECParameterSpec;
        import java.security.spec.ECPoint;
        import java.security.spec.EllipticCurve;
        import java.security.spec.ECFieldFp;
        import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;

public class crypto_asym_keys_recovery {
    public static void test_recover2() throws Exception {
        /*
        // Example private key (replace with your actual private key)
        BigInteger privateKeyValue = new BigInteger("09e2c6cd535d6439d4bb5d57d50c132ca292340ca37076fd262e4c336f571d792", 16);
        //BigInteger privateKeyValue = new BigInteger(privateKey_d, 16);
        //Log.w("shahin", "wallet recovery by (d): " + privateKey_d);

        // Define the curve (secp256r1)
        ECParameterSpec ecSpec = getECParameterSpecForSecp256r1();
        // Define the curve (secp256k1)
        //ECParameterSpec ecSpec = getECParameterSpecForSecp256k1();

        // Create private key spec
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        // Recover public key
        PublicKey publicKey = generatePublicKeyFromPrivate((ECPrivateKey) privateKey);

        // Output the result
        System.out.println("Public Key: " + publicKey);
        Log.w("shahin", "publicKey recovery by (d): " + publicKey);
        */

        ECParameterSpec spec = getECParameterSpecForSecp256r1();
        BigInteger privateKey = new BigInteger("9e2c6cd535d6439d4bb5d57d50c132ca292340ca37076fd262e4c336f571d792", 16); // Replace with your d
        ECPoint publicKey = scalarMultiply(privateKey, spec.getGenerator(), spec);

        Log.w("shahin", "publicKey recovery by (d): " + publicKey);
        Log.w("shahin", "publicKey (x): " + publicKey.getAffineX().toString(16));
        Log.w("shahin", "publicKey (y): " + publicKey.getAffineY().toString(16));
    }
    // publickey recovery
    public static ECPoint recover(String curve_type, String privateKey_d) throws Exception {

        // define the curve
        ECParameterSpec ecSpec = null;
        if (curve_type.equals("secp256r1"))
        {
            ecSpec = getECParameterSpecForSecp256r1();
        }
        if (curve_type.equals("secp256k1"))
        {
            ecSpec = getECParameterSpecForSecp256k1();
        }

        // private key
        BigInteger privateKey_d_bigint = new BigInteger(privateKey_d, 16);

        // Recover public key
        ECPoint publicKey_w = scalarMultiply(privateKey_d_bigint, ecSpec.getGenerator(), ecSpec);

        //Log.w("shahin", "publicKey recovery by (d): " + publicKey_w);
        //Log.w("shahin", "publicKey (x): " + publicKey_w.getAffineX().toString(16));
        //Log.w("shahin", "publicKey (y): " + publicKey_w.getAffineY().toString(16));

        Access_log.log_it("w","shahin","publicKey (x): " + publicKey_w.getAffineX().toString(16));
        Access_log.log_it("w","shahin","publicKey (y): " + publicKey_w.getAffineY().toString(16));

        return publicKey_w;
    }
    // Scalar multiplication: Q = k * P
    public static ECPoint scalarMultiply(BigInteger k, ECPoint p, ECParameterSpec spec) {
        BigInteger pMod = ((ECFieldFp) spec.getCurve().getField()).getP();
        BigInteger a = spec.getCurve().getA();

        ECPoint result = ECPoint.POINT_INFINITY; // Identity element
        ECPoint temp = p;

        String kBits = k.toString(2); // Binary representation of scalar
        for (int i = kBits.length() - 1; i >= 0; i--) {
            if (kBits.charAt(i) == '1') {
                result = addPoints(result, temp, a, pMod);
            }
            temp = addPoints(temp, temp, a, pMod); // Double the point
        }

        return result;
    }
    // Point addition: R = P + Q
    private static ECPoint addPoints(ECPoint p1, ECPoint p2, BigInteger a, BigInteger p) {
        if (p1.equals(ECPoint.POINT_INFINITY)) return p2;
        if (p2.equals(ECPoint.POINT_INFINITY)) return p1;

        BigInteger x1 = p1.getAffineX();
        BigInteger y1 = p1.getAffineY();
        BigInteger x2 = p2.getAffineX();
        BigInteger y2 = p2.getAffineY();

        if (x1.equals(x2) && !y1.equals(y2)) {
            return ECPoint.POINT_INFINITY; // Points are inverses
        }

        BigInteger slope;
        if (p1.equals(p2)) {
            // Point doubling: slope = (3x^2 + a) / (2y)
            BigInteger num = x1.pow(2).multiply(BigInteger.valueOf(3)).add(a).mod(p);
            BigInteger den = y1.multiply(BigInteger.valueOf(2)).mod(p);
            slope = num.multiply(den.modInverse(p)).mod(p);
        } else {
            // Point addition: slope = (y2 - y1) / (x2 - x1)
            BigInteger num = y2.subtract(y1).mod(p);
            BigInteger den = x2.subtract(x1).mod(p);
            slope = num.multiply(den.modInverse(p)).mod(p);
        }

        // xr = slope^2 - x1 - x2
        BigInteger xr = slope.pow(2).subtract(x1).subtract(x2).mod(p);
        // yr = slope * (x1 - xr) - y1
        BigInteger yr = slope.multiply(x1.subtract(xr)).subtract(y1).mod(p);

        return new ECPoint(xr, yr);
    }
    // Define secp256r1 curve parameters
    private static ECParameterSpec getECParameterSpecForSecp256r1() {
        // Prime modulus p & Curve coefficients
        BigInteger p = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16);
        BigInteger a = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16);
        BigInteger b = new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16);
        // Order n
        BigInteger order = new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);
        BigInteger cofactor = BigInteger.ONE;
        ECFieldFp field = new ECFieldFp(p);
        EllipticCurve curve = new EllipticCurve(field, a, b);
        ECPoint generator = new ECPoint(
                new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16),
                new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)
        );
        return new ECParameterSpec(curve, generator, order, cofactor.intValue());
    }
    // Define secp256k1 curve parameters
    private static ECParameterSpec getECParameterSpecForSecp256k1() {
        // Prime modulus p & Curve coefficients
        BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
        BigInteger a = BigInteger.ZERO; // secp256k1 has a = 0
        BigInteger b = BigInteger.valueOf(7); // secp256k1 has b = 7
        // Order n
        BigInteger order = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
        BigInteger cofactor = BigInteger.ONE;
        ECFieldFp field = new ECFieldFp(p);
        EllipticCurve curve = new EllipticCurve(field, a, b);
        // Generator point G
        ECPoint generator = new ECPoint(
                new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
                new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
        );
        return new ECParameterSpec(curve, generator, order, cofactor.intValue());
    }


    /*
    public static void test_recover() throws Exception {
        // Example private key (replace with your actual private key)
        BigInteger privateKeyValue = new BigInteger("09e2c6cd535d6439d4bb5d57d50c132ca292340ca37076fd262e4c336f571d792", 16);
        //BigInteger privateKeyValue = new BigInteger(privateKey_d, 16);
        //Log.w("shahin", "wallet recovery by (d): " + privateKey_d);

        // Define the curve (secp256r1)
        ECParameterSpec ecSpec = getECParameterSpecForSecp256r1();
        // Define the curve (secp256k1)
        //ECParameterSpec ecSpec = getECParameterSpecForSecp256k1();

        // Create private key spec
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        // Recover public key
        PublicKey publicKey = generatePublicKeyFromPrivate((ECPrivateKey) privateKey);

        // Output the result
        System.out.println("Public Key: " + publicKey);
        Log.w("shahin", "publicKey recovery by (d): " + publicKey);

    }
    public static PublicKey recover(String curve_type, String privateKey_d) throws Exception {

        // define the curve
        ECParameterSpec ecSpec = null;
        if (curve_type.equals("secp256r1"))
        {
            ecSpec = getECParameterSpecForSecp256r1();
        }
        if (curve_type.equals("secp256k1"))
        {
            ecSpec = getECParameterSpecForSecp256k1();
        }

        // private key
        BigInteger privateKey_d_bigint = new BigInteger(privateKey_d, 16);

        // Create private key spec
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKey_d_bigint, ecSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        // Recover public key
        PublicKey publicKey = generatePublicKeyFromPrivate((ECPrivateKey) privateKey);

        return publicKey;
    }
    // Define secp256r1 curve parameters
    private static ECParameterSpec getECParameterSpecForSecp256r1() {
        // Prime modulus p & Curve coefficients
        BigInteger p = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16);
        BigInteger a = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16);
        BigInteger b = new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16);
        // Order n
        BigInteger order = new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);
        BigInteger cofactor = BigInteger.ONE;
        ECFieldFp field = new ECFieldFp(p);
        EllipticCurve curve = new EllipticCurve(field, a, b);
        ECPoint generator = new ECPoint(
                new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16),
                new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)
        );
        return new ECParameterSpec(curve, generator, order, cofactor.intValue());
    }
    // Define secp256k1 curve parameters
    private static ECParameterSpec getECParameterSpecForSecp256k1() {
        // Prime modulus p & Curve coefficients
        BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
        BigInteger a = BigInteger.ZERO; // secp256k1 has a = 0
        BigInteger b = BigInteger.valueOf(7); // secp256k1 has b = 7
        // Order n
        BigInteger order = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
        BigInteger cofactor = BigInteger.ONE;
        ECFieldFp field = new ECFieldFp(p);
        EllipticCurve curve = new EllipticCurve(field, a, b);
        // Generator point G
        ECPoint generator = new ECPoint(
                new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16),
                new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
        );
        return new ECParameterSpec(curve, generator, order, cofactor.intValue());
    }
    // Recover public key from private key
    private static PublicKey generatePublicKeyFromPrivate(ECPrivateKey privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        ECParameterSpec ecSpec = privateKey.getParams();
        BigInteger s = privateKey.getS();
        ECPoint generator = ecSpec.getGenerator();
        ECPoint publicPoint = multiplyPoint(generator, s, ecSpec);

        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(publicPoint, ecSpec);
        return keyFactory.generatePublic(publicKeySpec);
    }
    // Complete multiplyPoint function for EC scalar multiplication
    private static ECPoint multiplyPoint(ECPoint point, BigInteger scalar, ECParameterSpec spec) {
        if (scalar.signum() == 0) {
            return ECPoint.POINT_INFINITY; // Return point at infinity for scalar = 0
        }

        EllipticCurve curve = spec.getCurve();
        BigInteger p = ((ECFieldFp) curve.getField()).getP(); // Prime modulus
        BigInteger a = curve.getA();

        ECPoint result = ECPoint.POINT_INFINITY; // Initialize as point at infinity
        ECPoint current = point;

        // Double-and-add algorithm
        String scalarBits = scalar.toString(2); // Convert scalar to binary
        for (int i = 0; i < scalarBits.length(); i++) {
            if (scalarBits.charAt(i) == '1') {
                result = addPoints(result, current, a, p);
            }
            current = doublePoint(current, a, p);
        }

        return result;
    }
    // Point addition: P + Q
    private static ECPoint addPoints_old(ECPoint p1, ECPoint p2, BigInteger a, BigInteger p) {
        if (p1.equals(ECPoint.POINT_INFINITY)) {
            return p2;
        }
        if (p2.equals(ECPoint.POINT_INFINITY)) {
            return p1;
        }

        BigInteger x1 = p1.getAffineX();
        BigInteger y1 = p1.getAffineY();
        BigInteger x2 = p2.getAffineX();
        BigInteger y2 = p2.getAffineY();

        if (x1.equals(x2) && !y1.equals(y2)) {
            return ECPoint.POINT_INFINITY; // Points are inverses
        }

        BigInteger lambda;
        if (p1.equals(p2)) {
            // Use doubling formula if points are the same
            return doublePoint(p1, a, p);
        } else {
            // Slope: (y2 - y1) / (x2 - x1) mod p
            BigInteger num = y2.subtract(y1);
            BigInteger den = x2.subtract(x1);
            lambda = num.multiply(den.modInverse(p)).mod(p);
        }

        // x3 = lambda^2 - x1 - x2 mod p
        BigInteger x3 = lambda.multiply(lambda).subtract(x1).subtract(x2).mod(p);
        // y3 = lambda * (x1 - x3) - y1 mod p
        BigInteger y3 = lambda.multiply(x1.subtract(x3)).subtract(y1).mod(p);

        return new ECPoint(x3, y3);
    }
    // Point doubling: 2P
    private static ECPoint doublePoint(ECPoint p1, BigInteger a, BigInteger p) {
        if (p1.equals(ECPoint.POINT_INFINITY)) {
            return ECPoint.POINT_INFINITY;
        }

        BigInteger x1 = p1.getAffineX();
        BigInteger y1 = p1.getAffineY();

        if (y1.signum() == 0) {
            return ECPoint.POINT_INFINITY; // Point at infinity if y = 0
        }

        // Slope: (3x1^2 + a) / (2y1) mod p
        BigInteger num = x1.multiply(x1).multiply(BigInteger.valueOf(3)).add(a).mod(p);
        BigInteger den = y1.multiply(BigInteger.valueOf(2)).modInverse(p);
        BigInteger lambda = num.multiply(den).mod(p);

        // x3 = lambda^2 - 2x1 mod p
        BigInteger x3 = lambda.multiply(lambda).subtract(x1.multiply(BigInteger.valueOf(2))).mod(p);
        // y3 = lambda * (x1 - x3) - y1 mod p
        BigInteger y3 = lambda.multiply(x1.subtract(x3)).subtract(y1).mod(p);

        return new ECPoint(x3, y3);
    }
*/
}
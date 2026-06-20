package net.mixoftix.tallybox;


import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

public class crypto_asym_keys_store {

    private static final int KEY_LENGTH = 2048;
    private static final String KEY_ALIAS = MainActivity.file_name_path; // "z_axis_keys";

    public static KeyPair createKeyPair() {
        KeyPair keyPair = null;

        try {
            // https://stackoverflow.com/questions/42570020/invalidkeyexception-keystore-operation-failed-on-rsa-decrypt-on-android-device
            // https://developer.android.com/privacy-and-security/keystore#java
            // https://stackoverflow.com/questions/30929103/digital-signature-in-java-android-rsa-keys
            // https://stackoverflow.com/questions/42110123/save-and-retrieve-keypair-in-androidkeystore

            KeyPairGenerator keygen = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

            keygen.initialize(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setKeySize(KEY_LENGTH)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build(), new SecureRandom());

            keyPair = keygen.generateKeyPair();

            Log.i("shahin", "keyPair_store - generated");

        } catch (NoSuchAlgorithmException e) {
            Log.i("shahin", "keyPair_store - " + e);
            e.printStackTrace();
            return null;
        } catch (InvalidAlgorithmParameterException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
        return keyPair;
    }

    public static String getPrivateKeyBase64Str(KeyPair keyPair){
        if (keyPair == null) return null;

        String my_prv_key = getBase64StrFromByte(keyPair.getPrivate().getEncoded());
        return my_prv_key;
    }

    public static String getPublicKeyBase64Str(KeyPair keyPair){
        if (keyPair == null) return null;

        String my_pub_key = getBase64StrFromByte(keyPair.getPublic().getEncoded());
        return my_pub_key;
    }

    public static String getBase64StrFromByte(byte[] key){
        if (key == null || key.length == 0) return null;
        //return new String(Base64.encode(key));
        return Base64.encodeToString(key, Base64.DEFAULT);
    }

    public static String DigitalSign(String data)  {

        try {

            /*
             * Use a PrivateKey in the KeyStore to create a signature over
             * some data.
             */
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(KEY_ALIAS, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.w("shahin", "Not an instance of a PrivateKeyEntry");
                return null;
            }
            Signature s = Signature.getInstance("SHA256withRSA");

            PrivateKey prv_key = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
            s.initSign(prv_key);
            s.update(data.getBytes());
            byte[] signature = s.sign();

            String my_sign = Base64.encodeToString(signature, Base64.DEFAULT);
            Log.i("shahin", "my_sign: " + my_sign);

            return my_sign;

        } catch(Exception e){
            Log.i("shahin", "my_sign - err: " + e);
            return null;
        }
    }

    public static boolean VerfiySign(String signature, String data){

        try{

            /*
             * Verify a signature previously made by a private key in the
             * KeyStore. This uses the X.509 certificate attached to the
             * private key in the KeyStore to validate a previously
             * generated signature.
             */
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(KEY_ALIAS, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.w("shahin", "Not an instance of a PrivateKeyEntry");
                return false;
            }
            Signature s = Signature.getInstance("SHA256withRSA");

            Certificate certificate = ((KeyStore.PrivateKeyEntry) entry).getCertificate();
            //Log.w("shahin", "certificate: " + certificate);
            s.initVerify(certificate);
            s.update(data.getBytes());
            boolean valid = s.verify(Base64.decode(signature, Base64.DEFAULT));
            Log.i("shahin", "my_verify: " + valid);

            return valid;

        }catch(Exception e){
            e.printStackTrace();
            Log.i("shahin", "my_verify - err: " + e);
            return false;
        }
    }

    public static String encriptData(String txt_plain)
    {
        String encoded = "";
        byte[] txt_cipher = null;

        try {

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            Cipher cip = null;
            RSAPublicKey pubKey = (RSAPublicKey) entry.getCertificate().getPublicKey();
            cip = Cipher.getInstance("RSA/ECB/PKCS1PADDING");  // "RSA/ECB/NoPadding"
            cip.init(Cipher.ENCRYPT_MODE, pubKey);
            txt_cipher = cip.doFinal(txt_plain.getBytes());
            encoded = Base64.encodeToString(txt_cipher, Base64.DEFAULT);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return encoded;
    }

    public static String decriptData(String txt_cipher_64)
    {
        byte[] txt_plain = null;

        try {

            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            Cipher cip = null;
            cip = Cipher.getInstance("RSA/ECB/PKCS1PADDING"); // "RSA/ECB/NoPadding"
            cip.init(Cipher.DECRYPT_MODE, entry.getPrivateKey());
            txt_plain = cip.doFinal(Base64.decode(txt_cipher_64, Base64.DEFAULT));

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new String(txt_plain);
    }

}

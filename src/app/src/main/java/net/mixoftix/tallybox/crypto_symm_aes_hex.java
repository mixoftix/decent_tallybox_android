package net.mixoftix.tallybox;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class crypto_symm_aes_hex {

    public static final String PROVIDER = "BC";
    public static final int KEY_LENGTH = 256;
    public static final int SALT_LENGTH = 16; // 20
    public static final int IV_LENGTH = 16;
    public static final int PBE_ITERATION_COUNT = 100;

    private static final String RANDOM_ALGORITHM = "SHA1PRNG";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String PBE_ALGORITHM = "PBEWithSHA256And256BitAES-CBC-BC";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String TAG = "shahin";

    private static String secret_key = "";
    private static String secret_token = "";


    public static void start_enc() {
        String originalPassword = "This is password of shahiN";
        Log.e(TAG, "originalPassword => " + originalPassword);

        String encryptedPassword = encryptAndStorePassword(originalPassword);
        Log.e(TAG, "encryptedPassword => " + encryptedPassword);

        String decryptedPassword = decryptAndGetPassword();
        Log.e(TAG, "decryptedPassword => " + decryptedPassword);
    }

    private static String decryptAndGetPassword() {

        //SharedPreferences prefs = getSharedPreferences("pswd", MODE_PRIVATE);
        //String encryptedPasswrd = prefs.getString("token", "");
        String encryptedPasswrd = secret_token;
        String passwrd = "";

        if (encryptedPasswrd!=null && !encryptedPasswrd.isEmpty()) {
            try {
                //String output = prefs.getString("S_KEY", "");
                String output = secret_key;
                byte[] encoded = hexStringToByteArray(output);
                SecretKey aesKey = new SecretKeySpec(encoded, SECRET_KEY_ALGORITHM);
                passwrd = decrypt(aesKey, encryptedPasswrd);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return passwrd;
    }

    public static String encryptAndStorePassword(String password) {

        //SharedPreferences.Editor editor = getSharedPreferences("pswd", MODE_PRIVATE).edit();
        String encryptedPassword = "";

        if (password!=null && !password.isEmpty()) {
            SecretKey secretKey = null;
            try {
                secretKey = getSecretKey(password, generateSalt());

                byte[] encoded = secretKey.getEncoded();
                String input = byteArrayToHexString(encoded);
                //editor.putString("S_KEY", input);
                secret_key = input;
                encryptedPassword = encrypt(secretKey, password);

            } catch (Exception e) {
                e.printStackTrace();
            }
            //editor.putString("token", encryptedPassword);
            //editor.apply();
            secret_token = encryptedPassword;
        }
        return encryptedPassword;
    }

    public static String encrypt(SecretKey secret, String cleartext) throws Exception {
        try {
            byte[] iv = generateIv();
            String ivHex = byteArrayToHexString(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            Cipher encryptionCipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secret, ivspec);
            byte[] encryptedText = encryptionCipher.doFinal(cleartext.getBytes(StandardCharsets.UTF_8));
            String encryptedHex = byteArrayToHexString(encryptedText);

            Log.e(TAG, "iv => " + new String(iv));
            Log.e(TAG, "ivHex => " + ivHex);
            Log.e(TAG, "encryptedText => " + new String(encryptedText));
            Log.e(TAG, "encryptedHex => " + encryptedHex);

            return ivHex + encryptedHex;

        } catch (Exception e) {
            Log.e("SecurityException", Objects.requireNonNull(Objects.requireNonNull(e.getCause()).getLocalizedMessage()));
            throw new Exception("Unable to encrypt", e);
        }
    }

    public static String decrypt(SecretKey secret, String encrypted) throws Exception {
        try {
            Cipher decryptionCipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
            String ivHex = encrypted.substring(0, IV_LENGTH * 2);
            String encryptedHex = encrypted.substring(IV_LENGTH * 2);
            IvParameterSpec ivspec = new IvParameterSpec(hexStringToByteArray(ivHex));
            decryptionCipher.init(Cipher.DECRYPT_MODE, secret, ivspec);
            byte[] decryptedText = decryptionCipher.doFinal(hexStringToByteArray(encryptedHex));
            String decrypted = new String(decryptedText, StandardCharsets.UTF_8);
            return decrypted;
        } catch (Exception e) {
            Log.e("SecurityException", Objects.requireNonNull(Objects.requireNonNull(e.getCause()).getLocalizedMessage()));
            throw new Exception("Unable to decrypt", e);
        }
    }

    public static String generateSalt() throws Exception {
        try {
            SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            Log.e(TAG, "salt => " + new String(salt));
            String saltHex = byteArrayToHexString(salt);
            Log.e(TAG, "saltHex => " + saltHex);
            return saltHex;
        } catch (Exception e) {
            throw new Exception("Unable to generate salt", e);
        }
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static SecretKey getSecretKey(String password, String salt) throws Exception {
        try {
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), hexStringToByteArray(salt), PBE_ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBE_ALGORITHM, PROVIDER);
            SecretKey tmp = factory.generateSecret(pbeKeySpec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), SECRET_KEY_ALGORITHM);
            return secret;
        } catch (Exception e) {
            throw new Exception("Unable to get secret key", e);
        }
    }

    private static byte[] generateIv() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }
}


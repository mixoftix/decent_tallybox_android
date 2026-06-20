package net.mixoftix.tallybox;


import android.util.Base64;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class crypto_symm_aes {

    public static final int KEY_LENGTH = 256;
    public static final int SALT_LENGTH = 16; // 20
    public static final int IV_LENGTH = 16;
    public static final int PBE_ITERATION_COUNT = 3; // 100

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String PBE_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String TAG = "shahin";


    // AES - key generator
    private static SecretKey get_aes_key(String password, String salt) throws Exception {
        try {
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(),
                    PBE_ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
            SecretKey tmp = factory.generateSecret(pbeKeySpec);
            SecretKey aes_key = new SecretKeySpec(tmp.getEncoded(), SECRET_KEY_ALGORITHM);
            return aes_key;
        } catch (Exception e) {
            throw new Exception("Unable to get secret key", e);
        }
    }

    // AES - basic functions
    public static String aes_encrypt_b64(String txt_plain, String aes_256_secret)
            throws Exception
    {
        if (aes_256_secret.length() < 64) {
            throw new IllegalArgumentException("Secret key must be at least 64 characters");
        }

        // UPDATE(2025-03-22): Fix by Grok
        String aes_password = aes_256_secret.substring(0, 32);   // First 32 hex chars (32 bytes)
        String aes_iv = aes_256_secret.substring(32, 48);       // Next 16 hex chars (16 bytes)
        String aes_salt = aes_256_secret.substring(48, 64);     // Last 16 hex chars (16 bytes)

        // key = password + salt
        SecretKey aes_key = get_aes_key(aes_password, aes_salt);

        // prepare IV
        byte[] my_iv = aes_iv.getBytes("ASCII");
        IvParameterSpec ivspec = new IvParameterSpec(my_iv);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, aes_key, ivspec);
        byte[] text_cipher = cipher.doFinal(txt_plain.getBytes(StandardCharsets.UTF_8));
        String text_cipher_64 = Base64.encodeToString(text_cipher, Base64.DEFAULT);

        return text_cipher_64;
    }
    public static String aes_decrypt_b64(String txt_cipher, String aes_256_secret)
            throws Exception
    {
        if (aes_256_secret.length() < 64) {
            throw new IllegalArgumentException("Secret key must be at least 64 characters");
        }

        // UPDATE(2025-03-22): Fix by Grok
        String aes_password = aes_256_secret.substring(0, 32);   // First 32 hex chars (32 bytes)
        String aes_iv = aes_256_secret.substring(32, 48);       // Next 16 hex chars (16 bytes)
        String aes_salt = aes_256_secret.substring(48, 64);     // Last 16 hex chars (16 bytes)

        // key = password + salt
        SecretKey aes_key = get_aes_key(aes_password, aes_salt);

        // prepare IV
        byte[] my_iv = aes_iv.getBytes("ASCII");
        IvParameterSpec ivspec = new IvParameterSpec(my_iv);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, aes_key, ivspec);
        byte[] text_plain = cipher.doFinal(Base64.decode(txt_cipher, Base64.DEFAULT));
        String text_plain_str = new String(text_plain, StandardCharsets.UTF_8);

        return text_plain_str;
    }

    // AES - advanced functions
    // custom_padding reference: https://mailarchive.ietf.org/arch/msg/ideas/VEzD4RXKlCFIUftYIrrEdMFgoW0/
    public static String AES_Encrypt_by_secret_with_custom_padding(String plain_text, String private_key_secret) throws Exception {

        // BGN: add custom-padding to the plain_text
        int custom_padding_size_left = Integer.parseInt(crypto_random.rnd_advanced(2, "digit"));
        String custom_padding_left = crypto_random.rnd_advanced(custom_padding_size_left, "key");

        int custom_padding_size_right = Integer.parseInt(crypto_random.rnd_advanced(2, "digit"));
        String custom_padding_right = crypto_random.rnd_advanced(custom_padding_size_right, "key");

        String plain_text_with_custom_padding = custom_padding_left + "|" + plain_text + "|" + custom_padding_right;
        // END: add custom-padding to the plain_text

        // encrypt
        String the_aes_encrypt_b64 = aes_encrypt_b64(plain_text_with_custom_padding, private_key_secret);

        return the_aes_encrypt_b64;
    }
    public static String AES_Decrypt_by_secret_with_custom_padding(String cipher_text, String private_key_secret) throws Exception {

        // decrypt
        String plain_text_with_custom_padding = aes_decrypt_b64(cipher_text, private_key_secret);

        // remove custom-padding to the plain_text_with_custom_padding
        int custom_padding_location_1 = plain_text_with_custom_padding.indexOf("|", 0) + 1;
        int custom_padding_location_2 = plain_text_with_custom_padding.indexOf("|", custom_padding_location_1);
        String plain_text = plain_text_with_custom_padding.substring(custom_padding_location_1,
                custom_padding_location_2);

        return plain_text;
    }

}


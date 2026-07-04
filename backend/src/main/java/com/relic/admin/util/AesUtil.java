package com.relic.admin.util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * AES encryption/decryption utility.
 *
 * <p>Uses {@code AES/ECB/PKCS5Padding} with a 128-bit key. The supplied key
 * string is padded with zero bytes or truncated to exactly 16 bytes so that a
 * consistent key length is always produced regardless of the input length.</p>
 *
 * <p>File-based operations stream data through {@link CipherInputStream} /
 * {@link CipherOutputStream} so that large backup files do not need to be
 * loaded entirely into memory.</p>
 */
public final class AesUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    /** 128-bit AES key length in bytes */
    private static final int KEY_LENGTH = 16;

    private AesUtil() {
    }

    /**
     * Normalize the given key string to a fixed 16-byte array. If the key is
     * longer than 16 bytes it is truncated; if shorter it is right-padded
     * with zero bytes.
     */
    private static byte[] normalizeKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[KEY_LENGTH];
        int length = Math.min(keyBytes.length, KEY_LENGTH);
        System.arraycopy(keyBytes, 0, result, 0, length);
        return result;
    }

    private static SecretKeySpec buildKeySpec(String key) {
        return new SecretKeySpec(normalizeKey(key), ALGORITHM);
    }

    /**
     * Encrypt a byte array with AES/ECB/PKCS5Padding.
     */
    public static byte[] encrypt(byte[] data, String key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildKeySpec(key));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt a byte array with AES/ECB/PKCS5Padding.
     */
    public static byte[] decrypt(byte[] data, String key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildKeySpec(key));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt a source file and write the ciphertext to the destination file.
     */
    public static void encryptFile(File source, File dest, String key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildKeySpec(key));
            try (InputStream in = Files.newInputStream(source.toPath());
                 OutputStream out = new CipherOutputStream(Files.newOutputStream(dest.toPath()), cipher)) {
                in.transferTo(out);
            }
        } catch (Exception e) {
            throw new RuntimeException("AES file encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt a source file and write the plaintext to the destination file.
     */
    public static void decryptFile(File source, File dest, String key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildKeySpec(key));
            try (InputStream in = new CipherInputStream(Files.newInputStream(source.toPath()), cipher);
                 OutputStream out = Files.newOutputStream(dest.toPath())) {
                in.transferTo(out);
            }
        } catch (Exception e) {
            throw new RuntimeException("AES file decryption failed: " + e.getMessage(), e);
        }
    }
}

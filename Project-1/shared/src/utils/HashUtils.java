package utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static String generateSHA256(byte[] message) {
        return byteArrayToString(hashByteArray(message, "SHA-256"));
    }

    public static Boolean checkSHA256Integrity(String hash, byte[] message) {
        if (hash == null) {
            return false;
        }
        return generateSHA256(message).equals(hash);
    }

    private static byte[] hashByteArray(byte[] byteMessage, String algorithm) {
        byte[] hashedBytes = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            hashedBytes = digest.digest(byteMessage);
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Could not generate hash from String" + ex);
        }
        return hashedBytes;
    }

    private static String byteArrayToString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}

package com.example.leavePortal.config;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    private static final String SECRET_KEY = "mySecretKey12345";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public static String decrypt(String encryptedData) throws Exception {

        byte[] decodedData = Base64.getDecoder().decode(encryptedData);

        byte[] iv = new byte[16];
        byte[] encryptedBytes = new byte[decodedData.length - 16];

        System.arraycopy(decodedData, 0, iv, 0, 16);
        System.arraycopy(decodedData, 16, encryptedBytes, 0, encryptedBytes.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
}
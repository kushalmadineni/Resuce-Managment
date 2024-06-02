package com.apps.rescueconnect.helper.encryption;

import android.util.Base64;

public class AESHelper {
    private static String key = "Andro-Socio";
    public static String encryptData(String data) {
        AES aes = new AES();
        String enc = data;
        try {
            enc = aes.AESencrypt(key.getBytes("UTF-16LE"), data.getBytes("UTF-16LE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enc;
    }

    public static String decryptData(String encryptedData) {
        AES aes = new AES();
        String decData = encryptedData;
        try {
            decData = aes.AESdecrypt(key, Base64.decode(encryptedData.getBytes("UTF-16LE"), Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decData;
    }
}

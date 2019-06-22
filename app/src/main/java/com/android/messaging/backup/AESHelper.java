package com.android.messaging.backup;

import com.android.messaging.util.CommonUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESHelper {
    public static final String EXTENSION_KEY = "0102030405060708";

    private static Cipher initAESCipher(String sKey, int cipherMode) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String key1 = userId.concat(EXTENSION_KEY).substring(0, 16);

        Cipher cipher = null;
        try {
            SecretKeySpec key = new SecretKeySpec(key1.getBytes("utf-8"), "AES");

            IvParameterSpec zeroIv = new IvParameterSpec(key1.getBytes());

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(cipherMode, key, zeroIv);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return cipher;
    }

    public static File encryptFile(String key, File sourceFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        File destFile = null;
        try {
            destFile = new File(CommonUtils.getDirectory("temp/crypto"), sourceFile.getName());
            if (destFile.exists()) {
                destFile.delete();
            }
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            destFile.createNewFile();
            destFile.deleteOnExit();
            if (sourceFile.exists() && sourceFile.isFile()) {
                in = new FileInputStream(sourceFile);
                out = new FileOutputStream(destFile);
                Cipher cipher = initAESCipher(key, Cipher.ENCRYPT_MODE);
                CipherInputStream cipherInputStream = new CipherInputStream(in, cipher);
                byte[] cache = new byte[1024];
                int nRead = 0;
                while ((nRead = cipherInputStream.read(cache)) != -1) {
                    out.write(cache, 0, nRead);
                    out.flush();
                }
                cipherInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return destFile;
    }

    public static File decryptFile(String key, File sourceFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        File destFile = null;
        try {
            destFile = File.createTempFile(sourceFile.getName(), "decrypt");
                    //new File(CommonUtils.getDirectory(BackupPersistManager.BASE_PATH), sourceFile.getName());
            destFile.deleteOnExit();

            if (sourceFile.exists() && sourceFile.isFile()) {
                in = new FileInputStream(sourceFile);
                out = new FileOutputStream(destFile);
                Cipher cipher = initAESCipher(key, Cipher.DECRYPT_MODE);
                CipherOutputStream cipherOutputStream = new CipherOutputStream(out, cipher);
                byte[] buffer = new byte[1024];
                int r;
                while ((r = in.read(buffer)) >= 0) {
                    cipherOutputStream.write(buffer, 0, r);
                }
                cipherOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return destFile;
    }
}

package com.android.messaging.ui.wallpaper;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Utils {

    private static final int STREAM_OP_BUFFER_SIZE = 4096;

    static boolean saveInputStreamToFile(byte[] preData, InputStream is, File fileOut) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(fileOut);
            if (null != preData) {
                output.write(preData);
            }

            byte[] buffer = new byte[STREAM_OP_BUFFER_SIZE];
            int read;

            while ((read = is.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ignored) {
            }
        }
        return true;
    }

    static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}

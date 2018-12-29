package com.android.messaging.download;

import android.content.Context;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * @param useInternal Only uses internal cache directory when {@code true}.
     */
    public static File getCacheDirectory(String subDirectory, boolean useInternal) {
        Context context = HSApplication.getContext();
        String cacheDirPath;
        File externalCache = null;
        if (!useInternal) {
            try {
                externalCache = context.getExternalCacheDir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (externalCache != null) {
            cacheDirPath = externalCache.getAbsolutePath() + File.separator + subDirectory + File.separator;
        } else {
            cacheDirPath = context.getCacheDir().getAbsolutePath() + File.separator + subDirectory + File.separator;
        }
        File cacheDir = new File(cacheDirPath);
        if (!cacheDir.exists()) {
            if (cacheDir.mkdirs()) {
                HSLog.d("Utils.Cache", "Created cache directory: " + cacheDir.getAbsolutePath());
            } else {
                HSLog.e("Utils.Cache", "Failed to create cache directory: " + cacheDir.getAbsolutePath());
            }
        }
        return cacheDir;
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}

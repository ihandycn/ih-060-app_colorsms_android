package com.android.messaging.download;

import java.io.File;

public interface DownloadListener {

    void onStart(String url);

    void onProgress(String url, float progressValue);

    void onSuccess(String url, File file);

    void onCancel(String url);

    void onFail(String url, String failMsg);
}

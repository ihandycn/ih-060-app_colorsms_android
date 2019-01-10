package com.android.messaging.debug;


import android.text.TextUtils;

import com.ihs.commons.utils.HSLog;
import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.HeapDump;
import com.squareup.leakcanary.LeakCanary;

public class UploadLeakService extends DisplayLeakService {
    private static final String TAG = "UploadLeakService";

    @Override
    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
        super.afterDefaultHandling(heapDump, result, leakInfo);
        if (result.excludedLeak) {
            return;
        }

        HSLog.d(TAG, "send leak");
        if (result.leakFound || result.failure != null) {
            String leakDetailString = LeakCanary.leakInfo(this, heapDump, result, true);

            if (!TextUtils.isEmpty(leakDetailString)
                    && leakDetailString.contains("android.arch.lifecycle.ReportFragment has leaked")) {
                return;
            }

            SlackUtils.sendLeak(leakDetailString);
        }
    }
}

package com.android.messaging.datamodel.action;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DataModelException;
import com.android.messaging.util.Assert;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.LoggingTimer;
import com.ihs.app.framework.HSApplication;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundWorkerThread {

    private static final String TAG = LogUtil.BUGLE_DATAMODEL_TAG;
    private static final String WAKELOCK_ID = "bugle_data_model_background_thread_worker_wakelock";

    private final ActionService mHost = DataModel.get().getActionService();
    private ExecutorService mSingleThreadExecutor;
    private static BackgroundWorkerThread sInstance;

    private PowerManager.WakeLock mWakeLock;

    public BackgroundWorkerThread() {
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Queue a list of requests from action service to this worker
     */
    static void queueBackgroundWork(final List<Action> actions) {
        for (final Action action : actions) {
            getInstance().executeAction(action, 0);
        }
    }

    public static BackgroundWorkerThread getInstance() {
        if (sInstance == null) {
            synchronized (BackgroundWorkerThread.class) {
                if (sInstance == null) {
                    sInstance = new BackgroundWorkerThread();
                }
            }
        }
        return sInstance;
    }

    private void executeAction(final Action action, final int attempt) {
        acquireWakeLock();
        mSingleThreadExecutor.execute(() -> {
            try {
                doBackgroundWork(action, attempt);
            } finally {
                releaseWakeLock();
            }
        });
    }

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            synchronized (BackgroundWorkerThread.class) {
                if (mWakeLock == null) {
                    final PowerManager pm = (PowerManager) HSApplication.getContext().getSystemService(Context.POWER_SERVICE);
                    if (pm != null) {
                        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_ID);
                        mWakeLock.setReferenceCounted(true);
                    }
                }
            }
        }
        if (mWakeLock != null) {
            mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    /**
     * Local execution of background work for action on ActionService thread
     */
    private void doBackgroundWork(final Action action, final int attempt) {
        action.markBackgroundWorkStarting();
        Bundle response;
        try {
            final LoggingTimer timer = new LoggingTimer(
                    TAG, action.getClass().getSimpleName() + "#doBackgroundWork");
            timer.start();

            response = action.doBackgroundWork();

            timer.stopAndLog();
            action.markBackgroundCompletionQueued();
            mHost.handleResponseFromBackgroundWorker(action, response);
        } catch (final Exception exception) {
            final boolean retry = false;
            LogUtil.e(TAG, "Error in background worker", exception);
            if (!(exception instanceof DataModelException)) {
                Assert.fail("Unexpected error in background worker - abort");
            }
            if (retry) {
                action.markBackgroundWorkQueued();
                executeAction(action, attempt + 1);
            } else {
                action.markBackgroundCompletionQueued();
                mHost.handleFailureFromBackgroundWorker(action, exception);
            }
        }
    }
}

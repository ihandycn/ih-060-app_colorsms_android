package com.android.messaging.datamodel.action;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.LoggingTimer;
import com.google.common.annotations.VisibleForTesting;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ActionService used to perform background processing for data model
 */
public class ActionThreadImpl {
    private static final String WAKELOCK_ID = "bugle_data_model_service_wakelock_thread";
    private static final String TAG = LogUtil.BUGLE_DATAMODEL_TAG;
    private static ActionThreadImpl sInstance;
    private ExecutorService mSingleThreadExecutor;
    private PowerManager.WakeLock mWakeLock;

    private ActionThreadImpl() {
        mBackgroundWorker = DataModel.get().getBackgroundWorkerForActionService();
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    public static ActionThreadImpl getInstance() {
        if (sInstance == null) {
            synchronized (ActionThreadImpl.class) {
                if (sInstance == null) {
                    sInstance = new ActionThreadImpl();
                }
            }
        }
        return sInstance;
    }

    private void acquireWakeLock() {
        if (mWakeLock == null) {
            synchronized (ActionThreadImpl.class) {
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
     * Start action by sending intent to the service
     *
     * @param action - action to start
     */
    static void startAction(final Action action) {
        action.markStart();
        getInstance().executeActionInPool(OP_START_ACTION, action, null, null);
    }

    static void handleResponseFromBackgroundWorker(final Action action,
                                                   final Bundle response) {
        getInstance().executeActionInPool(OP_RECEIVE_BACKGROUND_RESPONSE, action, null, response);
    }

    static void handleFailureFromBackgroundWorker(final Action action,
                                                  final Exception exception) {
        getInstance().executeActionInPool(OP_RECEIVE_BACKGROUND_FAILURE, action, exception, null);
    }

    // ops
    @VisibleForTesting
    private static final int OP_START_ACTION = 200;
    @VisibleForTesting
    private static final int OP_RECEIVE_BACKGROUND_RESPONSE = 201;
    @VisibleForTesting
    private static final int OP_RECEIVE_BACKGROUND_FAILURE = 202;

    private BackgroundWorker mBackgroundWorker;

    //public void release() {
//        DataModel.get().getConnectivityUtil().unregisterForSignalStrength();
//    }

    void executeActionInPool(int opcode, Action action, Exception exception, Bundle bundle) {

        acquireWakeLock();
        mSingleThreadExecutor.execute(() -> {
            try {
                switch (opcode) {
                    case OP_START_ACTION: {
                        executeAction(action);
                        break;
                    }

                    case OP_RECEIVE_BACKGROUND_RESPONSE: {
                        processBackgroundResponse(action, bundle);
                        break;
                    }

                    case OP_RECEIVE_BACKGROUND_FAILURE: {
                        processBackgroundFailure(action);
                        HSLog.e(exception.getMessage());
                        break;
                    }

                    default:
                        throw new RuntimeException("Unrecognized opcode in ActionServiceImpl");
                }
                action.sendBackgroundActions(mBackgroundWorker);
            } finally {
                releaseWakeLock();
            }
        });
    }

    private static final long EXECUTION_TIME_WARN_LIMIT_MS = 1000; // 1 second

    /**
     * Local execution of action on ActionService thread
     */
    private void executeAction(final Action action) {
        action.markBeginExecute();

        final LoggingTimer timer = createLoggingTimer(action, "#executeActionInPool");
        timer.start();

        final Object result = action.executeAction();

        timer.stopAndLog();

        action.markEndExecute(result);
    }

    /**
     * Process response on ActionService thread
     */
    private void processBackgroundResponse(final Action action, final Bundle response) {
        final LoggingTimer timer = createLoggingTimer(action, "#processBackgroundResponse");
        timer.start();

        action.processBackgroundWorkResponse(response);

        timer.stopAndLog();
    }

    /**
     * Process failure on ActionService thread
     */
    private void processBackgroundFailure(final Action action) {
        final LoggingTimer timer = createLoggingTimer(action, "#processBackgroundFailure");
        timer.start();

        action.processBackgroundWorkFailure();

        timer.stopAndLog();
    }

    private static LoggingTimer createLoggingTimer(
            final Action action, final String methodName) {
        return new LoggingTimer(TAG, action.getClass().getSimpleName() + methodName,
                EXECUTION_TIME_WARN_LIMIT_MS);
    }
}

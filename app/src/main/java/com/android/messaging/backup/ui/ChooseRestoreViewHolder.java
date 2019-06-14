package com.android.messaging.backup.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.backup.BackupInfo;
import com.android.messaging.backup.BackupManager;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Networks;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.util.List;

import static com.ihs.app.framework.HSApplication.getContext;

public class ChooseRestoreViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private BackupManager.CloudFileListLoadListener mListLoadListener;
    private List<BackupInfo> mCloudBackups;
    private List<BackupInfo> mLocalBackups;
    private TextView mFromLocalTitle;
    private TextView mFromCloudTitle;
    private TextView mLocalSummary;
    private TextView mCloudSummary;

    ChooseRestoreViewHolder(final Context context) {
        mContext = context;
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.choose_restore_page, container, false);
        AppCompatCheckBox fromLocalCheckBox = view.findViewById(R.id.from_local);
        fromLocalCheckBox.setEnabled(false);
        AppCompatCheckBox fromCloudCheckBox = view.findViewById(R.id.from_cloud);
        fromCloudCheckBox.setEnabled(false);

        View localContainer = view.findViewById(R.id.from_local_container);
        View cloudContainer = view.findViewById(R.id.from_cloud_container);

        mFromLocalTitle = view.findViewById(R.id.from_local_title);
        mFromCloudTitle = view.findViewById(R.id.from_cloud_title);

        MessagesTextView restoreButton = view.findViewById(R.id.restore_confirm_button);
        mLocalSummary = view.findViewById(R.id.from_local_summary);
        mCloudSummary = view.findViewById(R.id.from_cloud_summary);

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        0xffa5abb1
                        , PrimaryColors.getPrimaryColor(),
                }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fromLocalCheckBox.getCompoundDrawables()[2].setTintList(colorStateList);
            fromCloudCheckBox.getCompoundDrawables()[2].setTintList(colorStateList);
        }

        final boolean[] restoreBgChanged = {false};
        restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getContext().getResources().getColor(R.color.backup_button_default_color),
                Dimensions.pxFromDp(3.3f), false));

        localContainer.setOnClickListener(v -> {
            if (!fromLocalCheckBox.isChecked()
                    && mLocalBackups != null
                    && mLocalBackups.size() > 0) {
                fromLocalCheckBox.setChecked(true);
                fromCloudCheckBox.setChecked(false);
                if (!restoreBgChanged[0]) {
                    restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                            Dimensions.pxFromDp(3.3f), true));
                    restoreBgChanged[0] = true;
                }
                BugleAnalytics.logEvent("Backup_RestorePage_Local_Click");
            }
        });

        cloudContainer.setOnClickListener(v -> {
            if (!fromCloudCheckBox.isChecked()
                    && mCloudBackups != null
                    && mCloudBackups.size() > 0) {
                fromLocalCheckBox.setChecked(false);
                fromCloudCheckBox.setChecked(true);
                if (!restoreBgChanged[0]) {
                    restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                            Dimensions.pxFromDp(3.3f), true));
                    restoreBgChanged[0] = true;
                }
                BugleAnalytics.logEvent("Backup_RestorePage_Cloud_Click");
            }
        });

        restoreButton.setOnClickListener(v -> {
            if (fromLocalCheckBox.isChecked()) {
                if (mLocalBackups != null) {
                    RestoreProcessDialog restoreProcessDialog = new RestoreProcessDialog();
                    restoreProcessDialog.setCancelable(false);
                    BackupManager.getInstance().restoreMessages(mLocalBackups.get(0),
                            new MessageRestoreListenerImpl(restoreProcessDialog, true));
                    UiUtils.showDialogFragment((Activity) mContext, restoreProcessDialog);
                    BugleAnalytics.logEvent("Backup_RestorePage_Restore_Click", true,
                            "restorefrom", "local");
                }
            } else if (fromCloudCheckBox.isChecked()) {
                if (mCloudBackups != null) {
                    if (!Networks.isNetworkAvailable(-1)) {
                        Toasts.showToast(R.string.sms_network_error);
                        return;
                    }
                    RestoreProcessDialog restoreProcessDialog = new RestoreProcessDialog();
                    restoreProcessDialog.setCancelable(false);
                    BackupManager.getInstance().restoreMessages(mCloudBackups.get(0),
                            new MessageRestoreListenerImpl(restoreProcessDialog, false));
                    UiUtils.showDialogFragment((Activity) mContext, restoreProcessDialog);
                    BugleAnalytics.logEvent("Backup_RestorePage_Restore_Click", true,
                            "restorefrom", "cloud");
                }
            }
        });

        onLoginSuccess();
        reloadBackupData();

        return view;
    }

    private class MessageRestoreListenerImpl implements BackupManager.MessageRestoreListener {
        //[0] more than min time,[1] backup complete
        boolean[] backupCondition = {false, false};
        RestoreProcessDialog mRestoreProcessDialog;
        boolean mIsLocal;

        private MessageRestoreListenerImpl(@NonNull RestoreProcessDialog restoreProcessDialog, boolean isLocal) {
            this.mRestoreProcessDialog = restoreProcessDialog;
            this.mIsLocal = isLocal;
        }

        @Override
        public void onDownloadStart() {
            Threads.postOnMainThread(() -> {
                mRestoreProcessDialog.setStateText(getContext().getString(R.string.restore_downloading));
                mRestoreProcessDialog.hideProgressBar(true);
            });
        }

        @Override
        public void onDownloadSuccess() {

        }

        @Override
        public void onDownloadFailed() {
            Threads.postOnMainThread(() -> {
                mRestoreProcessDialog.dismissAllowingStateLoss();
                Toasts.showToast(R.string.restore_fail);
            });
        }

        @Override
        public void onRestoreStart(int messageCount) {
            Threads.postOnMainThread(() -> {
                mRestoreProcessDialog.hideProgressBar(false);
                mRestoreProcessDialog.setStateText(getContext().getString(R.string.restore_process_hint));
                mRestoreProcessDialog.setTotal(messageCount);
                mRestoreProcessDialog.startProgress();
            });

            Threads.postOnMainThreadDelayed(() -> {
                backupCondition[0] = true;
                if (backupCondition[1]) {
                    onRestoreSuccess();
                }
            }, RestoreProcessDialog.MIN_PROGRESS_TIME);
        }

        @Override
        public void onRestoreUpdate(int restoredCount) {
            Threads.postOnMainThread(() -> {
                mRestoreProcessDialog.setStateText(getContext().getString(R.string.restore_process_hint));
                mRestoreProcessDialog.setProgress(restoredCount);
            });
        }

        @Override
        public void onRestoreSuccess() {
            backupCondition[1] = true;
            if (backupCondition[0]) {
                Threads.postOnMainThread(() -> {
                    mRestoreProcessDialog.dismissAllowingStateLoss();
                    backupCondition[0] = false;
                    backupCondition[1] = false;
                    Toasts.showToast(R.string.restore_success);
                    BugleAnalytics.logEvent("Backup_RestorePage_Restore_Success", true,
                            "restorefrom", mIsLocal ? "local" : "cloud");
                });
            }
        }

        @Override
        public void onRestoreFailed() {
            Threads.postOnMainThread(() -> {
                mRestoreProcessDialog.dismissAllowingStateLoss();
                Toasts.showToast(R.string.restore_fail);
            });
        }
    }

    void onLoginSuccess() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mListLoadListener = new BackupManager.CloudFileListLoadListener() {
                @Override
                public void onLoadSuccess(List<BackupInfo> cloudList) {
                    if (cloudList != null && cloudList.size() > 0) {
                        mCloudBackups = cloudList;
                        mCloudSummary.setText(cloudList.get(0).getBackupTimeStr());
                        mFromCloudTitle.setTextColor(getContext().getResources()
                                .getColor(R.color.text_primary_color));
                    } else {
                        mFromCloudTitle.setTextColor(getContext().getResources()
                                .getColor(R.color.restore_summary_default_color));
                    }
                }

                @Override
                public void onLoadCancel() {
                    mCloudSummary.setText(R.string.restore_default_summary);
                    mFromCloudTitle.setTextColor(getContext().getResources()
                            .getColor(R.color.restore_summary_default_color));
                }
            };
            BackupManager.getInstance().getCloudBackupFilesName(mListLoadListener);
        }
    }

    void onLoginFailed() {

    }

    void reloadBackupData() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            BackupManager.getInstance().getCloudBackupFilesName(mListLoadListener);
        }
        List<BackupInfo> localBackups = BackupManager.getInstance().getLocalBackupFilesInfo();
        if (localBackups == null || localBackups.size() == 0) {
            mLocalSummary.setText(R.string.restore_default_summary);
            mFromLocalTitle.setTextColor(getContext().getResources()
                    .getColor(R.color.restore_summary_default_color));
        } else {
            mLocalSummary.setText(localBackups.get(0).getBackupTimeStr());
            mLocalBackups = localBackups;
            mFromLocalTitle.setTextColor(getContext().getResources()
                    .getColor(R.color.text_primary_color));
        }
    }

    @Override
    protected void setHasOptionsMenu() {

    }

    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.restore_tab);
    }

    @Override
    public void onPageSelected() {

    }
}

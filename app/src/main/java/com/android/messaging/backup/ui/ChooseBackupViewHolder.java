package com.android.messaging.backup.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.backup.BackupInfo;
import com.android.messaging.backup.BackupManager;
import com.android.messaging.backup.BackupPersistManager;
import com.android.messaging.ui.BasePagerViewHolder;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.CommonUtils;
import com.android.messaging.util.UiUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.util.List;

import static com.ihs.app.framework.HSApplication.getContext;

public class ChooseBackupViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    public static final String PREF_KEY_BACKUP_SUCCESS_FOR_EVENT = "pref_key_backup_success_for_event";
    private static final String PREF_KEY_BACKUP_TIP_SHOWN = "pref_key_backup_tip_show";
    private Context mContext;
    private CheckBox mLocalCheckBox;
    private CheckBox mCloudCheckBox;
    private TextView mCloudSummary;
    private boolean mHasBackup;

    ChooseBackupViewHolder(final Context context) {
        mContext = context;
        loadBackupInfo();
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.choose_backup_page, container, false);

        ((TextView) view.findViewById(R.id.backup_local_summary))
                .setText(CommonUtils.getDirectory(BackupPersistManager.BASE_PATH).getAbsolutePath());

        mLocalCheckBox = view.findViewById(R.id.backup_local);
        mCloudCheckBox = view.findViewById(R.id.backup_cloud);
        mLocalCheckBox.setChecked(true);

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
            mLocalCheckBox.getCompoundDrawables()[2].setTintList(colorStateList);
            mCloudCheckBox.getCompoundDrawables()[2].setTintList(colorStateList);
        }

        MessagesTextView backupButton = view.findViewById(R.id.backup_confirm_button);
        backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f), true));

        mLocalCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && !mCloudCheckBox.isChecked()) {
                backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        getContext().getResources().getColor(R.color.backup_button_default_color),
                        Dimensions.pxFromDp(3.3f), false));
            } else {
                backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        PrimaryColors.getPrimaryColor(),
                        Dimensions.pxFromDp(3.3f), true));
            }
        });

        mCloudCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && !mLocalCheckBox.isChecked()) {
                backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        getContext().getResources().getColor(R.color.backup_button_default_color),
                        Dimensions.pxFromDp(3.3f), false));
            } else {
                backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        PrimaryColors.getPrimaryColor(),
                        Dimensions.pxFromDp(3.3f), true));
            }

            if (isChecked && FirebaseAuth.getInstance().getCurrentUser() == null) {
                if (mContext instanceof BackupRestoreActivity) {
                    ((BackupRestoreActivity) mContext).login();
                }
            }
        });

        backupButton.setOnClickListener(v -> {
            if (mHasBackup) {
                showTipsDialog();
            } else {
                backupAndShowDialog();
            }
            String backupType = "empty";
            if (mLocalCheckBox.isChecked() && mCloudCheckBox.isChecked()) {
                backupType = "both";
            } else if (mLocalCheckBox.isChecked()) {
                backupType = "local";
            } else if (mCloudCheckBox.isChecked()) {
                backupType = "cloud";
            }
            BugleAnalytics.logEvent("Backup_BackupPage_Backup_Click", true, "type", backupType);
        });

        mCloudSummary = view.findViewById(R.id.backup_cloud_summary);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mCloudSummary.setText(user.getEmail());
        } else {
            mCloudSummary.setText(R.string.backup_no_account);
        }

        return view;
    }

    private void showTipsDialog() {
        BackupTipsDialog tipsDialog = new BackupTipsDialog();
        tipsDialog.setOnNegativeButtonClickListener(v -> {
            if (mContext instanceof BackupRestoreActivity) {
                ((BackupRestoreActivity) mContext).jumpToRestorePage();
            }
            tipsDialog.dismissAllowingStateLoss();
            BugleAnalytics.logEvent("Backup_BackupPage_BackupAlert_Restore_Click");
        });

        tipsDialog.setOnPositiveButtonClickListener(v -> {
            tipsDialog.dismissAllowingStateLoss();
            backupAndShowDialog();
            BugleAnalytics.logEvent("Backup_BackupPage_BackupAlert_Backup_Click");
        });

        UiUtils.showDialogFragment((Activity) mContext, tipsDialog);
        BugleAnalytics.logEvent("Backup_BackupPage_BackupAlert_Show");
    }

    private void backupAndShowDialog() {
        BackupProcessDialog backupDialog = new BackupProcessDialog();
        backupDialog.setCancelable(false);
        //[0] more than min time,[1] backup complete
        boolean[] backupCondition = {false, false};

        BackupManager.MessageBackupListener listener = new BackupManager.MessageBackupListener() {
            @Override
            public void onBackupStart() {
                Threads.postOnMainThread(() -> {
                    UiUtils.showDialogFragment((Activity) mContext, backupDialog);
                    backupDialog.setStateString(getContext().getString(R.string.backup_state_scanning));
                    backupDialog.hideProgressBar(true);
                });
            }

            @Override
            public void onSyncAndLoadSuccess(int messageCount) {
                Threads.postOnMainThread(() -> {
                    backupDialog.setTotal(messageCount);
                    backupDialog.hideProgressBar(false);
                    backupDialog.startProgress();
                    backupDialog.setProgress(0);
                    backupDialog.setStateString(getContext().getString(R.string.local_backup_process_hint));
                });
                Threads.postOnMainThreadDelayed(() -> {
                    backupCondition[0] = true;
                    if (backupCondition[1]) {
                        onBackupSuccess();
                    }
                }, BackupProcessDialog.MIN_PROGRESS_TIME);
            }

            @Override
            public void onBackupUpdate(int backedUpCount) {
                Threads.postOnMainThread(() -> backupDialog.setProgress(backedUpCount));
            }


            @Override
            public void onBackupFailed() {
                Threads.postOnMainThread(() -> {
                    backupDialog.dismiss();
                    Toasts.showToast(R.string.backup_failed_toast);
                });
            }

            @Override
            public void onUploadStart() {
                Threads.postOnMainThread(() -> {
                    backupDialog.changeLottie(false);
                    backupDialog.setStateString(getContext().getResources().getString(R.string.cloud_backup_process_hint));
                    backupDialog.setProgress(0);
                    backupDialog.hideProgressBar(true);
                });
            }

            @Override
            public void onUploadSuccess() {

            }

            @Override
            public void onUploadFailed() {
                Threads.postOnMainThread(() -> {
                    backupDialog.dismiss();
                    Toasts.showToast(R.string.backup_failed_toast);
                });
            }

            @Override
            public void onBackupSuccess() {
                backupCondition[1] = true;
                if (backupCondition[0]) {
                    Threads.postOnMainThread(() -> {
                        backupDialog.dismiss();
                        if (mContext != null && mContext instanceof BackupRestoreActivity) {
                            ((BackupRestoreActivity) mContext).onBackupDataChanged();
                        }
                        if (HSConfig.optBoolean(false, "Application", "BackupRestore", "FreeUpOldmsg")) {
                            MessageFreeUpDialog freeUpDialog = new MessageFreeUpDialog();
                            freeUpDialog.setOnPositiveButtonClickListener(v -> {
                                //freeUpDialog.dismissAllowingStateLoss();
                                freeUpAndShowDialog();
                                BugleAnalytics.logEvent("Backup_Freeupmsg_Alert_Click");
                            });
                            UiUtils.showDialogFragment((Activity) mContext, freeUpDialog);
                            BugleAnalytics.logEvent("Backup_Freeupmsg_Alert_Show");
                        }
                    });
                }

                Preferences.getDefault().putBoolean(PREF_KEY_BACKUP_SUCCESS_FOR_EVENT, true);
                String backupType = "empty";
                if (mLocalCheckBox.isChecked() && mCloudCheckBox.isChecked()) {
                    backupType = "both";
                } else if (mLocalCheckBox.isChecked()) {
                    backupType = "local";
                } else if (mCloudCheckBox.isChecked()) {
                    backupType = "cloud";
                }
                BugleAnalytics.logEvent("Backup_BackupPage_Backup_Success", true, "type", backupType);
            }
        };
        if (mLocalCheckBox.isChecked() && mCloudCheckBox.isChecked()) {
            BackupManager.getInstance().backupMessages(BackupInfo.BOTH, listener);
        } else if (mLocalCheckBox.isChecked()) {
            BackupManager.getInstance().backupMessages(BackupInfo.LOCAL, listener);
        } else if (mCloudCheckBox.isChecked()) {
            BackupManager.getInstance().backupMessages(BackupInfo.CLOUD, listener);
        }
    }

    private void freeUpAndShowDialog() {
        MessageFreeUpProcessDialog dialog = new MessageFreeUpProcessDialog();
        dialog.setCancelable(false);
        // dismiss[0] more than 3s, dismiss[1] delete complete
        final boolean[] dismissCondition = {false, false};
        Threads.postOnMainThreadDelayed(() -> {
            dismissCondition[0] = true;
            if (dismissCondition[1]) {
                dialog.dismissAllowingStateLoss();
            }
        }, 3000);

        BackupManager.getInstance().deleteLocalMessages(30, new BackupManager.MessageDeleteListener() {
            @Override
            public void onDeleteStart() {

            }

            @Override
            public void onDeleteFailed() {
                if (dialog != null) {
                    dismissCondition[1] = true;
                    if (dismissCondition[0]) {
                        dialog.dismissAllowingStateLoss();
                    }
                }
            }

            @Override
            public void onDeleteSuccess() {
                if (dialog != null) {
                    dismissCondition[1] = true;
                    if (dismissCondition[0]) {
                        dialog.dismissAllowingStateLoss();
                    }
                }
                Preferences.getDefault().doOnce(() -> {
                    UiUtils.showDialogFragment((Activity) mContext,
                            new BackupTipsBeforeUninstallingDialog());
                    BugleAnalytics.logEvent("Backup_Freeupmsg_Tips_Show");
                }, PREF_KEY_BACKUP_TIP_SHOWN);
                BugleAnalytics.logEvent("Backup_Freeupmsg_Success");
            }
        });

        UiUtils.showDialogFragment((Activity) mContext, dialog);
    }

    private void loadBackupInfo() {
        List<BackupInfo> localBackups = BackupManager.getInstance().getLocalBackupFilesInfo();
        if (localBackups == null || localBackups.size() == 0) {
            mHasBackup = true;
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            BackupManager.CloudFileListLoadListener mListLoadListener = new BackupManager.CloudFileListLoadListener() {
                @Override
                public void onLoadSuccess(List<BackupInfo> cloudList) {
                    if (cloudList != null && cloudList.size() > 0) {
                        mHasBackup = true;
                    }
                }

                @Override
                public void onLoadCancel() {

                }
            };
            BackupManager.getInstance().getCloudBackupFilesName(mListLoadListener);
        }
    }

    @Override
    protected void setHasOptionsMenu() {

    }

    @Override
    public CharSequence getPageTitle(Context context) {
        return context.getString(R.string.backup_tab);
    }

    @Override
    public void onPageSelected() {

    }

    void onLoginSuccess() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mCloudSummary.setText(user.getEmail());
        } else {
            mCloudSummary.setText(R.string.backup_no_account);
        }
        loadBackupInfo();
    }

    void onLoginFailed() {

    }
}

package com.android.messaging.backup.ui;

import android.app.Activity;
import android.content.Context;
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
import com.android.messaging.util.UiUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.util.List;

import static com.ihs.app.framework.HSApplication.getContext;

public class ChooseRestoreViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private BackupManager.CloudFileListLoadListener mListLoadListener;
    private List<BackupInfo> mCloudBackups;
    private List<BackupInfo> mLocalBackups;
    private TextView mLocalSummary;
    private TextView mCloudSummary;

    public ChooseRestoreViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.choose_restore_page, container, false);
        AppCompatCheckBox fromLocalCheckBox = view.findViewById(R.id.from_local);
        AppCompatCheckBox fromCloudCheckBox = view.findViewById(R.id.from_cloud);
        MessagesTextView restoreButton = view.findViewById(R.id.restore_confirm_button);
        mLocalSummary = view.findViewById(R.id.from_local_summary);
        mCloudSummary = view.findViewById(R.id.from_cloud_summary);
        restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getContext().getResources().getColor(R.color.backup_button_default_color),
                Dimensions.pxFromDp(3.3f), false));
        fromLocalCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                    Dimensions.pxFromDp(3.3f), true));
            if (isChecked && fromCloudCheckBox.isChecked()) {
                fromCloudCheckBox.setChecked(false);
            } else if (!isChecked && !fromCloudCheckBox.isChecked()) {
                fromLocalCheckBox.setChecked(true);
            }
        });

        fromCloudCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            restoreButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                    Dimensions.pxFromDp(3.3f), true));
            if (isChecked && fromLocalCheckBox.isChecked()) {
                fromLocalCheckBox.setChecked(false);
            } else if (!isChecked && !fromLocalCheckBox.isChecked()) {
                fromCloudCheckBox.setChecked(true);
            }
        });

        RestoreProcessDialog restoreProcessDialog = new RestoreProcessDialog();
        restoreProcessDialog.setCancelable(false);
        BackupManager.MessageRestoreListener listener = new BackupManager.MessageRestoreListener() {
            @Override
            public void onDownloadStart() {
                Threads.postOnMainThread(() -> {
                    restoreProcessDialog.setStateText(getContext().getString(R.string.restore_downloading));
                    restoreProcessDialog.hideProgressBar(true);
                });
            }

            @Override
            public void onDownloadSuccess() {

            }

            @Override
            public void onDownloadFailed() {
                Threads.postOnMainThread(() -> {
                    restoreProcessDialog.dismissAllowingStateLoss();
                    Toasts.showToast(R.string.restore_fail);
                });
            }

            @Override
            public void onRestoreStart(int messageCount) {
                Threads.postOnMainThread(() -> {
                    restoreProcessDialog.hideProgressBar(false);
                    restoreProcessDialog.setStateText(getContext().getString(R.string.restore_process_hint));
                    restoreProcessDialog.setTotal(messageCount);
                });
            }

            @Override
            public void onRestoreUpdate(int restoredCount) {
                Threads.postOnMainThread(() -> {
                    restoreProcessDialog.setStateText(getContext().getString(R.string.restore_process_hint));
                    restoreProcessDialog.setProgress(restoredCount);
                });
            }

            @Override
            public void onRestoreSuccess() {
                Threads.postOnMainThread(() -> {
                    restoreProcessDialog.dismissAllowingStateLoss();
                    Toasts.showToast(R.string.restore_success);
                });
            }

            @Override
            public void onRestoreFailed() {
                Threads.postOnMainThread(() -> {
                    restoreProcessDialog.dismissAllowingStateLoss();
                    Toasts.showToast(R.string.restore_fail);
                });
            }
        };

        restoreButton.setOnClickListener(v -> {
            if (fromLocalCheckBox.isChecked()) {
                if (mLocalBackups != null) {
                    BackupManager.getInstance().restoreMessages(mLocalBackups.get(0), listener);
                    UiUtils.showDialogFragment((Activity) mContext, restoreProcessDialog);
                }
            } else if (fromCloudCheckBox.isChecked()) {
                if (mCloudBackups != null) {
                    BackupManager.getInstance().restoreMessages(mCloudBackups.get(0), listener);
                    UiUtils.showDialogFragment((Activity) mContext, restoreProcessDialog);
                }
            }
        });

        onLoginSuccess();
        reloadBackupData();

        return view;
    }

    void onLoginSuccess() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mListLoadListener = new BackupManager.CloudFileListLoadListener() {
                @Override
                public void onLoadSuccess(List<BackupInfo> cloudList) {
                    if (cloudList != null && cloudList.size() > 0) {
                        mCloudBackups = cloudList;
                        mCloudSummary.setText(cloudList.get(0).getBackupTimeStr());
                    }
                }

                @Override
                public void onLoadCancel() {
                    mCloudSummary.setText(R.string.restore_default_summary);
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
        } else {
            mLocalSummary.setText(localBackups.get(0).getBackupTimeStr());
            mLocalBackups = localBackups;
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

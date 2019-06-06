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
import com.google.firebase.auth.FirebaseUser;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import static com.ihs.app.framework.HSApplication.getContext;

public class ChooseBackupViewHolder extends BasePagerViewHolder implements CustomPagerViewHolder {
    private Context mContext;
    private boolean mIsLoggedIn = false;

    public ChooseBackupViewHolder(final Context context) {
        mContext = context;
    }

    @Override
    protected View createView(ViewGroup container) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.choose_backup_page, container, false);

        AppCompatCheckBox localCheckBox = view.findViewById(R.id.backup_local);
        AppCompatCheckBox cloudCheckBox = view.findViewById(R.id.backup_cloud);
        localCheckBox.setChecked(true);
        MessagesTextView backupButton = view.findViewById(R.id.backup_confirm_button);
        backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                Dimensions.pxFromDp(3.3f), true));
        localCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && !cloudCheckBox.isChecked()) {
                backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getContext().getResources().getColor(R.color.backup_button_default_color),
                        Dimensions.pxFromDp(3.3f), false));
            } else {
                backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                        Dimensions.pxFromDp(3.3f), true));
            }
        });
        cloudCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && !localCheckBox.isChecked()) {
                backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getContext().getResources().getColor(R.color.backup_button_default_color),
                        Dimensions.pxFromDp(3.3f), false));
            } else {
                backupButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(),
                        Dimensions.pxFromDp(3.3f), true));
            }
        });

        BackupProcessDialog backupDialog = new BackupProcessDialog();
        backupDialog.setCancelable(false);

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
                    backupDialog.setProgress(0);
                    backupDialog.setStateString(getContext().getString(R.string.local_backup_process_hint));
                });
            }

            @Override
            public void onBackupUpdate(int backedUpCount) {
                Threads.postOnMainThread(() -> {
                    backupDialog.setProgress(backedUpCount);
                });
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
                Threads.postOnMainThread(() -> {
                    backupDialog.dismiss();
                    if (mContext != null && mContext instanceof BackupRestoreActivity) {
                        ((BackupRestoreActivity) mContext).onBackupDataChanged();
                    }
                    MessageFreeUpDialog freeUpDialog = new MessageFreeUpDialog();
                    UiUtils.showDialogFragment((Activity) mContext, freeUpDialog);
                });
            }
        };

        backupButton.setOnClickListener(v -> {
            if (localCheckBox.isChecked() && cloudCheckBox.isChecked()) {
                BackupManager.getInstance().backupMessages(BackupInfo.BOTH, listener);
            } else if (localCheckBox.isChecked()) {
                BackupManager.getInstance().backupMessages(BackupInfo.LOCAL, listener);
            } else if (cloudCheckBox.isChecked()) {
                BackupManager.getInstance().backupMessages(BackupInfo.CLOUD, listener);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ((TextView) view.findViewById(R.id.backup_cloud_summary)).setText(user.getEmail());
        } else {
            ((TextView) view.findViewById(R.id.backup_cloud_summary)).setText(R.string.backup_no_account);
        }

        return view;
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

    public void onLoginSuccess() {

    }

    public void onLoginFailed() {

    }
}

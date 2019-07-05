package com.android.messaging.backup.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.backup.BackupAutopilotUtils;
import com.android.messaging.mmslib.SqliteWrapper;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

public class BackupGuideDialogActivity extends HSAppCompatActivity {
    public static final String PREF_KEY_BACKUP_FULL_GUIDE_SHOWN = "pref_key_backup_full_guide_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_guide_dialog);

        TextView userCount = findViewById(R.id.backup_guide_dialog_second_content);
        userCount.setText(getString(R.string.backup_guide_dialog_second_content, 30));

        View backupDialogButton = findViewById(R.id.backup_guide_dialog_btn);
        backupDialogButton.setBackground(BackgroundDrawables.createBackgroundDrawable(PrimaryColors.getPrimaryColor(), Dimensions.pxFromDp(3.3f), true));
        backupDialogButton.setOnClickListener(v -> {
            BackupRestoreActivity.startBackupRestoreActivity(this, BackupRestoreActivity.ENTRANCE_FULL_GUIDE);
            overridePendingTransition(R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
            finish();
            BugleAnalytics.logEvent("BackupFullGuide_Click", true);
            BackupAutopilotUtils.logFullGuideClick();
        });

        ImageView backupDialogCloseButton = findViewById(R.id.backup_guide_dialog_close);
        backupDialogCloseButton.setBackground(BackgroundDrawables.createTransparentBackgroundDrawable(
                0x33000000, Dimensions.pxFromDp(14)));
        backupDialogCloseButton.setOnClickListener(v -> finish());

        Threads.postOnThreadPoolExecutor(() -> {
            final Context context = HSApplication.getContext();
            Cursor cursor = SqliteWrapper.query(
                    context,
                    context.getContentResolver(),
                    Telephony.Sms.CONTENT_URI,
                    new String[]{"COUNT(*)"},
                    MmsUtils.getSmsTypeSelectionSql(),
                    null,
                    Telephony.Sms.DATE + " DESC");

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    Threads.postOnMainThread(() -> userCount.setText(getString(R.string.backup_guide_dialog_second_content, count)));
                }
                cursor.close();
            }
        });

        Preferences.getDefault().putBoolean(PREF_KEY_BACKUP_FULL_GUIDE_SHOWN, true);
        BugleAnalytics.logEvent("BackupFullGuide_Show", true);
        BackupAutopilotUtils.logFullGuideShow();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0, R.anim.app_lock_fade_out_long);
    }
}

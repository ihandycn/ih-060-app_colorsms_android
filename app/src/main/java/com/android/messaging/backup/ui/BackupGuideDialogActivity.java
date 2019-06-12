package com.android.messaging.backup.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.ui.customize.PrimaryColors;
import com.android.messaging.util.BugleAnalytics;
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
            finish();
            BugleAnalytics.logEvent("BackupFullGuide_Click");
        });

        ImageView backupDialogCloseButton = findViewById(R.id.backup_guide_dialog_close);
        backupDialogCloseButton.setBackground(BackgroundDrawables.createTransparentBackgroundDrawable(
                0x33000000, Dimensions.pxFromDp(14)));
        backupDialogCloseButton.setOnClickListener(v -> finish());

        Threads.postOnThreadPoolExecutor(() -> {
            DatabaseWrapper db = DataModel.get().getDatabaseWithoutMainCheck();
            Cursor cursor = db.query(DatabaseHelper.MESSAGES_TABLE, new String[]{"COUNT(*)"},
                    null, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    Threads.postOnMainThread(() -> userCount.setText(getString(R.string.backup_guide_dialog_second_content, count)));
                }
                cursor.close();
            }
        });

        Preferences.getDefault().putBoolean(PREF_KEY_BACKUP_FULL_GUIDE_SHOWN, true);
        BugleAnalytics.logEvent("BackupFullGuide_Show");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //startActivity(new Intent(this, ConversationListActivity.class));
        finish();
    }
}

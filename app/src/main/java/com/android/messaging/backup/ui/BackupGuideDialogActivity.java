package com.android.messaging.backup.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.view.MessagesTextView;
import com.android.messaging.util.BuglePrefs;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.view.RoundImageView;

import static com.android.messaging.ui.conversationlist.ConversationListFragment.PREF_KEY_BACKUP_SHOW_BANNER_GUIDE;

public class BackupGuideDialogActivity extends HSAppCompatActivity {
    private BuglePrefs mBackupBannerGuideHidePrefs = Factory.get().getCustomizePrefs();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_guide_dialog);
        MessagesTextView backupDialogButton = findViewById(R.id.backup_guide_dialog_btn);
        backupDialogButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.primary_color), Dimensions.pxFromDp(3.3f), true));
        backupDialogButton.setOnClickListener(v -> {
            mBackupBannerGuideHidePrefs.putBoolean(PREF_KEY_BACKUP_SHOW_BANNER_GUIDE, false);
            final Intent intent = new Intent(this, BackupRestoreActivity.class);
            this.startActivity(intent);
        });
        ImageView backupDialogCloseButton = findViewById(R.id.backup_guide_dialog_close);
        backupDialogCloseButton.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.backup_guide_dialog_close_button_color), Dimensions.pxFromDp(40f), true));
        backupDialogCloseButton.setOnClickListener(v -> {
            final Intent intent = new Intent(this, ConversationListActivity.class);
            this.startActivity(intent);
            finish();
        });
        RoundImageView backupGuideDialogHomepage = findViewById(R.id.backup_guide_dialog_homepage);
        ViewGroup.LayoutParams layoutParams = backupGuideDialogHomepage.getLayoutParams();
        layoutParams.width = (int) (Dimensions.getPhoneWidth(this) * 0.82);
        layoutParams.height = layoutParams.width * 15 / 22;
        backupGuideDialogHomepage.setLayoutParams(layoutParams);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, ConversationListActivity.class));
        finish();
    }
}

package com.android.messaging.backup.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.CustomHeaderViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.UiUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

import java.util.Collections;


public class BackupRestoreActivity extends BaseActivity {
    public static final String PREF_KEY_BACKUP_ACTIVITY_SHOWN = "pref_key_backup_activity_show";

    public static final String ENTRANCE_TOP_GUIDE = "top_guide";
    public static final String ENTRANCE_FULL_GUIDE = "full_guide";
    public static final String ENTRANCE_MENU = "menu";

    private static final int RC_SIGN_IN = 12;
    private ChooseBackupViewHolder mBackUpViewHolder;
    private ChooseRestoreViewHolder mRestoreViewHolder;
    private CustomHeaderViewPager mCustomHeaderViewPager;

    public static void startBackupRestoreActivity(Context context, @NonNull String entrance) {
        Intent intent = new Intent(context, BackupRestoreActivity.class);
        intent.putExtra("from", entrance);
        Navigations.startActivitySafely(context, intent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.backup_restore_activity);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");

        TextView title = mToolbar.findViewById(R.id.toolbar_title);
        title.setText(getString(R.string.menu_backup_restore_toolbar_title));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initPager(this);

        UiUtils.setTitleBarBackground(mToolbar, this);

        Preferences.getDefault().putBoolean(PREF_KEY_BACKUP_ACTIVITY_SHOWN, true);

        BugleAnalytics.logEvent("Backup_BackupPage_Show", true,
                "from", getIntent().getStringExtra("from"));
    }

    private void initPager(Context context) {
        mBackUpViewHolder = new ChooseBackupViewHolder(context);
        mRestoreViewHolder = new ChooseRestoreViewHolder(context);
        final CustomPagerViewHolder[] viewHolders = {mBackUpViewHolder, mRestoreViewHolder};
        mCustomHeaderViewPager = findViewById(R.id.backup_header_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        mCustomHeaderViewPager.setCurrentItem(0);
        final boolean[] eventLogged = {false};
        mCustomHeaderViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1 && !eventLogged[0]) {
                    BugleAnalytics.logEvent("Backup_RestorePage_Show", true);
                    eventLogged[0] = true;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void login() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Collections.singletonList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
        }
    }

    public void onBackupDataChanged() {
        mRestoreViewHolder.reloadBackupData();
    }

    public void jumpToRestorePage() {
        if (mCustomHeaderViewPager != null && mCustomHeaderViewPager.getChildCount() > 1) {
            mCustomHeaderViewPager.setCurrentItem(1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toasts.showToast(R.string.firebase_login_succeed);
                mBackUpViewHolder.onLoginSuccess();
                mRestoreViewHolder.onLoginSuccess();
            } else {
                Toasts.showToast(R.string.firebase_login_failed);
                mBackUpViewHolder.onLoginFailed();
                mRestoreViewHolder.onLoginFailed();
            }
        }
    }
}

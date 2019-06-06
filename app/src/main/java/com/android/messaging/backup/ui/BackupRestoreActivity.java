package com.android.messaging.backup.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.messaging.BaseActivity;
import com.android.messaging.R;
import com.android.messaging.ui.CustomHeaderViewPager;
import com.android.messaging.ui.CustomPagerViewHolder;
import com.android.messaging.ui.CustomViewPager;
import com.android.messaging.util.UiUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.superapps.util.Toasts;

import java.util.Collections;


public class BackupRestoreActivity extends BaseActivity {
    private static final int RC_SIGN_IN = 12;
    private ChooseBackupViewHolder mBackUpViewHolder;
    private ChooseRestoreViewHolder mRestoreViewHolder;

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
    }

    private void initPager(Context context) {
        mBackUpViewHolder = new ChooseBackupViewHolder(context);
        mRestoreViewHolder = new ChooseRestoreViewHolder(context);
        final CustomPagerViewHolder[] viewHolders = {mBackUpViewHolder, mRestoreViewHolder};
        CustomHeaderViewPager mCustomHeaderViewPager = findViewById(R.id.backup_header_pager);
        mCustomHeaderViewPager.setViewHolders(viewHolders);
        mCustomHeaderViewPager.setViewPagerTabHeight(CustomViewPager.DEFAULT_TAB_STRIP_SIZE);
        mCustomHeaderViewPager.setBackgroundColor(getResources().getColor(R.color.contact_picker_background));
        mCustomHeaderViewPager.setCurrentItem(0);
        mCustomHeaderViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

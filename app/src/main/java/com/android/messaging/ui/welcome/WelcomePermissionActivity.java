package com.android.messaging.ui.welcome;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.OsUtil;
import com.ihs.commons.config.HSConfig;

public class WelcomePermissionActivity extends AppCompatActivity {
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 2;
    private boolean mShieldBackKey = false;
    private String[] mRequiredPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_permission);

        findViewById(R.id.welcome_permission_button).setOnClickListener(v -> {
            if (mRequiredPermissions.length != 0) {
                requestPermissions(mRequiredPermissions, REQUIRED_PERMISSIONS_REQUEST_CODE);
                BugleAnalytics.logEvent("SMS_Start_PermissionPage_BtnClick", true);
            } else {
                redirect();
            }
        });

        mShieldBackKey = HSConfig.optBoolean(false, "Application", "StartPageAllowBack");
    }

    @Override
    public void onStart() {
        super.onStart();

        mRequiredPermissions = OsUtil.getMissingRequiredPermissions();
        refreshPermissionList(mRequiredPermissions);
        BugleAnalytics.logEvent("SMS_Start_PermissionPage_Show", true);
    }

    @Override
    public void onBackPressed() {
        if (!mShieldBackKey) {
            super.onBackPressed();
            BugleAnalytics.logEvent("SMS_Start_PermissionPage_Back", true);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String permissions[], final int[] grantResults) {
        if (requestCode == REQUIRED_PERMISSIONS_REQUEST_CODE) {
            if (OsUtil.hasRequiredPermissions()) {
                redirect();
            } else {
                for (String permission : mRequiredPermissions) {
                    switch (permission) {
                        case Manifest.permission.READ_SMS:
                            setWarningIconVisibility(R.id.welcome_permission_sms_warning,
                                    OsUtil.hasPermission(Manifest.permission.READ_SMS));
                            break;

                        case Manifest.permission.READ_CONTACTS:
                            setWarningIconVisibility(R.id.welcome_permission_contacts_warning,
                                    OsUtil.hasPermission(Manifest.permission.READ_CONTACTS));
                            break;

                        case Manifest.permission.READ_PHONE_STATE:
                            setWarningIconVisibility(R.id.welcome_permission_phone_warning,
                                    OsUtil.hasPermission(Manifest.permission.READ_PHONE_STATE));
                            break;

                        default:
                            break;
                    }
                }
                Toast.makeText(this, R.string.welcome_permission_failed_toast,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void refreshPermissionList(String[] permissions){
        LinearLayout container = findViewById(R.id.welcome_permission_container);
        container.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (String permission : permissions) {
            switch (permission) {
                case Manifest.permission.READ_SMS:
                    inflater.inflate(R.layout.item_welcome_permission_sms, container, true);
                    break;

                case Manifest.permission.READ_CONTACTS:
                    inflater.inflate(R.layout.item_welcome_permission_contacts, container, true);
                    break;

                case Manifest.permission.READ_PHONE_STATE:
                    inflater.inflate(R.layout.item_welcome_permission_phone, container, true);
                    break;

                default:
                    break;
            }
        }
    }

    private void redirect() {
        UIIntents.get().launchConversationListActivity(this);
        BugleAnalytics.logEvent("SMS_Start_PermissionPage_Permission_Success", true);
        finish();
    }

    private void setWarningIconVisibility(int id, boolean invisible) {
        View view = findViewById(id);
        if (view != null) {
            view.setVisibility(invisible ? View.INVISIBLE : View.VISIBLE);
        }
    }
}

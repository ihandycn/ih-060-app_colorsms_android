package com.android.messaging.ui.welcome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleAnalytics;
import com.android.messaging.util.OsUtil;

public class WelcomePermissionActivity extends AppCompatActivity {
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_permission);

        findViewById(R.id.welcome_permission_button).setOnClickListener(v -> {
            String[] permissions = OsUtil.getMissingRequiredPermissions();
            if (permissions.length != 0) {
                requestPermissions(permissions, REQUIRED_PERMISSIONS_REQUEST_CODE);
                BugleAnalytics.logEvent("SMS_Start_PermissionPage_BtnClick", true);
            } else {
                redirect();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        BugleAnalytics.logEvent("SMS_Start_PermissionPage_Show", true);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        BugleAnalytics.logEvent("SMS_Start_PermissionPage_Back", true);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String permissions[], final int[] grantResults) {
        if (requestCode == REQUIRED_PERMISSIONS_REQUEST_CODE) {
            if (OsUtil.hasRequiredPermissions()) {
                redirect();
            } else {
                Toast.makeText(this, R.string.welcome_permission_failed_toast,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void redirect(){
        UIIntents.get().launchConversationListActivity(this);
        BugleAnalytics.logEvent("SMS_Start_Permission_Success", true);
        finish();
    }
}

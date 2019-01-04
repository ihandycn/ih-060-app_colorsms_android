package com.android.messaging.ui.welcome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.messaging.R;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.OsUtil;

public class WelcomePermissionActivity extends AppCompatActivity {
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_permission);

        findViewById(R.id.welcome_permission_button).setOnClickListener(v -> {
            String[] permissions = OsUtil.getMissingPermissions(OsUtil.REQUIRE_PERMISSION_IN_WELCOME);
            requestPermissions(permissions, REQUIRED_PERMISSIONS_REQUEST_CODE);
        });
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String permissions[], final int[] grantResults) {
        if (requestCode == REQUIRED_PERMISSIONS_REQUEST_CODE) {
            if (OsUtil.hasPermissions(OsUtil.REQUIRE_PERMISSION_IN_WELCOME)) {
                UIIntents.get().launchConversationListActivity(this);
                finish();
            } else {
                Toast.makeText(this, R.string.welcome_permission_failed_toast,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}

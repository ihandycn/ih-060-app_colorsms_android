package com.android.messaging.ui.welcome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.messaging.R;
import com.android.messaging.ui.SnackBarManager;
import com.android.messaging.ui.UIIntents;

public class WelcomeSetAsDefault extends AppCompatActivity {
    private static final int REQUEST_SET_DEFAULT_SMS_APP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_set_as_default);

        findViewById(R.id.welcome_set_default_button).setOnClickListener(v -> {
            final Intent intent = UIIntents.get().getChangeDefaultSmsAppIntent(WelcomeSetAsDefault.this);
            startActivityForResult(intent, REQUEST_SET_DEFAULT_SMS_APP);
        });

        findViewById(R.id.welcome_set_default_skip_button).setOnClickListener(v -> {
            UIIntents.get().launchConversationListActivity(this);
            finish();
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        SnackBarManager.get().dismiss();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_SET_DEFAULT_SMS_APP){
            UIIntents.get().launchConversationListActivity(this);
            finish();
        }
    }
}

package com.android.messaging.debug;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.EditText;

import com.android.messaging.R;
import com.android.messaging.ui.customize.PrimaryColors;

public class ChangeThemeColorDebugActivity extends Activity {

    EditText mEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_theme_color);
        mEditText = findViewById(R.id.edit_text);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (TextUtils.isEmpty(mEditText.getText())) {
            return;
        }
        PrimaryColors.changePrimaryColor(Color.parseColor(mEditText.getText().toString()));
    }
}

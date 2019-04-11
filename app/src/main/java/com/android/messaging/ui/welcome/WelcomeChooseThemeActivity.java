package com.android.messaging.ui.welcome;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.messaging.R;
import com.android.messaging.util.BugleActivityUtil;

public class WelcomeChooseThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugleActivityUtil.adaptScreen4VerticalSlide(this, 360);
        setContentView(R.layout.welcome_choose_theme_activity);
    }

    @Override
    protected void onDestroy() {
        BugleActivityUtil.cancelAdaptScreen(this);
        super.onDestroy();
    }
}


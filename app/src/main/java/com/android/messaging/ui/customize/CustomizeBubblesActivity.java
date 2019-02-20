package com.android.messaging.ui.customize;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.messaging.R;

public class CustomizeBubblesActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_bubbles_activity);
    }
}

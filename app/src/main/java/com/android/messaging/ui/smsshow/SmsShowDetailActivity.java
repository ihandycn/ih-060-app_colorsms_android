package com.android.messaging.ui.smsshow;

import android.os.Bundle;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.messaging.R;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class SmsShowDetailActivity extends HSAppCompatActivity {
    private TextView mApplyButton;
    private LottieAnimationView mLoadingAnimView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_show_detail_activity);

        mApplyButton = findViewById(R.id.apply_button);
        mLoadingAnimView = findViewById(R.id.loading_lottie_animation);



        mApplyButton.setOnClickListener(v -> {

        });
    }
}

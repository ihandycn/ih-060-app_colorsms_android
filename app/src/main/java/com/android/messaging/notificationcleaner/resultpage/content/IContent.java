package com.android.messaging.notificationcleaner.resultpage.content;

import android.content.Context;

public interface IContent {

    void initView(Context context);

    void startAnimation();

    void onActivityDestroy();
}

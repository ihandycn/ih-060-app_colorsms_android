package com.android.messaging.notificationcleaner.resultpage.transition;

import android.view.View;

import com.android.messaging.notificationcleaner.resultpage.content.IContent;

public interface ITransition {

    int getLayoutId();

    void onFinishInflateTransitionView(View transitionView);

    void setContent(IContent content);
}

package com.android.messaging.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.messaging.R;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

public class PermissionGuideActivity extends HSActivity implements INotificationObserver {

    public static String EVENT_DISMISS = "event_dismiss";

    private FrameLayout rootView;
    private BasePermissionGuideDialog dialog;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.empty_content_activity);
        rootView = findViewById(R.id.root_view);
        rootView.removeAllViews();

        dialog = PermissionGuideManager.getInstance().getCurrentPermissionGuide();
        if (rootView != null && dialog != null) {
            if (dialog.getParent() != null) {
                ViewParent vp = dialog.getParent();
                if (vp instanceof ViewGroup) {
                    ((ViewGroup) vp).removeView(dialog);
                }
            }

            FrameLayout.LayoutParams fparams;
            ViewGroup.LayoutParams params = dialog.getLayoutParams();
            if (params instanceof WindowManager.LayoutParams) {
                WindowManager.LayoutParams wparam = (WindowManager.LayoutParams) params;
                fparams = new FrameLayout.LayoutParams(wparam.width, wparam.height, wparam.gravity);
            } else if (params instanceof FrameLayout.LayoutParams) {
                fparams = (FrameLayout.LayoutParams) params;
            } else {
                fparams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            }
            HSLog.i("FWM", "show guide:" + params);
            rootView.addView(dialog, fparams);
//            getWindow().addContentView(dialog, fparams);
            dialog.onAddedToWindow();

            HSGlobalNotificationCenter.addObserver(EVENT_DISMISS, this);
        } else {
            HSLog.i("FWM", "root == " + rootView + "    view == " + dialog);
            finish();
        }
    }

    @Override public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (dialog != null) {
            dialog.onRemovedFromWindow();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (rootView != null) {
            rootView.removeAllViews();
        }
        HSGlobalNotificationCenter.removeObserver(this);
        PermissionGuideManager.getInstance().removePermissionGuide(false);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(s, EVENT_DISMISS)) {
            finish();
        }
    }

    @Override protected void onResume() {
        super.onResume();
//        if (!LifecycleCallbacks.getInstance().isLauncherStop()) {
//            finish();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.app_lock_fade_out_long);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        HSLog.i("FWM", "PermissionActivity onTouch " + event);
        if (dialog != null) {
            dialog.setVisibility(View.GONE);
        }
        if (rootView != null) {
            rootView.removeAllViews();
        }
        HSGlobalNotificationCenter.removeObserver(this);
        PermissionGuideManager.getInstance().removePermissionGuide(false);
        finish();
        return false;
    }
}

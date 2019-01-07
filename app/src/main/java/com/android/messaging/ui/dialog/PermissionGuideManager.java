package com.android.messaging.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;

import com.android.messaging.util.CommonUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.rom.RomUtils;

import java.lang.ref.WeakReference;

public class PermissionGuideManager {

    public static final long RESOLVER_WIZARD_AUTO_DISMISS_TIME = 3200;
    private volatile static PermissionGuideManager sInstance;

    private BasePermissionGuideDialog mPermissionGuide;
    private LayoutParams mTurnOnGuideWindowParams;
    private WeakReference<Context> mContextRef;

    protected final Handler mHandler = new Handler();

    public enum PermissionGuideType {
        ICON_BADGE,
        SET_AS_DEFAULT,
        SET_AS_DEFAULT_HUAWEI_KITKAT,
        SET_AS_DEFAULT_HUAWEI_UP_M,
        SET_AS_DEFAULT_HUAWEI_EMUI_4_1,
        NOTIFICATION_CLEANER_ACCESS_FULL_SCREEN,
        APP_LOCK,
        MESSAGE_CENTER_ACCESS,
        FIVE_STAR_RATE,
        WALLPAPER_LIVE,
        WALLPAPER_3D,
        COUNTER_USAGE,
    }
    
    private boolean mEnabled = true;
    private boolean mPermissionGuideShowAsActivity = false;

    public static PermissionGuideManager getInstance() {
        if (sInstance == null) {
            synchronized (PermissionGuideManager.class) {
                if (sInstance == null) {
                    sInstance = new PermissionGuideManager();
                }
            }
        }
        return sInstance;
    }

    // Do NOT invoke this directly, use getInstance()
    public PermissionGuideManager() {
        /**
         *  We handle Call Ringing in {@link com.honeycomb.launcher.receiver.LauncherPhoneStateListener} <br>
         *
         */
    }

    void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    private void setNavigationBarColor(@ColorInt int color) {
        if (mContextRef != null) {
            Context context = mContextRef.get();
            if (context != null) {
                Activity activity = CommonUtils.getActivity(context);
                if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.getWindow().setNavigationBarColor(color);
                } else if (activity != null) {
//                    ActivityUtils.setNavigationBarColorNative(activity, color);
                }
            }
        }
    }

    public void showPermissionGuide(Context context, PermissionGuideType type, boolean isShowImmediately) {
        if (!mEnabled) {
            HSLog.d("FloatWindowManager", "Disabled");
            return;
        }
        try {
            if (mPermissionGuide == null) {
                mPermissionGuide = createPermissionGuide(context, type);
                mPermissionGuide.setShowContentImmediately(isShowImmediately);
                configLayoutParam(type);

                HSLog.i("FWM", "PermissionGuideActivity type == " + type);
                mPermissionGuideShowAsActivity = true;
                mPermissionGuide.setLayoutParams(mTurnOnGuideWindowParams);
                final long[] delay = new long[1];
                delay[0] = 0;
                if (type == PermissionGuideType.APP_LOCK ||
                        type == PermissionGuideType.FIVE_STAR_RATE ||
                        type == PermissionGuideType.SET_AS_DEFAULT ||
                        type == PermissionGuideType.SET_AS_DEFAULT_HUAWEI_UP_M ||
                        type == PermissionGuideType.SET_AS_DEFAULT_HUAWEI_EMUI_4_1 ||
                        type == PermissionGuideType.SET_AS_DEFAULT_HUAWEI_KITKAT ||
                        type == PermissionGuideType.NOTIFICATION_CLEANER_ACCESS_FULL_SCREEN) {
                    delay[0] = 800;
                    Preferences.getDefault().doOnce(() -> {
                        delay[0] = 1600;
                    }, "PermissionGuideType.APP_LOCK");
                } else if (RomUtils.checkIsVivoRom() && Compats.getFuntouchVersion() < 3.0) {
                    delay[0] = 800;
                }

                if (Compats.IS_OPPO_DEVICE) {
                    delay[0] = 0;
                }
                mHandler.postDelayed(() -> Navigations.startActivity(HSApplication.getContext(), PermissionGuideActivity.class), delay[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.e("Error creating permission guide: " + e.getMessage());
        }
    }

    private BasePermissionGuideDialog createPermissionGuide(Context context, PermissionGuideType type) {
        switch (type) {
            case FIVE_STAR_RATE:
                return new FiveStarRateGuideDialog(context);
//            case SET_AS_DEFAULT:
//                return new SetAsDefaultGuide(context);
//            case SET_AS_DEFAULT_HUAWEI_KITKAT:
//                return new SetAsDefaultGuideHuaweiKitkat(context);
//            case SET_AS_DEFAULT_HUAWEI_UP_M:
//                return new SetAsDefaultGuideHuaweiUpM(context);
//            case SET_AS_DEFAULT_HUAWEI_EMUI_4_1:
//                return new SetAsDefaultGuideHuaweiEmui_4_1(context);
//            case ICON_BADGE:
//                return new IconBadgeAccessGuideDialog(context);
//            case WALLPAPER_LIVE:
//                return new WallpaperHintDialog(context, PermissionGuideType.WALLPAPER_LIVE);
//            case WALLPAPER_3D:
//                return new WallpaperHintDialog(context, PermissionGuideType.WALLPAPER_3D);
//            case NOTIFICATION_CLEANER_ACCESS_FULL_SCREEN:
//                return new NotificationCleanerAccessGuideAnimationDialog(context);
//            case MESSAGE_CENTER_ACCESS:
//                return new MessageCenterAccessGuideDialog(context);
//            case APP_LOCK:
//                return new AppLockPermissionGuideDialog(context);
//
//            case COUNTER_USAGE:
//                return new CounterUsagePermissionGuideAnimationDialog(context);
            default:
                throw new IllegalArgumentException("Unknown permission guide type.");
        }
    }

    private void configLayoutParam(PermissionGuideType type) {
        boolean autoCleanSelf = true;
        if (mTurnOnGuideWindowParams == null) {
            mTurnOnGuideWindowParams = getDefaultLayoutParams(false);
        }
        mTurnOnGuideWindowParams.height = LayoutParams.MATCH_PARENT;
        mTurnOnGuideWindowParams.gravity = Gravity.CENTER;

        if (type == PermissionGuideType.ICON_BADGE
                || type == PermissionGuideType.SET_AS_DEFAULT
                || type == PermissionGuideType.APP_LOCK
                || type == PermissionGuideType.MESSAGE_CENTER_ACCESS
                || type == PermissionGuideType.SET_AS_DEFAULT_HUAWEI_EMUI_4_1
                || type == PermissionGuideType.FIVE_STAR_RATE) {
            mTurnOnGuideWindowParams.width = LayoutParams.MATCH_PARENT;
            mTurnOnGuideWindowParams.height = LayoutParams.WRAP_CONTENT;
            mTurnOnGuideWindowParams.gravity = Gravity.BOTTOM;
        } else if (type == PermissionGuideType.WALLPAPER_LIVE
                || type == PermissionGuideType.WALLPAPER_3D) {
            mTurnOnGuideWindowParams.width = LayoutParams.MATCH_PARENT;
            mTurnOnGuideWindowParams.height = LayoutParams.WRAP_CONTENT;
            mTurnOnGuideWindowParams.y = Dimensions.pxFromDp(64);
            mTurnOnGuideWindowParams.gravity = Gravity.BOTTOM;
            autoCleanSelf = false;
        } else if (type == PermissionGuideType.NOTIFICATION_CLEANER_ACCESS_FULL_SCREEN
                || type == PermissionGuideType.SET_AS_DEFAULT_HUAWEI_UP_M
                || type == PermissionGuideType.COUNTER_USAGE) {
            mTurnOnGuideWindowParams.width = LayoutParams.MATCH_PARENT;
            mTurnOnGuideWindowParams.height = LayoutParams.MATCH_PARENT;
            mTurnOnGuideWindowParams.gravity = Gravity.CENTER;
            if (type == PermissionGuideType.SET_AS_DEFAULT_HUAWEI_UP_M) {
                mTurnOnGuideWindowParams.y = Dimensions.pxFromDp(32);
            }
        } else if (type == PermissionGuideType.SET_AS_DEFAULT_HUAWEI_KITKAT) {
            mTurnOnGuideWindowParams.width = LayoutParams.MATCH_PARENT;
            mTurnOnGuideWindowParams.height = LayoutParams.WRAP_CONTENT;
            mTurnOnGuideWindowParams.gravity = Gravity.TOP;
        }
        mTurnOnGuideWindowParams.format = PixelFormat.TRANSLUCENT;
        // In HuaWei System Settings - Notification Center - Dropzones, Default block app float window but TYPE_TOAST
        // TYPE_TOAST float window will dismiss above api 25
        mTurnOnGuideWindowParams.flags |= LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_NOT_TOUCH_MODAL;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (autoCleanSelf) {
                mPermissionGuide.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removePermissionGuide(false);
//                        removeFloatButton();
                    }
                }, 5000);
            }
            mTurnOnGuideWindowParams.type = LayoutParams.TYPE_TOAST;
        } else if (Compats.IS_HUAWEI_DEVICE) {
            mTurnOnGuideWindowParams.type = LayoutParams.TYPE_TOAST;
        } else {
            mTurnOnGuideWindowParams.type = getFloatWindowType();
        }
        HSLog.i("FWM", "configLayoutParam == " + mTurnOnGuideWindowParams);
    }

    private int getFloatWindowType() {
        int floatWindowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            floatWindowType = LayoutParams.TYPE_TOAST;
        } else {
            floatWindowType = LayoutParams.TYPE_PHONE;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            floatWindowType = LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(HSApplication.getContext())) {
            floatWindowType = LayoutParams.TYPE_SYSTEM_ERROR;
        }
        return floatWindowType;
    }

//    private void showAsFloatWindow(PermissionGuideType type) {
//        mPermissionGuide.setLayoutParams(mTurnOnGuideWindowParams);
//        getWindowManager().addView(mPermissionGuide, mTurnOnGuideWindowParams);
//        mPermissionGuide.onAddedToWindow(getWindowManager());
//    }

    BasePermissionGuideDialog getCurrentPermissionGuide() {
        HSLog.i("FWM", "getCurrentPermissionGuide == " + mPermissionGuide);
        return mPermissionGuide;
    }

    public void removePermissionGuide(boolean showFloatButton) {
        HSLog.i("FWM", "removePermissionGuide == " + mPermissionGuide + "    sfb == " + showFloatButton);
        HSGlobalNotificationCenter.sendNotification(PermissionGuideActivity.EVENT_DISMISS);
        mPermissionGuideShowAsActivity = false;
        mPermissionGuide = null;
    }

    public boolean isPermissionGuideShowing() {
        return null != mPermissionGuide;
    }

    public void dismissAnyModalTip() { // Resolver wizard excluded
        removePermissionGuide(true);
    }

    private LayoutParams getDefaultLayoutParams(boolean notFocusable) {
        LayoutParams lp = new LayoutParams();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.format = PixelFormat.TRANSLUCENT;
        if (CommonUtils.ATLEAST_O) {
            lp.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = LayoutParams.TYPE_PHONE;
        }
        if (notFocusable) {
            lp.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        return lp;
    }

    private LayoutParams getFullScreenDefaultLayoutParams(boolean notFocusable) {
        LayoutParams lp = new LayoutParams();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        lp.format = PixelFormat.TRANSLUCENT;
        if (CommonUtils.ATLEAST_O) {
            lp.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = LayoutParams.TYPE_PHONE;
        }
        lp.flags |= LayoutParams.FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            lp.flags |= LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        if (notFocusable) {
            lp.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        return lp;
    }

    public interface ClickActionListener {
        void onClickAction();
    }
}

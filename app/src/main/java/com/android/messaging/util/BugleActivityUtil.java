/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.messaging.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.UserManager;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;

/**
 * Utility class including logic to verify requirements to run Bugle and other activity startup
 * logic. Called from base Bugle activity classes.
 */
public class BugleActivityUtil {

    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;

    /**
     * Determine if the requirements for the app to run are met. Log any Activity startup
     * analytics.
     *
     * @param context
     * @param activity is used to launch an error Dialog if necessary
     * @return true if resume should continue normally. Returns false if some requirements to run
     * are not met.
     */
    public static boolean onActivityResume(Context context, Activity activity) {
        if (OsUtil.hasRequiredPermissions()) {
            DataModel.get().onActivityResume();
            Factory.get().onActivityResume();
        }

        // Validate all requirements to run are met
        return checkHasSmsPermissionsForUser(context, activity);
    }

    /**
     * Determine if the user doesn't have SMS permissions. This can happen if you are not the phone
     * owner and the owner has disabled your SMS permissions.
     *
     * @param context  is the Context used to resolve the user permissions
     * @param activity is the Activity used to launch an error Dialog if necessary
     * @return true if the user has SMS permissions, otherwise false.
     */
    private static boolean checkHasSmsPermissionsForUser(Context context, Activity activity) {
        if (!OsUtil.isAtLeastL()) {
            // UserManager.DISALLOW_SMS added in L. No multiuser phones before this
            return true;
        }
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        if (userManager.hasUserRestriction(UserManager.DISALLOW_SMS)) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.requires_sms_permissions_message)
                    .setCancelable(false)
                    .setNegativeButton(R.string.requires_sms_permissions_close_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog,
                                                    final int button) {
                                    System.exit(0);
                                }
                            })
                    .show();
            return false;
        }
        return true;
    }

    public static Activity contextToActivitySafely(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return (Activity) (((ContextThemeWrapper) context).getBaseContext());
        } else if (context instanceof android.support.v7.view.ContextThemeWrapper) {
            return (Activity) (((android.support.v7.view.ContextThemeWrapper) context).getBaseContext());
        } else if (context instanceof android.support.v7.widget.TintContextWrapper) {
            return (Activity) (((android.support.v7.widget.TintContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isDestroyed(Activity activity) {
        if (activity == null) {
            return false;
        }

        if (activity.isFinishing()) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed();
        }
        return false;
    }

    /**
     * Adapt the screen for vertical slide.
     *
     * @param activity        The activity.
     * @param designWidthInPx The size of design diagram's width, in pixel.
     */
    public static void adaptScreen4VerticalSlide(final Activity activity,
                                                 final int designWidthInPx) {
        adaptScreen(activity, designWidthInPx, true);
    }

    /**
     * Adapt the screen for horizontal slide.
     *
     * @param activity         The activity.
     * @param designHeightInPx The size of design diagram's height, in pixel.
     */
    public static void adaptScreen4HorizontalSlide(final Activity activity,
                                                   final int designHeightInPx) {
        adaptScreen(activity, designHeightInPx, false);
    }

    /**
     * Reference from: https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA
     */
    private static void adaptScreen(final Activity activity,
                                    final int sizeInPx,
                                    final boolean isVerticalSlide) {
        final DisplayMetrics systemDm = Resources.getSystem().getDisplayMetrics();
        final DisplayMetrics appDm = Factory.get().getApplicationContext().getResources().getDisplayMetrics();
        final DisplayMetrics activityDm = activity.getResources().getDisplayMetrics();
        if (isVerticalSlide) {
            activityDm.density = activityDm.widthPixels / (float) sizeInPx;
        } else {
            activityDm.density = activityDm.heightPixels / (float) sizeInPx;
        }
        activityDm.scaledDensity = activityDm.density * (systemDm.scaledDensity / systemDm.density);
        activityDm.densityDpi = (int) (160 * activityDm.density);
        appDm.density = activityDm.density;
        appDm.scaledDensity = activityDm.scaledDensity;
        appDm.densityDpi = activityDm.densityDpi;
    }

    /**
     * Cancel adapt the screen.
     *
     * @param activity The activity.
     */
    public static void cancelAdaptScreen(final Activity activity) {
        final DisplayMetrics systemDm = Resources.getSystem().getDisplayMetrics();
        final DisplayMetrics appDm = Factory.get().getApplicationContext().getResources().getDisplayMetrics();
        final DisplayMetrics activityDm = activity.getResources().getDisplayMetrics();
        activityDm.density = systemDm.density;
        activityDm.scaledDensity = systemDm.scaledDensity;
        activityDm.densityDpi = systemDm.densityDpi;
        appDm.density = systemDm.density;
        appDm.scaledDensity = systemDm.scaledDensity;
        appDm.densityDpi = systemDm.densityDpi;
    }

    /**
     * Return whether adapt screen.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isAdaptScreen() {
        final DisplayMetrics systemDm = Resources.getSystem().getDisplayMetrics();
        final DisplayMetrics appDm = Factory.get().getApplicationContext().getResources().getDisplayMetrics();
        return systemDm.density != appDm.density;
    }

}


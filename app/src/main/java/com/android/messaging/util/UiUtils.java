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
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.ui.SnackBar;
import com.android.messaging.ui.SnackBar.Placement;
import com.android.messaging.ui.SnackBarInteraction;
import com.android.messaging.ui.SnackBarManager;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.ui.welcome.WelcomePermissionActivity;
import com.android.messaging.ui.welcome.WelcomeSetAsDefault;
import com.android.messaging.ui.welcome.WelcomeStartActivity;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class UiUtils {
    /**
     * MediaPicker transition duration in ms
     */
    public static final int MEDIAPICKER_TRANSITION_DURATION =
            getApplicationContext().getResources().getInteger(
                    R.integer.mediapicker_transition_duration);
    /**
     * Short transition duration in ms
     */
    public static final int ASYNCIMAGE_TRANSITION_DURATION =
            getApplicationContext().getResources().getInteger(
                    R.integer.asyncimage_transition_duration);
    /**
     * Compose transition duration in ms
     */
    public static final int COMPOSE_TRANSITION_DURATION =
            getApplicationContext().getResources().getInteger(
                    R.integer.compose_transition_duration);
    /**
     * Generic duration for revealing/hiding a view
     */
    public static final int REVEAL_ANIMATION_DURATION =
            getApplicationContext().getResources().getInteger(
                    R.integer.reveal_view_animation_duration);

    public static final Interpolator DEFAULT_INTERPOLATOR = new CubicBezierInterpolator(
            0.4f, 0.0f, 0.2f, 1.0f);

    public static final Interpolator EASE_IN_INTERPOLATOR = new CubicBezierInterpolator(
            0.4f, 0.0f, 0.8f, 0.5f);

    public static final Interpolator EASE_OUT_INTERPOLATOR = new CubicBezierInterpolator(
            0.0f, 0.0f, 0.2f, 1f);

    private static final String TAG_DIALOG_FRAGMENT = "dialog_fragment";

    /**
     * Show a simple toast at the bottom
     */
    public static void showToastAtBottom(final int messageId) {
        UiUtils.showToastAtBottom(getApplicationContext().getString(messageId));
    }

    /**
     * Show a simple toast at the bottom
     */
    public static void showToastAtBottom(final String message) {
        final Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    /**
     * Show a simple toast at the default position
     */
    public static void showToast(final int messageId) {
        final Toast toast = Toast.makeText(getApplicationContext(),
                getApplicationContext().getString(messageId), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    /**
     * Show a simple toast at the default position
     */
    public static void showToast(final int pluralsMessageId, final int count) {
        final Toast toast = Toast.makeText(getApplicationContext(),
                getApplicationContext().getResources().getQuantityString(pluralsMessageId, count),
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    public static void showSnackBar(final Context context, @NonNull final View parentView,
                                    final String message, @Nullable final Runnable runnable, final int runnableLabel,
                                    @Nullable final List<SnackBarInteraction> interactions) {
        Assert.notNull(context);
        SnackBar.Action action = null;
        switch (runnableLabel) {
            case SnackBar.Action.SNACK_BAR_UNDO:
                action = SnackBar.Action.createUndoAction(runnable);
                break;
            case SnackBar.Action.SNACK_BAR_RETRY:
                action = SnackBar.Action.createRetryAction(runnable);
                break;
            default:
                break;
        }

        showSnackBarWithCustomAction(context, parentView, message, action, interactions,
                null /* placement */);
    }

    public static void showSnackBarWithCustomAction(final Context context,
                                                    @NonNull final View parentView,
                                                    @NonNull final String message,
                                                    @NonNull final SnackBar.Action action,
                                                    @Nullable final List<SnackBarInteraction> interactions,
                                                    @Nullable final Placement placement) {
        Assert.notNull(context);
        Assert.isTrue(!TextUtils.isEmpty(message));
        Assert.notNull(action);
        SnackBarManager.get()
                .newBuilder(parentView)
                .setText(message)
                .setAction(action)
                .withInteractions(interactions)
                .withPlacement(placement)
                .show();
    }

    /**
     * Run the given runnable once after the next layout pass of the view.
     */
    public static void doOnceAfterLayoutChange(final View view, final Runnable runnable) {
        final OnLayoutChangeListener listener = new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View v, final int left, final int top, final int right,
                                       final int bottom, final int oldLeft, final int oldTop, final int oldRight,
                                       final int oldBottom) {
                // Call the runnable outside the layout pass because very few actions are allowed in
                // the layout pass
                ThreadUtil.getMainThreadHandler().post(runnable);
                view.removeOnLayoutChangeListener(this);
            }
        };
        view.addOnLayoutChangeListener(listener);
    }

    public static boolean isLandscapeMode() {
        return Factory.get().getApplicationContext().getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    private static Context getApplicationContext() {
        return Factory.get().getApplicationContext();
    }

    public static CharSequence commaEllipsize(
            final String text,
            final TextPaint paint,
            final int width,
            final String oneMore,
            final String more) {
        CharSequence ellipsized = TextUtils.commaEllipsize(
                text,
                paint,
                width,
                oneMore,
                more);
        if (TextUtils.isEmpty(ellipsized)) {
            ellipsized = text;
        }
        return ellipsized;
    }

    /**
     * Reveals/Hides a view with a scale animation from view center.
     *
     * @param view              the view to animate
     * @param desiredVisibility desired visibility (e.g. View.GONE) for the animated view.
     * @param onFinishRunnable  an optional runnable called at the end of the animation
     */
    public static void revealOrHideViewWithAnimation(final View view, final int desiredVisibility,
                                                     @Nullable final Runnable onFinishRunnable) {
        final boolean needAnimation = view.getVisibility() != desiredVisibility;
        if (needAnimation) {
            final float fromScale = desiredVisibility == View.VISIBLE ? 0F : 1F;
            final float toScale = desiredVisibility == View.VISIBLE ? 1F : 0F;
            final ScaleAnimation showHideAnimation =
                    new ScaleAnimation(fromScale, toScale, fromScale, toScale,
                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            showHideAnimation.setDuration(REVEAL_ANIMATION_DURATION);
            showHideAnimation.setInterpolator(DEFAULT_INTERPOLATOR);
            showHideAnimation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(final Animation animation) {
                }

                @Override
                public void onAnimationRepeat(final Animation animation) {
                }

                @Override
                public void onAnimationEnd(final Animation animation) {
                    if (onFinishRunnable != null) {
                        // Rather than running this immediately, we post it to happen next so that
                        // the animation will be completed so that the view can be detached from
                        // it's window.  Otherwise, we may leak memory.
                        ThreadUtil.getMainThreadHandler().post(onFinishRunnable);
                    }
                }
            });
            view.clearAnimation();
            view.startAnimation(showHideAnimation);
            // We are playing a view Animation; unlike view property animations, we can commit the
            // visibility immediately instead of waiting for animation end.
            view.setVisibility(desiredVisibility);
        } else if (onFinishRunnable != null) {
            // Make sure onFinishRunnable is always executed.
            ThreadUtil.getMainThreadHandler().post(onFinishRunnable);
        }
    }

    public static Rect getMeasuredBoundsOnScreen(final View view) {
        final int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Rect(location[0], location[1],
                location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }

    public static void setStatusBarColor(final Activity activity, final int color) {
        if (OsUtil.isAtLeastL()) {
            // we need statusbar color same as actionbar color
            activity.getWindow().setStatusBarColor(color);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = activity.getWindow().getDecorView();
                if (color == Color.WHITE) {
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    // We want to change tint color to white again.
                    // You can also record the flags in advance so that you can turn UI back completely if
                    // you have set other flags before, such as translucent or full screen.
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
        }
    }

    public static void lockOrientation(final Activity activity) {
        final int orientation = activity.getResources().getConfiguration().orientation;
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        // rotation tracks the rotation of the device from its natural orientation
        // orientation tracks whether the screen is landscape or portrait.
        // It is possible to have a rotation of 0 (device in its natural orientation) in portrait
        // (phone), or in landscape (tablet), so we have to check both values to determine what to
        // pass to setRequestedOrientation.
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
        }
    }

    public static void unlockOrientation(final Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public static int getPaddingStart(final View view) {
        return OsUtil.isAtLeastJB_MR1() ? view.getPaddingStart() : view.getPaddingLeft();
    }

    public static int getPaddingEnd(final View view) {
        return OsUtil.isAtLeastJB_MR1() ? view.getPaddingEnd() : view.getPaddingRight();
    }

    public static boolean isRtlMode() {
        return OsUtil.isAtLeastJB_MR2() && Factory.get().getApplicationContext().getResources()
                .getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Judge for welcome pages.
     */
    private static final String PREF_KEY_FIRST_TIME_LAUNCH = "pref_key_first_launch";

    private static boolean shouldShowWelcomeStart() {
        return Preferences.getDefault().getBoolean(PREF_KEY_FIRST_TIME_LAUNCH, true);
    }

    private static boolean shouldShowWelcomePermission() {
        return !OsUtil.hasRequiredPermissions();
    }

    private static boolean shouldShowWelcomeSetDefault() {
        return !PhoneUtils.getDefault().isDefaultSmsApp();
    }

    /**
     * Check if the activity needs to be redirected to permission check
     *
     * @return true if {@link Activity#finish()} was called because redirection was performed
     */
    public static boolean redirectToWelcomeIfNeeded(final Activity activity) {
        if (activity instanceof WelcomeStartActivity) {
            return false;
        }

        if (activity instanceof WelcomePermissionActivity) {
            return false;
        }

        if (activity instanceof WelcomeSetAsDefault) {
            return false;
        }

        if (shouldShowWelcomeStart()) {
            Preferences.getDefault().putBoolean(PREF_KEY_FIRST_TIME_LAUNCH, false);
            UIIntents.get().launchWelcomeStartActivity(activity);
            HSLog.d("UiUtil", "Show welcome start");
        } else {
            if (shouldShowWelcomeSetDefault()) {
                UIIntents.get().launchWelcomeSetAsDefaultActivity(activity);
                HSLog.d("UiUtil", "Show welcome set as default");
            } else {
                if (shouldShowWelcomePermission()) {
                    UIIntents.get().launchWelcomePermissionActivity(activity);
                    HSLog.d("UiUtil", "Show welcome permission");
                } else {
                    // No redirect performed
                    return false;
                }
            }
        }

        // Redirect performed
        activity.finish();
        HSLog.d("UiUtil", activity.getLocalClassName() + " finish");
        return true;
    }

    /**
     * Called to check if all conditions are nominal and a "go" for some action, such as deleting
     * a message, that requires this app to be the default app. This is also a precondition
     * required for sending a draft.
     *
     * @return true if all conditions are nominal and we're ready to send a message
     */
    public static boolean isReadyForAction() {
        final PhoneUtils phoneUtils = PhoneUtils.getDefault();

        // Have all the conditions been met:
        // Supports SMS?
        // Has a preferred sim?
        // Is the default sms app?
        return phoneUtils.isSmsCapable() &&
                phoneUtils.getHasPreferredSmsSim() &&
                phoneUtils.isDefaultSmsApp();
    }

    /*
     * Removes all html markup from the text and replaces links with the the text and a text version
     * of the href.
     * @param htmlText HTML markup text
     * @return Sanitized string with link hrefs inlined
     */
    public static String stripHtml(final String htmlText) {
        final StringBuilder result = new StringBuilder();
        final Spanned markup = Html.fromHtml(htmlText);
        final String strippedText = markup.toString();

        final URLSpan[] links = markup.getSpans(0, markup.length() - 1, URLSpan.class);
        int currentIndex = 0;
        for (final URLSpan link : links) {
            final int spanStart = markup.getSpanStart(link);
            final int spanEnd = markup.getSpanEnd(link);
            if (spanStart > currentIndex) {
                result.append(strippedText, currentIndex, spanStart);
            }
            final String displayText = strippedText.substring(spanStart, spanEnd);
            final String linkText = link.getURL();
            result.append(getApplicationContext().getString(R.string.link_display_format,
                    displayText, linkText));
            currentIndex = spanEnd;
        }
        if (strippedText.length() > currentIndex) {
            result.append(strippedText, currentIndex, strippedText.length());
        }
        return result.toString();
    }

    public static void setActionBarShadowVisibility(final AppCompatActivity activity, final boolean visible) {
        final ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setElevation(visible ?
                activity.getResources().getDimensionPixelSize(R.dimen.action_bar_elevation) :
                0);
        final View actionBarView = activity.getWindow().getDecorView().findViewById(
                android.support.v7.appcompat.R.id.decor_content_parent);
        if (actionBarView != null) {
            // AppCompatActionBar has one drawable Field, which is the shadow for the action bar
            // set the alpha on that drawable manually
            final Field[] fields = actionBarView.getClass().getDeclaredFields();
            try {
                for (final Field field : fields) {
                    if (field.getType().equals(Drawable.class)) {
                        field.setAccessible(true);
                        final Drawable shadowDrawable = (Drawable) field.get(actionBarView);
                        if (shadowDrawable != null) {
                            shadowDrawable.setAlpha(visible ? 255 : 0);
                            actionBarView.invalidate();
                            return;
                        }
                    }
                }
            } catch (final IllegalAccessException ex) {
                // Not expected, we should avoid this via field.setAccessible(true) above
                LogUtil.e(LogUtil.BUGLE_TAG, "Error setting shadow visibility", ex);
            }
        }
    }

    public static void showDialogFragment(Activity activity, DialogFragment dialogFragment) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            return;
        }
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        dialogFragment.show(ft, TAG_DIALOG_FRAGMENT);
    }

    /**
     * Get the activity that's hosting the view, typically casting view.getContext() as an Activity
     * is sufficient, but sometimes the context is a context wrapper, in which case we need to case
     * the base context
     */
    public static Activity getActivity(final View view) {
        if (view == null) {
            return null;
        }
        return getActivity(view.getContext());
    }

    /**
     * Get the activity for the supplied context, typically casting context as an Activity
     * is sufficient, but sometimes the context is a context wrapper, in which case we need to case
     * the base context
     */
    public static Activity getActivity(final Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }

        // We've hit a non-activity context such as an app-context
        return null;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isDestroyed(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed();
        }
        return false;
    }

    public static RemoteViews getWidgetMissingPermissionView(final Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.widget_missing_permission);
    }

    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant        The descendant to which the passed coordinate is relative.
     * @param root              The root view to make the coordinates relative to.
     * @param outCoord          The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *                          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     * this scale factor is assumed to be equal in X and Y, and so if at any point this
     * assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(
            View descendant, View root, int[] outCoord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<>();

        float[] pt = {outCoord[0], outCoord[1]};

        View v = descendant;
        while (v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }

            v0.getMatrix().mapPoints(pt);
            pt[0] += v0.getLeft();
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        outCoord[0] = Math.round(pt[0]);
        outCoord[1] = Math.round(pt[1]);
        return scale;
    }
}

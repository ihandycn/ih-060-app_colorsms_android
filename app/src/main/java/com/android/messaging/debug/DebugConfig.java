package com.android.messaging.debug;

import com.android.messaging.BuildConfig;

/**
 * Notice: DO NOT push any changes of this file to remote.
 * <p>
 * Run the following command to ignore future changes on this file:
 * git update-index --assume-unchanged launcher/src/com/honeycomb/launcher/debug/DebugConfig.java
 */
@SuppressWarnings("PointlessBooleanExpression")
public class DebugConfig {

    private static final boolean MONKEY = false;


    /**
     * Whether crashlytics crash reports is enabled on debug builds.
     * Disable this during development. Enable for test.
     */
    public static final boolean ENABLE_CRASHLYTICS_ON_DEBUG = true;

    /**
     * Whether LeakCanary is enabled.
     */
    public static final boolean ENABLE_LEAK_CANARY = false && BuildConfig.DEBUG;

    /**
     * Whether BlockCanary is enabled.
     */
    public static final boolean ENABLE_BLOCK_CANARY = false && BuildConfig.DEBUG;

    /**
     * Whether to skip welcome animation for test.
     */
    public static final boolean SKIP_WELCOME_ANIMATION = false && BuildConfig.DEBUG;

    /**
     * Whether to use "Volume Up" as Launcher activity finish() button and "Volume Down" as
     * Launcher#onDebugAction() trigger.
     */
    public static final boolean OVERRIDE_VOLUME_KEYS = true && BuildConfig.DEBUG;

    public static final boolean FORCE_ENABLE_ALL_FEATURES = false && BuildConfig.DEBUG;
}


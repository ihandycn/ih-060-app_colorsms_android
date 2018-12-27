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

import android.graphics.Typeface;

import com.android.messaging.Factory;

/**
 * Provides access to typefaces used by code. Specially important for typefaces coming from assets,
 * which appear (from platform code inspection) to not be cached.
 * Note: Considered making this a singleton provided by factory/appcontext, but seemed too simple,
 * not worth stubbing.
 */
public class Typefaces {

    private static Typeface sCustomSemiBold;
    private static Typeface sCustomRegular;
    private static Typeface sCustomMedium;

    public static Typeface getCustomSemiBold() {
        Assert.isMainThread();
        if (sCustomSemiBold == null) {
            sCustomSemiBold = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
                    "fonts/Custom-SemiBold.ttf");
        }
        return sCustomSemiBold;
    }

    public static Typeface getCustomRegular() {
        Assert.isMainThread();
        if (sCustomRegular == null) {
            sCustomRegular = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
                    "fonts/Custom-Regular.ttf");
        }
        return sCustomRegular;
    }

    public static Typeface getCustomMedium() {
        Assert.isMainThread();
        if (sCustomMedium == null) {
            sCustomMedium = Typeface.createFromAsset(Factory.get().getApplicationContext().getAssets(),
                    "fonts/Custom-Medium.ttf");
        }
        return sCustomMedium;
    }
}

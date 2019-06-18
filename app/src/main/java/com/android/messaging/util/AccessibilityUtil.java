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

import android.view.View;

public class AccessibilityUtil {

    /**
     * Check to see if the current layout is Right-to-Left. This check is only supported for
     * API 17+.
     * For earlier versions, this method will just return false.
     * @return boolean Boolean indicating whether the currently locale is RTL.
     */
    public static boolean isLayoutRtl(final View view) {
        if (OsUtil.isAtLeastJB_MR1()) {
            return View.LAYOUT_DIRECTION_RTL == view.getLayoutDirection();
        } else {
            return false;
        }
    }
}

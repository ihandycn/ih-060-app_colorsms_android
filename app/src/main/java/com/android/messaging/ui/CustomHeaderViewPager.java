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
package com.android.messaging.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.android.messaging.R;

/**
 * A view that contains both a view pager and a tab strip wrapped in a linear layout.
 */
public class CustomHeaderViewPager extends CustomViewPager {
    public CustomHeaderViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.custom_header_view_pager;
    }
}

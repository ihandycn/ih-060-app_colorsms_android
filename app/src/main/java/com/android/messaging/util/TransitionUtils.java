package com.android.messaging.util;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;

import com.android.messaging.R;

/**
 * Created by lizhe on 2019/6/10.
 */

public class TransitionUtils {

    public static Bundle getTransitionInBundle(Context context) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_from_right_and_fade, R.anim.anim_null);
        return options.toBundle();
    }
}

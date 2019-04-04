package com.android.messaging.util;

import com.superapps.util.Threads;
import com.superapps.util.TimeTicker;

public class BugleTimeTicker extends TimeTicker {

    @Override public void onTick() {
        Threads.postOnThreadPoolExecutor(() -> {
        });
    }
}

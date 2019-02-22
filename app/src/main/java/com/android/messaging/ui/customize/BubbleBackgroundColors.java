package com.android.messaging.ui.customize;

public class BubbleBackgroundColors {

    static int[] COLORS = new int[]{
            PrimaryColors.getPrimaryColor(),
            0xff0083fe,
            0xff16c7d3,
            0xffff7e2a,
            0xff7646ff,
            0xfff846c0,
            0xffd619ff,
            0xfff14d4d,
            0xfff5d20d,
            0xff000000,
            0xff81de09,
            0xfff6bd01
    };


    public static int getBubbleBackgroundColor() {
        return PrimaryColors.getPrimaryColor();
    }

    public static int getBubbleBackgroundColorDark() {
        return PrimaryColors.getPrimaryColorDark();
    }
}

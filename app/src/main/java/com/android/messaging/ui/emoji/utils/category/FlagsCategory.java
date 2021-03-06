package com.android.messaging.ui.emoji.utils.category;


import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.emoji.Emoji;
import com.android.messaging.ui.emoji.utils.emoji.EmojiCategory;

public final class FlagsCategory implements EmojiCategory {

    @Override
    public int getIcon() {
        return R.drawable.emoji_category_flag;
    }

    @Override
    public int getIconSelected() {
        return R.drawable.emoji_category_flag_selected;
    }

    private static final Emoji[] DATA = new Emoji[]{
            new Emoji(127987, "emoji_1f3f3"),
            new Emoji(127988, "emoji_1f3f4"),
            new Emoji(127937, "emoji_1f3c1"),
            new Emoji(128681, "emoji_1f6a9"),
            new Emoji(new int[]{127987, 65039, 8205, 127752}, "emoji_1f3f3_1f308"),
            new Emoji(new int[]{127462, 127467}, "emoji_1f1e6_1f1eb"),
            new Emoji(new int[]{127462, 127485}, "emoji_1f1e6_1f1fd"),
            new Emoji(new int[]{127462, 127473}, "emoji_1f1e6_1f1f1"),
            new Emoji(new int[]{127465, 127487}, "emoji_1f1e9_1f1ff"),
            new Emoji(new int[]{127462, 127480}, "emoji_1f1e6_1f1f8"),
            new Emoji(new int[]{127462, 127465}, "emoji_1f1e6_1f1e9"),
            new Emoji(new int[]{127462, 127476}, "emoji_1f1e6_1f1f4"),
            new Emoji(new int[]{127462, 127470}, "emoji_1f1e6_1f1ee"),
            new Emoji(new int[]{127462, 127478}, "emoji_1f1e6_1f1f6"),
            new Emoji(new int[]{127462, 127468}, "emoji_1f1e6_1f1ec"),
            new Emoji(new int[]{127462, 127479}, "emoji_1f1e6_1f1f7"),
            new Emoji(new int[]{127462, 127474}, "emoji_1f1e6_1f1f2"),
            new Emoji(new int[]{127462, 127484}, "emoji_1f1e6_1f1fc"),
            new Emoji(new int[]{127462, 127481}, "emoji_1f1e6_1f1f9"),
            new Emoji(new int[]{127462, 127487}, "emoji_1f1e6_1f1ff"),
            new Emoji(new int[]{127463, 127480}, "emoji_1f1e7_1f1f8"),
            new Emoji(new int[]{127463, 127469}, "emoji_1f1e7_1f1ed"),
            new Emoji(new int[]{127463, 127465}, "emoji_1f1e7_1f1e9"),
            new Emoji(new int[]{127463, 127463}, "emoji_1f1e7_1f1e7"),
            new Emoji(new int[]{127463, 127486}, "emoji_1f1e7_1f1fe"),
            new Emoji(new int[]{127463, 127466}, "emoji_1f1e7_1f1ea"),
            new Emoji(new int[]{127463, 127487}, "emoji_1f1e7_1f1ff"),
            new Emoji(new int[]{127463, 127471}, "emoji_1f1e7_1f1ef"),
            new Emoji(new int[]{127463, 127474}, "emoji_1f1e7_1f1f2"),
            new Emoji(new int[]{127463, 127481}, "emoji_1f1e7_1f1f9"),
            new Emoji(new int[]{127463, 127476}, "emoji_1f1e7_1f1f4"),
            new Emoji(new int[]{127463, 127462}, "emoji_1f1e7_1f1e6"),
            new Emoji(new int[]{127463, 127484}, "emoji_1f1e7_1f1fc"),
            new Emoji(new int[]{127463, 127479}, "emoji_1f1e7_1f1f7"),
            new Emoji(new int[]{127483, 127468}, "emoji_1f1fb_1f1ec"),
            new Emoji(new int[]{127463, 127475}, "emoji_1f1e7_1f1f3"),
            new Emoji(new int[]{127463, 127468}, "emoji_1f1e7_1f1ec"),
            new Emoji(new int[]{127463, 127467}, "emoji_1f1e7_1f1eb"),
            new Emoji(new int[]{127463, 127470}, "emoji_1f1e7_1f1ee"),
            new Emoji(new int[]{127472, 127469}, "emoji_1f1f0_1f1ed"),
            new Emoji(new int[]{127464, 127474}, "emoji_1f1e8_1f1f2"),
            new Emoji(new int[]{127464, 127462}, "emoji_1f1e8_1f1e6"),
            new Emoji(new int[]{127470, 127464}, "emoji_1f1ee_1f1e8"),
            new Emoji(new int[]{127464, 127483}, "emoji_1f1e8_1f1fb"),
            new Emoji(new int[]{127472, 127486}, "emoji_1f1f0_1f1fe"),
            new Emoji(new int[]{127464, 127467}, "emoji_1f1e8_1f1eb"),
            new Emoji(new int[]{127481, 127465}, "emoji_1f1f9_1f1e9"),
            new Emoji(new int[]{127464, 127473}, "emoji_1f1e8_1f1f1"),
            new Emoji(new int[]{127464, 127475}, "emoji_1f1e8_1f1f3"),
            new Emoji(new int[]{127464, 127485}, "emoji_1f1e8_1f1fd"),
            new Emoji(new int[]{127464, 127464}, "emoji_1f1e8_1f1e8"),
            new Emoji(new int[]{127464, 127476}, "emoji_1f1e8_1f1f4"),
            new Emoji(new int[]{127472, 127474}, "emoji_1f1f0_1f1f2"),
            new Emoji(new int[]{127464, 127468}, "emoji_1f1e8_1f1ec"),
            new Emoji(new int[]{127464, 127465}, "emoji_1f1e8_1f1e9"),
            new Emoji(new int[]{127464, 127472}, "emoji_1f1e8_1f1f0"),
            new Emoji(new int[]{127464, 127479}, "emoji_1f1e8_1f1f7"),
            new Emoji(new int[]{127464, 127470}, "emoji_1f1e8_1f1ee"),
            new Emoji(new int[]{127469, 127479}, "emoji_1f1ed_1f1f7"),
            new Emoji(new int[]{127464, 127482}, "emoji_1f1e8_1f1fa"),
            new Emoji(new int[]{127464, 127484}, "emoji_1f1e8_1f1fc"),
            new Emoji(new int[]{127464, 127486}, "emoji_1f1e8_1f1fe"),
            new Emoji(new int[]{127464, 127487}, "emoji_1f1e8_1f1ff"),
            new Emoji(new int[]{127465, 127472}, "emoji_1f1e9_1f1f0"),
            new Emoji(new int[]{127465, 127471}, "emoji_1f1e9_1f1ef"),
            new Emoji(new int[]{127465, 127474}, "emoji_1f1e9_1f1f2"),
            new Emoji(new int[]{127465, 127476}, "emoji_1f1e9_1f1f4"),
            new Emoji(new int[]{127466, 127464}, "emoji_1f1ea_1f1e8"),
            new Emoji(new int[]{127466, 127468}, "emoji_1f1ea_1f1ec"),
            new Emoji(new int[]{127480, 127483}, "emoji_1f1f8_1f1fb"),
            new Emoji(new int[]{127468, 127478}, "emoji_1f1ec_1f1f6"),
            new Emoji(new int[]{127466, 127479}, "emoji_1f1ea_1f1f7"),
            new Emoji(new int[]{127466, 127466}, "emoji_1f1ea_1f1ea"),
            new Emoji(new int[]{127466, 127481}, "emoji_1f1ea_1f1f9"),
            new Emoji(new int[]{127466, 127482}, "emoji_1f1ea_1f1fa"),
            new Emoji(new int[]{127467, 127476}, "emoji_1f1eb_1f1f4"),
            new Emoji(new int[]{127467, 127471}, "emoji_1f1eb_1f1ef"),
            new Emoji(new int[]{127467, 127470}, "emoji_1f1eb_1f1ee"),
            new Emoji(new int[]{127467, 127479}, "emoji_1f1eb_1f1f7"),
            new Emoji(new int[]{127477, 127467}, "emoji_1f1f5_1f1eb"),
            new Emoji(new int[]{127468, 127462}, "emoji_1f1ec_1f1e6"),
            new Emoji(new int[]{127468, 127474}, "emoji_1f1ec_1f1f2"),
            new Emoji(new int[]{127468, 127466}, "emoji_1f1ec_1f1ea"),
            new Emoji(new int[]{127465, 127466}, "emoji_1f1e9_1f1ea"),
            new Emoji(new int[]{127468, 127469}, "emoji_1f1ec_1f1ed"),
            new Emoji(new int[]{127468, 127470}, "emoji_1f1ec_1f1ee"),
            new Emoji(new int[]{127468, 127479}, "emoji_1f1ec_1f1f7"),
            new Emoji(new int[]{127468, 127473}, "emoji_1f1ec_1f1f1"),
            new Emoji(new int[]{127468, 127465}, "emoji_1f1ec_1f1e9"),
            new Emoji(new int[]{127468, 127482}, "emoji_1f1ec_1f1fa"),
            new Emoji(new int[]{127468, 127481}, "emoji_1f1ec_1f1f9"),
            new Emoji(new int[]{127468, 127468}, "emoji_1f1ec_1f1ec"),
            new Emoji(new int[]{127468, 127475}, "emoji_1f1ec_1f1f3"),
            new Emoji(new int[]{127468, 127484}, "emoji_1f1ec_1f1fc"),
            new Emoji(new int[]{127468, 127486}, "emoji_1f1ec_1f1fe"),
            new Emoji(new int[]{127469, 127481}, "emoji_1f1ed_1f1f9"),
            new Emoji(new int[]{127469, 127475}, "emoji_1f1ed_1f1f3"),
            new Emoji(new int[]{127469, 127472}, "emoji_1f1ed_1f1f0"),
            new Emoji(new int[]{127469, 127482}, "emoji_1f1ed_1f1fa"),
            new Emoji(new int[]{127470, 127480}, "emoji_1f1ee_1f1f8"),
            new Emoji(new int[]{127470, 127475}, "emoji_1f1ee_1f1f3"),
            new Emoji(new int[]{127470, 127465}, "emoji_1f1ee_1f1e9"),
            new Emoji(new int[]{127470, 127479}, "emoji_1f1ee_1f1f7"),
            new Emoji(new int[]{127470, 127478}, "emoji_1f1ee_1f1f6"),
            new Emoji(new int[]{127470, 127466}, "emoji_1f1ee_1f1ea"),
            new Emoji(new int[]{127470, 127474}, "emoji_1f1ee_1f1f2"),
            new Emoji(new int[]{127470, 127473}, "emoji_1f1ee_1f1f1"),
            new Emoji(new int[]{127470, 127481}, "emoji_1f1ee_1f1f9"),
            new Emoji(new int[]{127471, 127474}, "emoji_1f1ef_1f1f2"),
            new Emoji(new int[]{127471, 127477}, "emoji_1f1ef_1f1f5"),
            new Emoji(127884, "emoji_1f38c"),
            new Emoji(new int[]{127471, 127466}, "emoji_1f1ef_1f1ea"),
            new Emoji(new int[]{127471, 127476}, "emoji_1f1ef_1f1f4"),
            new Emoji(new int[]{127472, 127487}, "emoji_1f1f0_1f1ff"),
            new Emoji(new int[]{127472, 127466}, "emoji_1f1f0_1f1ea"),
            new Emoji(new int[]{127472, 127470}, "emoji_1f1f0_1f1ee"),
            new Emoji(new int[]{127472, 127484}, "emoji_1f1f0_1f1fc"),
            new Emoji(new int[]{127472, 127468}, "emoji_1f1f0_1f1ec"),
            new Emoji(new int[]{127473, 127462}, "emoji_1f1f1_1f1e6"),
            new Emoji(new int[]{127473, 127483}, "emoji_1f1f1_1f1fb"),
            new Emoji(new int[]{127473, 127463}, "emoji_1f1f1_1f1e7"),
            new Emoji(new int[]{127473, 127480}, "emoji_1f1f1_1f1f8"),
            new Emoji(new int[]{127473, 127479}, "emoji_1f1f1_1f1f7"),
            new Emoji(new int[]{127473, 127486}, "emoji_1f1f1_1f1fe"),
            new Emoji(new int[]{127473, 127470}, "emoji_1f1f1_1f1ee"),
            new Emoji(new int[]{127473, 127481}, "emoji_1f1f1_1f1f9"),
            new Emoji(new int[]{127473, 127482}, "emoji_1f1f1_1f1fa"),
            new Emoji(new int[]{127474, 127476}, "emoji_1f1f2_1f1f4"),
            new Emoji(new int[]{127474, 127472}, "emoji_1f1f2_1f1f0"),
            new Emoji(new int[]{127474, 127468}, "emoji_1f1f2_1f1ec"),
            new Emoji(new int[]{127474, 127484}, "emoji_1f1f2_1f1fc"),
            new Emoji(new int[]{127474, 127486}, "emoji_1f1f2_1f1fe"),
            new Emoji(new int[]{127474, 127483}, "emoji_1f1f2_1f1fb"),
            new Emoji(new int[]{127474, 127473}, "emoji_1f1f2_1f1f1"),
            new Emoji(new int[]{127474, 127481}, "emoji_1f1f2_1f1f9"),
            new Emoji(new int[]{127474, 127469}, "emoji_1f1f2_1f1ed"),
            new Emoji(new int[]{127474, 127479}, "emoji_1f1f2_1f1f7"),
            new Emoji(new int[]{127474, 127482}, "emoji_1f1f2_1f1fa"),
            new Emoji(new int[]{127474, 127485}, "emoji_1f1f2_1f1fd"),
            new Emoji(new int[]{127467, 127474}, "emoji_1f1eb_1f1f2"),
            new Emoji(new int[]{127474, 127465}, "emoji_1f1f2_1f1e9"),
            new Emoji(new int[]{127474, 127464}, "emoji_1f1f2_1f1e8"),
            new Emoji(new int[]{127474, 127475}, "emoji_1f1f2_1f1f3"),
            new Emoji(new int[]{127474, 127466}, "emoji_1f1f2_1f1ea"),
            new Emoji(new int[]{127474, 127480}, "emoji_1f1f2_1f1f8"),
            new Emoji(new int[]{127474, 127462}, "emoji_1f1f2_1f1e6"),
            new Emoji(new int[]{127474, 127487}, "emoji_1f1f2_1f1ff"),
            new Emoji(new int[]{127474, 127474}, "emoji_1f1f2_1f1f2"),
            new Emoji(new int[]{127475, 127462}, "emoji_1f1f3_1f1e6"),
            new Emoji(new int[]{127475, 127479}, "emoji_1f1f3_1f1f7"),
            new Emoji(new int[]{127475, 127477}, "emoji_1f1f3_1f1f5"),
            new Emoji(new int[]{127475, 127473}, "emoji_1f1f3_1f1f1"),
            new Emoji(new int[]{127475, 127487}, "emoji_1f1f3_1f1ff"),
            new Emoji(new int[]{127475, 127470}, "emoji_1f1f3_1f1ee"),
            new Emoji(new int[]{127475, 127466}, "emoji_1f1f3_1f1ea"),
            new Emoji(new int[]{127475, 127468}, "emoji_1f1f3_1f1ec"),
            new Emoji(new int[]{127475, 127482}, "emoji_1f1f3_1f1fa"),
            new Emoji(new int[]{127475, 127467}, "emoji_1f1f3_1f1eb"),
            new Emoji(new int[]{127472, 127477}, "emoji_1f1f0_1f1f5"),
            new Emoji(new int[]{127474, 127477}, "emoji_1f1f2_1f1f5"),
            new Emoji(new int[]{127476, 127474}, "emoji_1f1f4_1f1f2"),
            new Emoji(new int[]{127477, 127472}, "emoji_1f1f5_1f1f0"),
            new Emoji(new int[]{127477, 127484}, "emoji_1f1f5_1f1fc"),
            new Emoji(new int[]{127477, 127480}, "emoji_1f1f5_1f1f8"),
            new Emoji(new int[]{127477, 127462}, "emoji_1f1f5_1f1e6"),
            new Emoji(new int[]{127477, 127468}, "emoji_1f1f5_1f1ec"),
            new Emoji(new int[]{127477, 127486}, "emoji_1f1f5_1f1fe"),
            new Emoji(new int[]{127477, 127466}, "emoji_1f1f5_1f1ea"),
            new Emoji(new int[]{127477, 127469}, "emoji_1f1f5_1f1ed"),
            new Emoji(new int[]{127477, 127475}, "emoji_1f1f5_1f1f3"),
            new Emoji(new int[]{127477, 127473}, "emoji_1f1f5_1f1f1"),
            new Emoji(new int[]{127477, 127481}, "emoji_1f1f5_1f1f9"),
            new Emoji(new int[]{127477, 127479}, "emoji_1f1f5_1f1f7"),
            new Emoji(new int[]{127478, 127462}, "emoji_1f1f6_1f1e6"),
            new Emoji(new int[]{127479, 127476}, "emoji_1f1f7_1f1f4"),
            new Emoji(new int[]{127479, 127482}, "emoji_1f1f7_1f1fa"),
            new Emoji(new int[]{127479, 127484}, "emoji_1f1f7_1f1fc"),
            new Emoji(new int[]{127484, 127480}, "emoji_1f1fc_1f1f8"),
            new Emoji(new int[]{127480, 127474}, "emoji_1f1f8_1f1f2"),
            new Emoji(new int[]{127480, 127481}, "emoji_1f1f8_1f1f9"),
            new Emoji(new int[]{127480, 127462}, "emoji_1f1f8_1f1e6"),
            new Emoji(new int[]{127480, 127475}, "emoji_1f1f8_1f1f3"),
            new Emoji(new int[]{127479, 127480}, "emoji_1f1f7_1f1f8"),
            new Emoji(new int[]{127480, 127464}, "emoji_1f1f8_1f1e8"),
            new Emoji(new int[]{127480, 127473}, "emoji_1f1f8_1f1f1"),
            new Emoji(new int[]{127480, 127468}, "emoji_1f1f8_1f1ec"),
            new Emoji(new int[]{127480, 127485}, "emoji_1f1f8_1f1fd"),
            new Emoji(new int[]{127480, 127472}, "emoji_1f1f8_1f1f0"),
            new Emoji(new int[]{127480, 127470}, "emoji_1f1f8_1f1ee"),
            new Emoji(new int[]{127480, 127463}, "emoji_1f1f8_1f1e7"),
            new Emoji(new int[]{127480, 127476}, "emoji_1f1f8_1f1f4"),
            new Emoji(new int[]{127487, 127462}, "emoji_1f1ff_1f1e6"),
            new Emoji(new int[]{127472, 127479}, "emoji_1f1f0_1f1f7"),
            new Emoji(new int[]{127480, 127480}, "emoji_1f1f8_1f1f8"),
            new Emoji(new int[]{127473, 127472}, "emoji_1f1f1_1f1f0"),
            new Emoji(new int[]{127480, 127469}, "emoji_1f1f8_1f1ed"),
            new Emoji(new int[]{127472, 127475}, "emoji_1f1f0_1f1f3"),
            new Emoji(new int[]{127473, 127464}, "emoji_1f1f1_1f1e8"),
            new Emoji(new int[]{127483, 127464}, "emoji_1f1fb_1f1e8"),
            new Emoji(new int[]{127480, 127465}, "emoji_1f1f8_1f1e9"),
            new Emoji(new int[]{127480, 127479}, "emoji_1f1f8_1f1f7"),
            new Emoji(new int[]{127480, 127487}, "emoji_1f1f8_1f1ff"),
            new Emoji(new int[]{127480, 127466}, "emoji_1f1f8_1f1ea"),
            new Emoji(new int[]{127464, 127469}, "emoji_1f1e8_1f1ed"),
            new Emoji(new int[]{127480, 127486}, "emoji_1f1f8_1f1fe"),
            new Emoji(new int[]{127481, 127484}, "emoji_1f1f9_1f1fc"),
            new Emoji(new int[]{127481, 127471}, "emoji_1f1f9_1f1ef"),
            new Emoji(new int[]{127481, 127487}, "emoji_1f1f9_1f1ff"),
            new Emoji(new int[]{127481, 127469}, "emoji_1f1f9_1f1ed"),
            new Emoji(new int[]{127481, 127473}, "emoji_1f1f9_1f1f1"),
            new Emoji(new int[]{127481, 127468}, "emoji_1f1f9_1f1ec"),
            new Emoji(new int[]{127481, 127472}, "emoji_1f1f9_1f1f0"),
            new Emoji(new int[]{127481, 127476}, "emoji_1f1f9_1f1f4"),
            new Emoji(new int[]{127481, 127481}, "emoji_1f1f9_1f1f9"),
            new Emoji(new int[]{127481, 127475}, "emoji_1f1f9_1f1f3"),
            new Emoji(new int[]{127481, 127479}, "emoji_1f1f9_1f1f7"),
            new Emoji(new int[]{127481, 127474}, "emoji_1f1f9_1f1f2"),
            new Emoji(new int[]{127481, 127464}, "emoji_1f1f9_1f1e8"),
            new Emoji(new int[]{127481, 127483}, "emoji_1f1f9_1f1fb"),
            new Emoji(new int[]{127483, 127470}, "emoji_1f1fb_1f1ee"),
            new Emoji(new int[]{127482, 127468}, "emoji_1f1fa_1f1ec"),
            new Emoji(new int[]{127482, 127462}, "emoji_1f1fa_1f1e6"),
            new Emoji(new int[]{127462, 127466}, "emoji_1f1e6_1f1ea"),
            new Emoji(new int[]{127468, 127463}, "emoji_1f1ec_1f1e7"),
            new Emoji(new int[]{127482, 127486}, "emoji_1f1fa_1f1fe"),
            new Emoji(new int[]{127482, 127487}, "emoji_1f1fa_1f1ff"),
            new Emoji(new int[]{127483, 127482}, "emoji_1f1fb_1f1fa"),
            new Emoji(new int[]{127483, 127462}, "emoji_1f1fb_1f1e6"),
            new Emoji(new int[]{127483, 127466}, "emoji_1f1fb_1f1ea"),
            new Emoji(new int[]{127483, 127475}, "emoji_1f1fb_1f1f3"),
            new Emoji(new int[]{127486, 127466}, "emoji_1f1fe_1f1ea"),
            new Emoji(new int[]{127487, 127474}, "emoji_1f1ff_1f1f2"),
            new Emoji(new int[]{127487, 127484}, "emoji_1f1ff_1f1fc"),
            new Emoji(new int[]{127462, 127464}, "emoji_1f1e6_1f1e8")
    };

    public Emoji[] getEmojis() {
        return DATA;
    }
}

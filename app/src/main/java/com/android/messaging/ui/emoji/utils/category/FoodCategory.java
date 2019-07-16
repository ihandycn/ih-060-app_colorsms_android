package com.android.messaging.ui.emoji.utils.category;


import com.android.messaging.R;
import com.android.messaging.ui.emoji.utils.emoji.Emoji;
import com.android.messaging.ui.emoji.utils.emoji.EmojiCategory;

public final class FoodCategory implements EmojiCategory {
    private static final Emoji[] DATA = new Emoji[]{new Emoji(127823, "emoji_1f34f"),
            new Emoji(127822, "emoji_1f34e"),
            new Emoji(127824, "emoji_1f350"),
            new Emoji(127818, "emoji_1f34a"),
            new Emoji(127819, "emoji_1f34b"),
            new Emoji(127820, "emoji_1f34c"),
            new Emoji(127817, "emoji_1f349"),
            new Emoji(127815, "emoji_1f347"),
            new Emoji(127827, "emoji_1f353"),
            new Emoji(127816, "emoji_1f348"),
            new Emoji(127826, "emoji_1f352"),
            new Emoji(127825, "emoji_1f351"),
            new Emoji(127821, "emoji_1f34d"),
            new Emoji(129373, "emoji_1f95d"),
            new Emoji(129361, "emoji_1f951"),
            new Emoji(127813, "emoji_1f345"),
            new Emoji(127814, "emoji_1f346"),
            new Emoji(129362, "emoji_1f952"),
            new Emoji(129365, "emoji_1f955"),
            new Emoji(127805, "emoji_1f33d"),
            new Emoji(127798, "emoji_1f336"),
            new Emoji(129364, "emoji_1f954"),
            new Emoji(127840, "emoji_1f360"),
            new Emoji(127792, "emoji_1f330"),
            new Emoji(129372, "emoji_1f95c"),
            new Emoji(127855, "emoji_1f36f"),
            new Emoji(129360, "emoji_1f950"),
            new Emoji(127838, "emoji_1f35e"),
            new Emoji(129366, "emoji_1f956"),
            new Emoji(129472, "emoji_1f9c0"),
            new Emoji(129370, "emoji_1f95a"),
            new Emoji(127859, "emoji_1f373"),
            new Emoji(129363, "emoji_1f953"),
            new Emoji(129374, "emoji_1f95e"),
            new Emoji(127844, "emoji_1f364"),
            new Emoji(127831, "emoji_1f357"),
            new Emoji(127830, "emoji_1f356"),
            new Emoji(127829, "emoji_1f355"),
            new Emoji(127789, "emoji_1f32d"),
            new Emoji(127828, "emoji_1f354"),
            new Emoji(127839, "emoji_1f35f"),
            new Emoji(129369, "emoji_1f959"),
            new Emoji(127790, "emoji_1f32e"),
            new Emoji(127791, "emoji_1f32f"),
            new Emoji(129367, "emoji_1f957"),
            new Emoji(129368, "emoji_1f958"),
            new Emoji(127837, "emoji_1f35d"),
            new Emoji(127836, "emoji_1f35c"),
            new Emoji(127858, "emoji_1f372"),
            new Emoji(127845, "emoji_1f365"),
            new Emoji(127843, "emoji_1f363"),
            new Emoji(127857, "emoji_1f371"),
            new Emoji(127835, "emoji_1f35b"),
            new Emoji(127833, "emoji_1f359"),
            new Emoji(127834, "emoji_1f35a"),
            new Emoji(127832, "emoji_1f358"),
            new Emoji(127842, "emoji_1f362"),
            new Emoji(127841, "emoji_1f361"),
            new Emoji(127847, "emoji_1f367"),
            new Emoji(127848, "emoji_1f368"),
            new Emoji(127846, "emoji_1f366"),
            new Emoji(127856, "emoji_1f370"),
            new Emoji(127874, "emoji_1f382"),
            new Emoji(127854, "emoji_1f36e"),
            new Emoji(127853, "emoji_1f36d"),
            new Emoji(127852, "emoji_1f36c"),
            new Emoji(127851, "emoji_1f36b"),
            new Emoji(127871, "emoji_1f37f"),
            new Emoji(127849, "emoji_1f369"),
            new Emoji(127850, "emoji_1f36a"),
            new Emoji(129371, "emoji_1f95b"),
            new Emoji(127868, "emoji_1f37c"),
            new Emoji(9749, "emoji_2615"),
            new Emoji(127861, "emoji_1f375"),
            new Emoji(127862, "emoji_1f376"),
            new Emoji(127866, "emoji_1f37a"),
            new Emoji(127867, "emoji_1f37b"),
            new Emoji(129346, "emoji_1f942"),
            new Emoji(127863, "emoji_1f377"),
            new Emoji(129347, "emoji_1f943"),
            new Emoji(127864, "emoji_1f378"),
            new Emoji(127865, "emoji_1f379"),
            new Emoji(127870, "emoji_1f37e"),
            new Emoji(129348, "emoji_1f944"),
            new Emoji(127860, "emoji_1f374"),
            new Emoji(127869, "emoji_1f37d")};

    @Override
    public int getIcon() {
        return R.drawable.emoji_category_food;
    }

    @Override
    public int getIconSelected() {
        return R.drawable.emoji_category_food_selected;
    }

    public Emoji[] getEmojis() {
        return DATA;
    }
}

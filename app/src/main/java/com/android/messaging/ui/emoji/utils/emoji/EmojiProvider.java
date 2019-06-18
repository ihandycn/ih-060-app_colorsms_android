package com.android.messaging.ui.emoji.utils.emoji;


import com.android.messaging.ui.emoji.utils.category.ActivityCategory;
import com.android.messaging.ui.emoji.utils.category.FlagsCategory;
import com.android.messaging.ui.emoji.utils.category.FoodCategory;
import com.android.messaging.ui.emoji.utils.category.NatureCategory;
import com.android.messaging.ui.emoji.utils.category.ObjectsCategory;
import com.android.messaging.ui.emoji.utils.category.PeopleCategory;
import com.android.messaging.ui.emoji.utils.category.SymbolsCategory;
import com.android.messaging.ui.emoji.utils.category.TravelCategory;

public final class  EmojiProvider{

    public static EmojiCategory[] getCategories() {
        return new EmojiCategory[]{new PeopleCategory(), new NatureCategory(), new FoodCategory(), new ActivityCategory(), new TravelCategory(), new ObjectsCategory(), new SymbolsCategory(), new FlagsCategory()};
    }
}

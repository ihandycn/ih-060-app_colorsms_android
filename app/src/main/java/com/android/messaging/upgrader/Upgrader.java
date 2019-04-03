package com.android.messaging.upgrader;

import android.content.Context;
import android.database.Cursor;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.util.BuglePrefs;
import com.superapps.font.FontStyleManager;
import com.superapps.util.Preferences;

public class Upgrader extends BaseUpgrader {

    private static Upgrader sUpgrader;

    public static Upgrader getUpgrader(Context context) {
        if (sUpgrader == null) {
            sUpgrader = new Upgrader(context);
        }
        return sUpgrader;
    }

    public Upgrader(Context context) {
        super(context);
    }

    @Override
    protected void onAppUpgrade(int oldVersion, int newVersion) {
        if (oldVersion <= 12 && newVersion > 12) {
            Preferences.getDefault().putBoolean(ConversationListActivity.PREF_KEY_MAIN_DRAWER_OPENED, false);
        }

        if (oldVersion == 13 && newVersion > 13) {
            int fontLevel = BuglePrefs.getApplicationPrefs().getInt("message_font_scale", 2);
            FontStyleManager.getInstance().setFontScaleLevel(fontLevel);
        }

        if (oldVersion < 25 && newVersion >= 25) {
            addPinColumnInDB();
        }
    }

    public static void addPinColumnInDB() {
        final DatabaseWrapper db = DataModel.get().getDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.CONVERSATIONS_TABLE + " LIMIT 0"
                    , null);
            if (cursor != null && cursor.getColumnIndex(DatabaseHelper.ConversationColumns.PIN_TIMESTAMP) == -1) {
                db.execSQL("ALTER TABLE " + DatabaseHelper.CONVERSATIONS_TABLE
                        + " ADD COLUMN " + DatabaseHelper.ConversationColumns.PIN_TIMESTAMP
                        + " INT DEFAULT(0)");
            }
        } catch (Exception e) {

        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        DatabaseHelper.rebuildView(db, ConversationListItemData.getConversationListView(),
                ConversationListItemData.getConversationListViewSql());
    }

    private void migrateLong(Preferences from, Preferences to, String key) {
        to.putLong(key, from.getLong(key, 0L));
    }
}

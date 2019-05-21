package com.android.messaging.upgrader;

import android.content.Context;
import android.database.Cursor;

import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.privatebox.PrivateContactsManager;
import com.android.messaging.privatebox.PrivateMmsEntry;
import com.android.messaging.privatebox.PrivateSmsEntry;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.AvatarBgDrawables;
import com.android.messaging.ui.welcome.WelcomeChooseThemeActivity;
import com.android.messaging.ui.welcome.WelcomeStartActivity;
import com.android.messaging.util.BuglePrefs;
import com.ihs.commons.config.HSConfig;
import com.android.messaging.font.FontStyleManager;
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

        if (oldVersion < 42 && newVersion >= 42) {
            addIsPrivateColumnInConversationTable(oldVersion >= 25);
            createPrivateBoxTables();
        }

        if (oldVersion < 25 && newVersion >= 25) {
            addPinColumnInDB();
            Preferences.getDefault().putBoolean(WelcomeStartActivity.PREF_KEY_START_BUTTON_CLICKED,
                    !Preferences.getDefault().getBoolean("pref_key_first_launch", true));
        }

        if (oldVersion < 28 && newVersion >= 28) {
            AvatarBgDrawables.applyAvatarBg(HSConfig.optString("", "Application", "Themes", "Default", "AvatarUrl"));
            Preferences.getDefault().putBoolean(WelcomeChooseThemeActivity.PREF_KEY_WELCOME_CHOOSE_THEME_SHOWN, true);
        }

    }

    public static void addPinColumnInDB() {
        final DatabaseWrapper db = DataModel.get().getDatabaseWithoutMainCheck();
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

    public static void addIsPrivateColumnInConversationTable(boolean rebuildView) {
        final DatabaseWrapper db = DataModel.get().getDatabaseWithoutMainCheck();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.CONVERSATIONS_TABLE + " LIMIT 0"
                    , null);
            if (cursor != null && cursor.getColumnIndex(DatabaseHelper.ConversationColumns.IS_PRIVATE) == -1) {
                db.execSQL("ALTER TABLE " + DatabaseHelper.CONVERSATIONS_TABLE
                        + " ADD COLUMN " + DatabaseHelper.ConversationColumns.IS_PRIVATE
                        + " INT DEFAULT(0)");
            }
        } catch (Exception e) {

        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        if (rebuildView) {
            DatabaseHelper.rebuildView(db, ConversationListItemData.getConversationListView(),
                    ConversationListItemData.getConversationListViewSql());
        }
    }

    public static void createPrivateBoxTables() {
        final DatabaseWrapper db = DataModel.get().getDatabaseWithoutMainCheck();
        try {
            db.execSQL(PrivateMmsEntry.CREATE_MMS_TABLE_SQL);
            db.execSQL(PrivateSmsEntry.CREATE_SMS_TABLE_SQL);
            db.execSQL(PrivateContactsManager.CREATE_PRIVATE_CONTACTS_TABLE_SQL);
            db.execSQL(PrivateMmsEntry.Addr.CREATE_MMS_ADDRESS_TABLE_SQL);
        } catch (Exception e) {

        }
    }

    private void migrateLong(Preferences from, Preferences to, String key) {
        to.putLong(key, from.getLong(key, 0L));
    }
}

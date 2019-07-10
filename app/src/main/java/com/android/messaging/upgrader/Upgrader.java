package com.android.messaging.upgrader;

import android.content.Context;
import android.database.Cursor;

import com.android.messaging.Factory;
import com.android.messaging.R;
import com.android.messaging.datamodel.DataModel;
import com.android.messaging.datamodel.DatabaseHelper;
import com.android.messaging.datamodel.DatabaseWrapper;
import com.android.messaging.datamodel.data.ConversationListItemData;
import com.android.messaging.font.FontDownloadManager;
import com.android.messaging.font.FontStyleManager;
import com.android.messaging.font.FontUtils;
import com.android.messaging.ui.conversationlist.ConversationListActivity;
import com.android.messaging.ui.customize.theme.ThemeDownloadManager;
import com.android.messaging.ui.customize.theme.ThemeInfo;
import com.android.messaging.ui.customize.theme.ThemeUtils;
import com.android.messaging.ui.welcome.WelcomeChooseThemeActivity;
import com.android.messaging.ui.welcome.WelcomeStartActivity;
import com.android.messaging.util.BuglePrefs;
import com.android.messaging.util.BuglePrefsKeys;
import com.android.messaging.util.PhoneUtils;
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
            Preferences.getDefault().putBoolean(WelcomeStartActivity.PREF_KEY_START_BUTTON_CLICKED,
                    !Preferences.getDefault().getBoolean("pref_key_first_launch", true));
        }

        if (oldVersion < 28 && newVersion >= 28) {
            Preferences.getDefault().putBoolean(WelcomeChooseThemeActivity.PREF_KEY_WELCOME_CHOOSE_THEME_SHOWN, true);
        }

        if (oldVersion < 46 && newVersion >= 46) {
            updateThemeKey();
        }

        if (oldVersion < 50 && newVersion >= 50) {
            migrateLocalThemeAndFont();
        }

        if (oldVersion < 62 && newVersion >= 62) {
            addDeliveryReportPref();
        }

        FontDownloadManager.copyFontsFromAssetsAsync();
    }

    private void addDeliveryReportPref() {
        Context context = Factory.get().getApplicationContext();
        int subId = PhoneUtils.getDefault().getDefaultSmsSubscriptionId();
        final BuglePrefs prefs = BuglePrefs.getSubscriptionPrefs(subId);
        final String deliveryReportKey = context.getResources().getString(R.string.delivery_reports_pref_key);
        final boolean defaultValue = context.getResources().getBoolean(R.bool.delivery_reports_pref_default);
        boolean originalValue = prefs.getBoolean(deliveryReportKey, defaultValue);
        if (originalValue != defaultValue) {
            Preferences preferences = Preferences.getDefault();
            preferences.putBoolean(deliveryReportKey, originalValue);
        }
    }

    private void migrateLocalThemeAndFont() {
        String currentFontName = Preferences.getDefault().
                getString(FontStyleManager.PREF_KEY_MESSAGE_FONT_TYPE, FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE);
        if ("ExpletusSans".equals(currentFontName) || "SourceSerifPro".equals(currentFontName)) {
            Preferences.getDefault().putString(FontStyleManager.PREF_KEY_MESSAGE_FONT_TYPE, FontUtils.MESSAGE_FONT_FAMILY_DEFAULT_VALUE);
        }

        ThemeInfo currentTheme = ThemeUtils.getCurrentTheme();
        if (currentTheme.isNecessaryFilesInLocalFolder()) {
            return;
        }

        if (currentTheme.mIsLocalTheme) {
            ThemeDownloadManager.getInstance().copyFileFromAssetsSync(currentTheme,
                    new ThemeDownloadManager.IThemeMoveListener() {
                        @Override
                        public void onMoveSuccess() {

                        }

                        @Override
                        public void onMoveFailed() {

                        }
                    });
        } else {
            ThemeUtils.applyTheme(ThemeInfo.getThemeInfo(ThemeUtils.DEFAULT_THEME_KEY), 0);
            Preferences.getDefault().putBoolean(BuglePrefsKeys.PREFS_KEY_THEME_CLEARED_TO_DEFAULT, true);
        }
    }

    private void addPinColumnInDB() {
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
        } catch (Exception ignored) {

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

    private void updateThemeKey() {
        String themeName = Factory.get().getCustomizePrefs()
                .getString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, ThemeUtils.DEFAULT_THEME_KEY);
        if (!ThemeUtils.DEFAULT_THEME_KEY.equals(themeName)) {
            String newKey = ThemeUtils.DEFAULT_THEME_KEY;
            switch (themeName) {
                case "CuteGraffiti":
                    newKey = "cutegraffiti";
                    break;
                case "CoolGraffiti":
                    newKey = "coolgraffiti";
                    break;
                case "Starry":
                    newKey = "starry";
                    break;
                case "Unicorn":
                    newKey = "unicorn";
                    break;
                case "Technology":
                    newKey = "technology";
                    break;
                case "Diamond":
                    newKey = "diamond";
                    break;
                case "Neno":
                    newKey = "neon";
                    break;
                case "SimpleBusiness":
                    newKey = "simplebusiness";
                    break;
                case "WaterDrop":
                    newKey = "waterdrop";
                    break;
                case "GoldenDiamond":
                    newKey = "goldendiamond";
                    break;
            }
            Factory.get().getCustomizePrefs()
                    .putString(BuglePrefsKeys.PREFS_KEY_THEME_NAME, newKey);
        }
    }

    private void migrateLong(Preferences from, Preferences to, String key) {
        to.putLong(key, from.getLong(key, 0L));
    }
}

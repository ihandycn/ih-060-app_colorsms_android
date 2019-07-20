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
package com.android.messaging.datamodel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.messaging.Factory;
import com.android.messaging.backup.BackupDatabaseHelper;
import com.android.messaging.privatebox.PrivateContactsManager;
import com.android.messaging.privatebox.PrivateMmsEntry;
import com.android.messaging.privatebox.PrivateSmsEntry;
import com.android.messaging.util.Assert;
import com.android.messaging.util.LogUtil;

public class DatabaseUpgradeHelper {

    private static final String TAG = LogUtil.BUGLE_DATABASE_TAG;

    public void doOnUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Assert.isTrue(newVersion >= oldVersion);
        if (oldVersion == newVersion) {
            return;
        }

        LogUtil.i(TAG, "Database upgrade started from version " + oldVersion + " to " + newVersion);
        try {
            doUpgradeWithExceptions(db, oldVersion, newVersion);
            LogUtil.i(TAG, "Finished database upgrade");
        } catch (final Exception ex) {
            LogUtil.e(TAG, "Failed to perform db upgrade from version " +
                    oldVersion + " to version " + newVersion, ex);
            DatabaseHelper.rebuildTables(db);
        }
    }

    public void doUpgradeWithExceptions(final SQLiteDatabase db, final int oldVersion,
                                        final int newVersion) throws Exception {
        int currentVersion = oldVersion;
        if (currentVersion < 2) {
            currentVersion = upgradeToVersion2(db);
        }
        if (currentVersion < 3) {
            currentVersion = upgradeToVersion3(db);
        }
        if (currentVersion < 4) {
            currentVersion = upgradeToVersion4(db);
        }
        if (currentVersion < 5) {
            currentVersion = upgradeToVersion5(db);
        }
        if (currentVersion < 6) {
            currentVersion = upgradeToVersion6(db);
        }
        if (currentVersion < 7) {
            currentVersion = upgradeToVersion7(db);
        }

        // Rebuild all the views
        final Context context = Factory.get().getApplicationContext();
        DatabaseHelper.dropAllViews(db);
        DatabaseHelper.rebuildAllViews(new DatabaseWrapper(context, db));
        // Finally, check if we have arrived at the final version.
        checkAndUpdateVersionAtReleaseEnd(currentVersion, Integer.MAX_VALUE, newVersion);
    }

    private int upgradeToVersion2(final SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DatabaseHelper.CONVERSATIONS_TABLE + " ADD COLUMN " +
                DatabaseHelper.ConversationColumns.IS_ENTERPRISE + " INT DEFAULT(0)");
        LogUtil.i(TAG, "Ugraded database to version 2");
        return 2;
    }

    private int upgradeToVersion3(SQLiteDatabase db) {
        LogUtil.d("lock_test", "add_db_column");
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.MESSAGES_TABLE + " LIMIT 0"
                    , null);
            if (cursor != null && cursor.getColumnIndex(DatabaseHelper.MessageColumns.IS_LOCKED) == -1) {
                db.execSQL("ALTER TABLE " + DatabaseHelper.MESSAGES_TABLE
                        + " ADD COLUMN " + DatabaseHelper.MessageColumns.IS_LOCKED
                        + " INT DEFAULT(0)");
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return 3;
    }

    private int upgradeToVersion4(SQLiteDatabase db) {
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


        try {
            db.execSQL(PrivateMmsEntry.CREATE_MMS_TABLE_SQL);
            db.execSQL(PrivateSmsEntry.CREATE_SMS_TABLE_SQL);
            db.execSQL(PrivateContactsManager.CREATE_PRIVATE_CONTACTS_TABLE_SQL);
            db.execSQL(PrivateMmsEntry.Addr.CREATE_MMS_ADDRESS_TABLE_SQL);
        } catch (Exception e) {
        }
        return 4;
    }

    private int upgradeToVersion5(SQLiteDatabase db) {
        try {
            db.execSQL(BackupDatabaseHelper.MessageColumn.CREATE_BACKUP_TABLE_SQL);
        } catch (Exception ignored) {

        }
        return 5;
    }

    private int upgradeToVersion6(SQLiteDatabase db) {
        LogUtil.d("delete", "delete_messages");
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.MESSAGES_TABLE + " LIMIT 0"
                    , null);
            if (cursor != null && cursor.getColumnIndex(DatabaseHelper.MessageColumns.IS_DELETED) == -1) {
                db.execSQL("ALTER TABLE " + DatabaseHelper.MESSAGES_TABLE
                        + " ADD COLUMN " + DatabaseHelper.MessageColumns.IS_DELETED
                        + " INT DEFAULT(0)");
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return 6;
    }

    private int upgradeToVersion7(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.MESSAGES_TABLE + " LIMIT 0"
                    , null);
            if (cursor != null && cursor.getColumnIndex(DatabaseHelper.MessageColumns.IS_DELIVERY_REPORT_OPEN) == -1) {
                db.execSQL("ALTER TABLE " + DatabaseHelper.MESSAGES_TABLE
                        + " ADD COLUMN " + DatabaseHelper.MessageColumns.IS_DELIVERY_REPORT_OPEN
                        + " INT DEFAULT(0)");
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return 7;
    }

    /**
     * Checks db version correctness at the end of each milestone release. If target database
     * version lies beyond the version range that the current release may handle, we snap the
     * current version to the end of the release, so that we may go on to the next release' upgrade
     * path. Otherwise, if target version is within reach of the current release, but we are not
     * at the target version, then throw an exception to force a table rebuild.
     */
    private int checkAndUpdateVersionAtReleaseEnd(final int currentVersion,
                                                  final int maxVersionForRelease, final int targetVersion) throws Exception {
        if (maxVersionForRelease < targetVersion) {
            // Target version is beyond the current release. Snap to max version for the
            // current release so we can go on to the upgrade path for the next release.
            return maxVersionForRelease;
        }

        // If we are here, this means the current release' upgrade handler should upgrade to
        // target version...
        if (currentVersion != targetVersion) {
            // No more upgrade handlers. So we can't possibly upgrade to the final version.
            throw new Exception("Missing upgrade handler from version " +
                    currentVersion + " to version " + targetVersion);
        }
        // Upgrade succeeded.
        return targetVersion;
    }

    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        DatabaseHelper.rebuildTables(db);
        LogUtil.e(TAG, "Database downgrade requested for version " +
                oldVersion + " version " + newVersion + ", forcing db rebuild!");
    }
}

//  pinguino
//  RETSworks.com, Copyright 2015
//

package com.retsworks.pinguino;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public final class SettingsContract {
    String TAG = "PINGUINO-SettingsContract";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SettingsEntry.TABLE_NAME + " (" +
                    SettingsEntry._ID + " INTEGER PRIMARY KEY," +
                    SettingsEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    SettingsEntry.COLUMN_NAME_PHOTOPATH + TEXT_TYPE + COMMA_SEP +
                    SettingsEntry.COLUMN_NAME_DEFAULT + TEXT_TYPE +
                    // Any other options for the CREATE command
                    " )";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SettingsEntry.TABLE_NAME;


    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public SettingsContract() {
    }


    /* Inner class that defines the table contents */
    public static abstract class SettingsEntry implements BaseColumns {
        public static final String TABLE_NAME = "settings";
        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_PHOTOPATH = "photopath";
        public static final String COLUMN_NAME_DEFAULT = "photodefault";
    }


    public Boolean saveSettings(Context context, PhotoEntry spSettings) {
        SettingsReaderDbHelper mDbHelper = new SettingsReaderDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Log.d(TAG, "saveSettings()");

//        Log.d(TAG,"saveSettings - hardcoded call to onCreate()");
//        mDbHelper.onCreate(db);

        ContentValues values = new ContentValues();
        values.put(SettingsEntry._ID, 1);
        values.put(SettingsEntry.COLUMN_NAME_ENTRY_ID, spSettings.getDefaultPhoto());
        values.put(SettingsEntry.COLUMN_NAME_PHOTOPATH, String.valueOf(spSettings.getPhotoPathName()));
        values.put(SettingsEntry.COLUMN_NAME_DEFAULT, spSettings.getDefaultPhoto());

        int count = (int) db.insertWithOnConflict(SettingsEntry.TABLE_NAME, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);

        Log.d(TAG, "saveSettings() - count:" + count);
        Log.d(TAG, "saveSettings() - values:" + values);
        db.close();
        return true;
    }


    public PhotoEntry loadSettings(Context context) {
        SettingsReaderDbHelper mDbHelper = new SettingsReaderDbHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Log.d(TAG, "loadSettings()");

        PhotoEntry sp = new PhotoEntry();

        Cursor c = db.rawQuery("SELECT * FROM settings", null);
        c.moveToFirst();

        if (c.getCount() > 0) {
            Log.d(TAG, "loadSettings() - getCount():" + c.getCount());
            sp.setDefaultPhoto(Boolean.valueOf(
                    c.getString(c.getColumnIndexOrThrow(SettingsEntry.COLUMN_NAME_DEFAULT))));
            sp.setEntryID(c.getString(
                    c.getColumnIndexOrThrow(SettingsEntry.COLUMN_NAME_ENTRY_ID)));
            sp.setPhotoPathName(c.getString(
                    c.getColumnIndexOrThrow(SettingsEntry.COLUMN_NAME_PHOTOPATH)));
        } else {
            Log.d(TAG, "loadSettings() - Load default config, first time app is run:" + c.getCount());
            sp.setPhotoPathName(HelperFunctions.loadDefaultPhoto(context));
            sp.setDefaultPhoto(true);
            sp.setEntryID("1");
        }
        db.close();
        return sp;
    }


    private class SettingsReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 5;
        public static final String DATABASE_NAME = "pinguino.db";

        public SettingsReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

            Log.d(TAG, "SettingsReaderDBHelper Constructor()");
        }
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG,"SettingsReaderDBHelper.onCreate()");
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // discard the data and start over
            Log.d(TAG,"SettingsReaderDBHelper.onUpgrade()");
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG,"SettingsReaderDBHelper.onDowngrade()");
            onUpgrade(db, oldVersion, newVersion);
        }
    }

}

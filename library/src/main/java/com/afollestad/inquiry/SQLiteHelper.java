package com.afollestad.inquiry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private final String TABLE;

    public SQLiteHelper(Context context, String databaseName, String table, String columns) {
        super(context, databaseName, null, DATABASE_VERSION);
        TABLE = table;
        if (TABLE != null && columns != null) {
            try {
                final String createStatement = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", table, columns);
                getWritableDatabase().execSQL(createStatement);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (TABLE == null) return;
        Log.w(SQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
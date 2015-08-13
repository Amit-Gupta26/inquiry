package com.afollestad.inquiry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private final String mTableName;

    public SQLiteHelper(Context context, String databaseName, String table, String columns) {
        super(context, databaseName, null, DATABASE_VERSION);
        mTableName = table;
        if (mTableName != null && columns != null) {
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
        if (mTableName == null) return;
        Log.w(SQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + mTableName);
        onCreate(db);
    }

    public final Cursor query(String[] projection, String selection,
                              String[] selectionArgs, String sortOrder) {
        return getReadableDatabase().query(mTableName, projection, selection, selectionArgs, null, null, sortOrder);
    }

    public final long insert(ContentValues values) {
        return getWritableDatabase().insert(mTableName, null, values);
    }

    public final int delete(String selection, String[] selectionArgs) {
        return getWritableDatabase().delete(mTableName, selection, selectionArgs);
    }

    public final int update(ContentValues values, String selection, String[] selectionArgs) {
        return getWritableDatabase().update(mTableName, values, selection, selectionArgs);
    }
}
package com.afollestad.inquiry;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.inquiry.data.Column;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class Table extends ContentProvider {

    private SQLiteHelper database;

    protected Table() {
    }

    @NonNull
    public abstract String databaseName();

    @NonNull
    public abstract String tableName();

    @NonNull
    public abstract String authority();

    @NonNull
    public abstract Column[] columns();

    @SuppressWarnings("ConstantConditions")
    @Override
    public final boolean onCreate() {
        if (databaseName() == null)
            throw new IllegalStateException("Database name is null.");
        final Column[] columns = columns();
        if (columns == null)
            throw new IllegalStateException("Columns are null.");
        else if (authority() == null)
            throw new IllegalStateException("Authority is null.");
        database = new SQLiteHelper(getContext(), databaseName(), tableName(), Column.arrayString(columns));
        return false;
    }

    @Override
    public final Cursor query(Uri uri, String[] projection, String selection,
                              String[] selectionArgs, String sortOrder) {
        return database.getReadableDatabase().query(tableName(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public final String getType(Uri uri) {
        return null;
    }

    @Override
    public final Uri insert(Uri uri, ContentValues values) {
        database.getWritableDatabase().insert(tableName(), null, values);
        return null;
    }

    @Override
    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.getWritableDatabase().delete(tableName(), selection, selectionArgs);
    }

    @Override
    public final int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.getWritableDatabase().update(tableName(), values, selection, selectionArgs);
    }
}
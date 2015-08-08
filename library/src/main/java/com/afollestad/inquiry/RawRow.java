package com.afollestad.inquiry;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class RawRow {

    private Cursor mCursor;

    protected RawRow(@NonNull Cursor cursor) {
        mCursor = cursor;
    }

    /* Get blob */

    @Nullable
    public byte[] getBlob(int columnIndex) {
        if (columnIndex == -1) return null;
        return mCursor.getBlob(columnIndex);
    }

    @Nullable
    public byte[] getBlob(@NonNull String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getBlob(colIndex);
    }

    /* Get string */

    public String getString(int columnIndex, @Nullable String defaultValue) {
        if (columnIndex == -1) return defaultValue;
        return mCursor.getString(columnIndex);
    }

    public String getString(int columnIndex) {
        return getString(columnIndex, null);
    }

    public String getString(@NonNull String columnName, @Nullable String defaultValue) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getString(colIndex, defaultValue);
    }

    public String getString(@NonNull String columnName) {
        return getString(columnName, null);
    }

    /* Get short */

    public short getShort(int columnIndex, short defaultValue) {
        if (columnIndex == -1) return defaultValue;
        return mCursor.getShort(columnIndex);
    }

    public short getShort(int columnIndex) {
        return getShort(columnIndex, Short.MIN_VALUE);
    }

    public short getShort(@NonNull String columnName, short defaultValue) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getShort(colIndex, defaultValue);
    }

    public short getShort(@NonNull String columnName) {
        return getShort(columnName, Short.MIN_VALUE);
    }

    /* Get int */

    public int getInt(int columnIndex, int defaultValue) {
        if (columnIndex == -1) return defaultValue;
        return mCursor.getInt(columnIndex);
    }

    public int getInt(int columnIndex) {
        return getInt(columnIndex, Integer.MIN_VALUE);
    }

    public int getInt(@NonNull String columnName, int defaultValue) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getInt(colIndex, defaultValue);
    }

    public int getInt(@NonNull String columnName) {
        return getInt(columnName, Integer.MIN_VALUE);
    }

    /* Get long */

    public long getLong(int columnIndex, long defaultValue) {
        if (columnIndex == -1) return defaultValue;
        return mCursor.getLong(columnIndex);
    }

    public long getLong(int columnIndex) {
        return getLong(columnIndex, Long.MIN_VALUE);
    }

    public long getLong(@NonNull String columnName, long defaultValue) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getLong(colIndex, defaultValue);
    }

    public long getLong(@NonNull String columnName) {
        return getLong(columnName, Long.MIN_VALUE);
    }

    /* Get double */

    public double getDouble(int columnIndex, double defaultValue) {
        if (columnIndex == -1) return defaultValue;
        return mCursor.getDouble(columnIndex);
    }

    public double getDouble(int columnIndex) {
        return getDouble(columnIndex, Double.MIN_VALUE);
    }

    public double getDouble(@NonNull String columnName, double defaultValue) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getDouble(colIndex, defaultValue);
    }

    public double getDouble(@NonNull String columnName) {
        return getDouble(columnName, Double.MIN_VALUE);
    }

    /* Get float */

    public float getFloat(int columnIndex, float defaultValue) {
        if (columnIndex == -1) return defaultValue;
        return mCursor.getFloat(columnIndex);
    }

    public float getFloat(int columnIndex) {
        return getFloat(columnIndex, Float.MIN_VALUE);
    }

    public float getFloat(@NonNull String columnName, float defaultValue) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getFloat(colIndex, defaultValue);
    }

    public float getFloat(@NonNull String columnName) {
        return getFloat(columnName, Float.MIN_VALUE);
    }

    /* Get boolean */

    public boolean getBoolean(int columnIndex, boolean defaultValue) {
        if (columnIndex == -1)
            return defaultValue;
        return mCursor.getInt(columnIndex) == 1;
    }

    public boolean getBoolean(int columnIndex) {
        return getBoolean(columnIndex, false);
    }

    public boolean getBoolean(@NonNull String columnName, boolean defaultValue) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getBoolean(colIndex, defaultValue);
    }

    public boolean getBoolean(@NonNull String columnName) {
        return getBoolean(columnName, false);
    }

    protected void denit() {
        mCursor = null;
    }
}
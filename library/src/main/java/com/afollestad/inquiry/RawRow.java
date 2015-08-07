package com.afollestad.inquiry;

import android.database.Cursor;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class RawRow {

    private Cursor mCursor;

    protected RawRow(Cursor cursor) {
        mCursor = cursor;
    }

    public byte[] getBlob(int columnIndex) {
        if (columnIndex == -1) return null;
        return mCursor.getBlob(columnIndex);
    }

    public byte[] getBlob(String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getBlob(colIndex);
    }

    public String getString(int columnIndex) {
        if (columnIndex == -1) return null;
        return mCursor.getString(columnIndex);
    }

    public String getString(String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getString(colIndex);
    }

    public short getShort(int columnIndex) {
        if (columnIndex == -1) return Short.MIN_VALUE;
        return mCursor.getShort(columnIndex);
    }

    public short getShort(String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getShort(colIndex);
    }

    public int getInt(int columnIndex) {
        if (columnIndex == -1) return Integer.MIN_VALUE;
        return mCursor.getInt(columnIndex);
    }

    public int getInt(String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getInt(colIndex);
    }

    public long getLong(int columnIndex) {
        if (columnIndex == -1) return Long.MIN_VALUE;
        return mCursor.getLong(columnIndex);
    }

    public long getLong(String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getLong(colIndex);
    }

    public double getDouble(int columnIndex) {
        if (columnIndex == -1) return Double.MIN_VALUE;
        return mCursor.getDouble(columnIndex);
    }

    public double getDouble(String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getDouble(colIndex);
    }

    public float getFloat(int columnIndex) {
        if (columnIndex == -1) return Float.MIN_VALUE;
        return mCursor.getFloat(columnIndex);
    }

    public float getFloat(String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getFloat(colIndex);
    }

    public boolean getBoolean(int columnIndex) {
        return columnIndex != -1 && mCursor.getInt(columnIndex) == 1;
    }

    public boolean getBoolean(String columnName) {
        int colIndex = mCursor.getColumnIndex(columnName);
        return getBoolean(colIndex);
    }

    protected void denit() {
        mCursor.close();
        mCursor = null;
    }
}
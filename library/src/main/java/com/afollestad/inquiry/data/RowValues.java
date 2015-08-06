package com.afollestad.inquiry.data;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class RowValues {

    private ContentValues mValues;

    public RowValues() {
        mValues = new ContentValues();
    }

    public RowValues put(@NonNull String column, @Nullable String value) {
        mValues.put(column, value);
        return this;
    }

    public RowValues put(@NonNull String column, @Nullable Byte value) {
        mValues.put(column, value);
        return this;
    }

    public RowValues put(@NonNull String column, @Nullable Short value) {
        mValues.put(column, value);
        return this;
    }

    public RowValues put(@NonNull String column, @Nullable Integer value) {
        mValues.put(column, value);
        return this;
    }

    public RowValues put(@NonNull String column, @Nullable Long value) {
        mValues.put(column, value);
        return this;
    }

    public RowValues put(@NonNull String column, @Nullable Float value) {
        mValues.put(column, value);
        return this;
    }

    public RowValues put(@NonNull String column, @Nullable Double value) {
        mValues.put(column, value);
        return this;
    }

    public RowValues put(@NonNull String column, @Nullable Boolean value) {
        mValues.put(column, value);
        return this;
    }

    public RowValues put(@NonNull String column, @Nullable byte[] value) {
        mValues.put(column, value);
        return this;
    }

    public ContentValues content() {
        return mValues;
    }

    public static ContentValues[] contentArray(@NonNull RowValues[] values) {
        ContentValues[] result = new ContentValues[values.length];
        for (int i = 0; i < values.length; i++)
            result[i] = values[i].content();
        return result;
    }

    @Override
    public String toString() {
        return content().toString();
    }
}

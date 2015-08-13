package com.afollestad.inquiry;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class Inquiry {

    private static Inquiry mInquiry;

    protected Context mContext;
    protected Handler mHandler;
    protected String mDatabaseName;

    private Inquiry() {
        mHandler = new Handler();
    }

    @NonNull
    public static Inquiry init(@NonNull Context context, @NonNull String databaseName) {
        //noinspection ConstantConditions
        if (context == null)
            throw new IllegalArgumentException("Context can't be null.");
        if (mInquiry == null)
            mInquiry = new Inquiry();
        mInquiry.mContext = context;
        mInquiry.mDatabaseName = databaseName;
        return mInquiry;
    }

    public static void deinit() {
        if (mInquiry != null) {
            mInquiry.mContext = null;
            mInquiry.mHandler = null;
            mInquiry.mDatabaseName = null;
            mInquiry = null;
        }
    }

    public void dropTable(@NonNull String tableName) {
        new SQLiteHelper(mContext, mDatabaseName, null, null)
                .getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + tableName);
    }

    @NonNull
    public static Inquiry get() {
        if (mInquiry == null)
            throw new IllegalStateException("Inquiry not initialized, or has been garbage collected.");
        return mInquiry;
    }

    @NonNull
    public <RowType> Query<RowType> selectFrom(@NonNull String table, @NonNull Class<RowType> rowType) {
        return new Query<>(this, table, Query.SELECT, rowType);
    }

    @NonNull
    public <RowType> Query<RowType> insertInto(@NonNull String table, @NonNull Class<RowType> rowType) {
        return new Query<>(this, table, Query.INSERT, rowType);
    }

    @NonNull
    public <RowType> Query<RowType> update(@NonNull String table, @NonNull Class<RowType> rowType) {
        return new Query<>(this, table, Query.UPDATE, rowType);
    }

    @NonNull
    public <RowType> Query<RowType> deleteFrom(@NonNull String table, @NonNull Class<RowType> rowType) {
        return new Query<>(this, table, Query.DELETE, rowType);
    }
}
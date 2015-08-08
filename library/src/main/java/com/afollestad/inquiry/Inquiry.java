package com.afollestad.inquiry;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.afollestad.inquiry.data.Row;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class Inquiry {

    private static Inquiry mInquiry;

    protected Context mContext;
    protected Handler mHandler;
    private Class<? extends Table>[] mTables;

    private Inquiry() {
        mHandler = new Handler();
    }

    @SafeVarargs
    @NonNull
    public static Inquiry init(@NonNull Context context, @NonNull Class<? extends Table>... tables) {
        //noinspection ConstantConditions
        if (context == null || tables == null)
            throw new IllegalArgumentException("No parameters can be null in init().");
        if (mInquiry == null)
            mInquiry = new Inquiry();
        mInquiry.mContext = context;
        mInquiry.mTables = tables;
        return mInquiry;
    }

    public static void deinit() {
        if (mInquiry != null) {
            mInquiry.mContext = null;
            mInquiry.mTables = null;
            mInquiry.mHandler = null;
            mInquiry = null;
        }
    }

    public void dropTable(@NonNull String databaseName, @NonNull String tableName) {
        new SQLiteHelper(mContext, databaseName, null, null)
                .getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + tableName);
    }

    @NonNull
    public static Inquiry get() {
        if (mInquiry == null)
            throw new IllegalStateException("Inquiry not initialized, or has been garbage collected.");
        return mInquiry;
    }

    protected synchronized Uri findTableUri(String tableName) {
        if (mTables == null || mTables.length == 0)
            throw new IllegalStateException("No tables have been added to this Inquiry object.");
        for (final Class<? extends Table> tc : mTables) {
            final Table table = (Table) Util.newInstance(tc);
            if (table.tableName().equals(tableName))
                return Uri.parse(String.format("content://%s", table.authority()));
        }
        throw new IllegalStateException("No table found by the name of " + tableName);
    }

    @NonNull
    public <RowType extends Row> Query<RowType> selectFrom(@NonNull String table, @NonNull Class<RowType> rowType) {
        return new Query<>(this, findTableUri(table), Query.SELECT, rowType);
    }

    @NonNull
    public Query<?> insertInto(@NonNull String table) {
        return new Query<>(this, findTableUri(table), Query.INSERT, null);
    }

    @NonNull
    public Query<?> update(@NonNull String table) {
        return new Query<>(this, findTableUri(table), Query.UPDATE, null);
    }

    @NonNull
    public Query<?> deleteFrom(@NonNull String table) {
        return new Query<>(this, findTableUri(table), Query.DELETE, null);
    }
}
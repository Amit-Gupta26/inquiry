package com.afollestad.inquiry;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.inquiry.callbacks.GetCallback;
import com.afollestad.inquiry.callbacks.RunCallback;

import java.lang.reflect.Array;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class Query<RowType> {

    protected final static int SELECT = 1;
    protected final static int INSERT = 2;
    protected final static int UPDATE = 3;
    protected final static int DELETE = 4;

    private final Inquiry mInquiry;
    private final int mQueryType;
    @Nullable
    private final Class<RowType> mRowClass;

    protected Query(@NonNull Inquiry inquiry, @NonNull String tableName, int type, @Nullable Class<RowType> mClass) {
        mInquiry = inquiry;
        mQueryType = type;
        mRowClass = mClass;
        mDatabase = new SQLiteHelper(inquiry.mContext, inquiry.mDatabaseName,
                tableName, ClassRowConverter.getClassSchema(mClass));
    }

    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;
    private int mLimit;
    private RowType[] mValues;
    private SQLiteHelper mDatabase;

    public Query<RowType> projection(@NonNull String[] columns) {
        mProjection = columns;
        return this;
    }

    public Query<RowType> where(@NonNull String selection, @Nullable Object... selectionArgs) {
        mSelection = selection;
        if (selectionArgs != null) {
            mSelectionArgs = new String[selectionArgs.length];
            for (int i = 0; i < selectionArgs.length; i++)
                mSelectionArgs[i] = (String) selectionArgs[i];
        } else {
            mSelectionArgs = null;
        }
        return this;
    }

    public Query<RowType> sort(@NonNull String sortOrder) {
        mSortOrder = sortOrder;
        return this;
    }

    public Query<RowType> limit(int limit) {
        mLimit = limit;
        return this;
    }

    @SafeVarargs
    public final Query<RowType> values(@NonNull RowType... values) {
        mValues = values;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private RowType[] getInternal(int limit) {
        if (mRowClass == null) return null;
        if (mQueryType == SELECT) {
            String sort = mSortOrder;
            if (limit > -1) sort += String.format(" LIMIT %d", limit);
            Cursor cursor = mDatabase.query(mProjection, mSelection, mSelectionArgs, sort);
            if (cursor != null) {
                RowType[] results = null;
                if (cursor.getCount() > 0) {
                    results = (RowType[]) Array.newInstance(mRowClass, cursor.getCount());
                    int index = 0;
                    while (cursor.moveToNext()) {
                        results[index] = ClassRowConverter.cursorToCls(cursor, mRowClass);
                        index++;
                    }
                }
                cursor.close();
                return results;
            }
        }
        return null;
    }

    @Nullable
    public RowType get() {
        if (mRowClass == null) return null;
        RowType[] results = getInternal(1);
        if (results == null || results.length == 0)
            return null;
        return results[0];
    }

    @Nullable
    public RowType[] getAll() {
        return getInternal(mLimit > 0 ? mLimit : -1);
    }

    public void getAll(@NonNull final GetCallback<RowType> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final RowType[] results = getAll();
                if (mInquiry.mHandler == null) return;
                mInquiry.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.result(results);
                    }
                });
            }
        }).start();
    }

    public long run() {
        if (mQueryType != DELETE && (mValues == null || mValues.length == 0))
            throw new IllegalStateException("No values were provided for this query to run.");
        switch (mQueryType) {
            case INSERT:
                long inserted = 0;
                for (Object val : mValues)
                    inserted += mDatabase.insert(ClassRowConverter.clsToVals(val));
                return inserted;
            case UPDATE:
                return mDatabase.update(ClassRowConverter.clsToVals(mValues[mValues.length - 1]),
                        mSelection, mSelectionArgs);
            case DELETE:
                return mDatabase.delete(mSelection, mSelectionArgs);
        }
        return -1;
    }

    public void run(@NonNull final RunCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long changed = Query.this.run();
                if (mInquiry.mHandler == null) return;
                mInquiry.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.result(changed);
                    }
                });
            }
        }).start();
    }
}
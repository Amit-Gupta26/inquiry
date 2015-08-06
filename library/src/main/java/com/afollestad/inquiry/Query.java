package com.afollestad.inquiry;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.inquiry.callbacks.GetCallback;
import com.afollestad.inquiry.callbacks.RunCallback;
import com.afollestad.inquiry.data.Row;
import com.afollestad.inquiry.data.RowValues;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class Query<RowType extends Row> {

    protected final static int SELECT = 1;
    protected final static int INSERT = 2;
    protected final static int UPDATE = 3;
    protected final static int DELETE = 4;

    private final Inquiry mInquiry;
    private final Uri mTableUri;
    private final int mQueryType;
    @Nullable
    private final Class<RowType> mRowClass;

    protected Query(@NonNull Inquiry inquiry, @NonNull Uri tableUri, int type, @Nullable Class<RowType> mClass) {
        mInquiry = inquiry;
        mTableUri = tableUri;
        mQueryType = type;
        mRowClass = mClass;
    }

    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;
    private int mLimit;
    private RowValues[] mValues;

    public Query<RowType> projection(@NonNull String[] columns) {
        mProjection = columns;
        return this;
    }

    public Query<RowType> selection(@NonNull String selection, @Nullable Object... selectionArgs) {
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

    public Query<RowType> values(RowValues... values) {
        mValues = values;
        return this;
    }

    @SuppressWarnings("unchecked")
    private RowType[] getInternal(int limit) {
        if (mRowClass == null) return null;
        final ContentResolver cr = mInquiry.mContext.getContentResolver();
        if (mQueryType == SELECT) {
            String sort = mSortOrder;
            if (limit > -1) sort += String.format(" LIMIT %d", limit);
            Cursor cursor = cr.query(mTableUri, mProjection, mSelection, mSelectionArgs, sort);
            if (cursor != null) {
                RowType[] results = null;
                if (cursor.getCount() > 0) {
                    final Constructor ctor = Util.getDefaultConstructor(mRowClass);
                    results = (RowType[]) Array.newInstance(mRowClass, cursor.getCount());
                    int index = 0;
                    while (cursor.moveToNext()) {
                        try {
                            results[index] = (RowType) ctor.newInstance();
                            results[index].load(cursor);
                        } catch (Throwable t) {
                            throw new RuntimeException("Failed to instantiate " + mRowClass.getName() + ": " + t.getLocalizedMessage());
                        }
                        index++;
                    }
                }
                cursor.close();
                return results;
            }
        }
        return null;
    }

    public RowType get() {
        if (mRowClass == null) return null;
        RowType[] results = getInternal(1);
        if (results == null || results.length == 0)
            return null;
        return results[0];
    }

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

    public int run() {
        if (mQueryType != DELETE && (mValues == null || mValues.length == 0))
            throw new IllegalStateException("No values were provided for this query to run.");
        final ContentResolver cr = mInquiry.mContext.getContentResolver();
        switch (mQueryType) {
            case INSERT:
                if (mValues.length == 1) {
                    cr.insert(mTableUri, mValues[0].content());
                    return 1;
                } else {
                    return cr.bulkInsert(mTableUri, RowValues.contentArray(mValues));
                }
            case UPDATE:
                return cr.update(mTableUri, mValues[mValues.length - 1].content(),
                        mSelection, mSelectionArgs);
            case DELETE:
                return cr.delete(mTableUri, mSelection, mSelectionArgs);
        }
        return -1;
    }

    public void run(@NonNull final RunCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int changed = Query.this.run();
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
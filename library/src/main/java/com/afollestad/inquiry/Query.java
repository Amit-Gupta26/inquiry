package com.afollestad.inquiry;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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
    private Uri mContentUri;
    private final int mQueryType;
    @Nullable
    private final Class<RowType> mRowClass;
    @Nullable
    private SQLiteHelper mDatabase;

    protected Query(@NonNull Inquiry inquiry, @NonNull Uri contentUri, int type, @Nullable Class<RowType> mClass) {
        mInquiry = inquiry;
        mContentUri = contentUri;
        if (mContentUri.getScheme() == null || !mContentUri.getScheme().equals("content"))
            throw new IllegalStateException("You can only use content:// URIs for content providers.");
        mQueryType = type;
        mRowClass = mClass;
    }

    protected Query(@NonNull Inquiry inquiry, @NonNull String tableName, int type, @Nullable Class<RowType> mClass) {
        mInquiry = inquiry;
        mQueryType = type;
        mRowClass = mClass;
        if (inquiry.mDatabaseName == null)
            throw new IllegalStateException("Inquiry was not initialized with a database name, it can only use content providers in this configuration.");
        mDatabase = new SQLiteHelper(inquiry.mContext, inquiry.mDatabaseName,
                tableName, ClassRowConverter.getClassSchema(mClass));
    }

    private String[] mProjection;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mSortOrder;
    private int mLimit;
    private RowType[] mValues;

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
            Cursor cursor;
            if (mContentUri != null) {
                cursor = mInquiry.mContext.getContentResolver().query(mContentUri, mProjection, mSelection, mSelectionArgs, mSortOrder);
            } else {
                if (mDatabase == null) throw new IllegalStateException("Database helper was null.");
                cursor = mDatabase.query(mProjection, mSelection, mSelectionArgs, sort);
            }
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
    public RowType one() {
        if (mRowClass == null) return null;
        RowType[] results = getInternal(1);
        if (results == null || results.length == 0)
            return null;
        return results[0];
    }

    @Nullable
    public RowType[] all() {
        return getInternal(mLimit > 0 ? mLimit : -1);
    }

    public void all(@NonNull final GetCallback<RowType> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final RowType[] results = all();
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
        final ContentResolver cr = mInquiry.mContext.getContentResolver();
        switch (mQueryType) {
            case INSERT:
                if (mDatabase != null) {
                    long inserted = 0;
                    for (Object val : mValues)
                        inserted += mDatabase.insert(ClassRowConverter.clsToVals(val, null));
                    return inserted;
                } else if (mContentUri != null) {
                    if (mValues.length == 1) {
                        cr.insert(mContentUri, ClassRowConverter.clsToVals(mValues[0], null));
                        return 1;
                    } else
                        return cr.bulkInsert(mContentUri, ClassRowConverter.clsArrayToVals(mValues, null));
                } else
                    throw new IllegalStateException("Database helper was null.");
            case UPDATE: {
                final ContentValues values = ClassRowConverter.clsToVals(mValues[mValues.length - 1], mProjection);
                if (mDatabase != null)
                    return mDatabase.update(values, mSelection, mSelectionArgs);
                else if (mContentUri != null)
                    return cr.update(mContentUri, values, mSelection, mSelectionArgs);
                else
                    throw new IllegalStateException("Database helper was null.");
            }
            case DELETE: {
                if (mDatabase != null)
                    return mDatabase.delete(mSelection, mSelectionArgs);
                else if (mContentUri != null)
                    return cr.delete(mContentUri, mSelection, mSelectionArgs);
                else
                    throw new IllegalStateException("Database helper was null.");
            }
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
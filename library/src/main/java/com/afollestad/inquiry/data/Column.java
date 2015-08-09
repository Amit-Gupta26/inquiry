package com.afollestad.inquiry.data;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Column {

    @IntDef({DataType.INTEGER, DataType.BOOLEAN, DataType.TEXT, DataType.REAL, DataType.BLOB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DataTypeInt {
    }

    private final String mName;
    @DataTypeInt
    private final int mType;

    private boolean mPrimaryKey;
    private boolean mAutoIncrement;
    private boolean mNotNull;

    public Column(@NonNull String name, @DataTypeInt int type) {
        //noinspection ConstantConditions
        if (name == null)
            throw new IllegalStateException("Name cannot be null.");
        mName = name;
        mType = type;
    }

    /**
     * Adds a constraint to this column indicating that the value can never be null.
     */
    public Column notNull() {
        mNotNull = true;
        return this;
    }

    /**
     * Adds a constraint to this column, indicating it's the primary column.
     * <p/>
     * This is commonly used for a column that is an identifier for its row, like a user ID.
     */
    public Column primaryKey() {
        mPrimaryKey = true;
        return this;
    }

    /**
     * Used with INTEGER columns. Every time you insert a row, this column is incremented by one;
     * you don't manually set the value, the database does it for you.
     */
    public Column autoIncrement() {
        mAutoIncrement = true;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mName);
        sb.append(" ");
        sb.append(DataType.name(mType));
        if (mPrimaryKey)
            sb.append(" PRIMARY KEY");
        if (mAutoIncrement)
            sb.append(" AUTOINCREMENT");
        if (mNotNull)
            sb.append(" NOT NULL");
        return sb.toString();
    }

    @NonNull
    public static String arrayString(@NonNull Column[] columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            sb.append(columns[i].toString());
            if (i < columns.length - 1)
                sb.append(", ");
        }
        return sb.toString();
    }
}

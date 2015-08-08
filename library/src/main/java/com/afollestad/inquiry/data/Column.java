package com.afollestad.inquiry.data;

import android.support.annotation.NonNull;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Column {

    private final String mName;
    private final DataType mType;

    private boolean mPrimaryKey;
    private boolean mAutoIncrement;
    private boolean mNotNull;

    public Column(String name, DataType type) {
        mName = name;
        mType = type;
    }

    public Column notNull() {
        mNotNull = true;
        return this;
    }

    public Column primaryKey() {
        mPrimaryKey = true;
        return this;
    }

    public Column autoIncrement() {
        mAutoIncrement = true;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mName);
        sb.append(" ");
        sb.append(mType.getValue());
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

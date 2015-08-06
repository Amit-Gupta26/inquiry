package com.afollestad.inquiry.data;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class Row {

    protected Row() {
    }

    public abstract void load(@NonNull Cursor cursor);
}

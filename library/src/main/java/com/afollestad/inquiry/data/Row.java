package com.afollestad.inquiry.data;

import android.support.annotation.NonNull;

import com.afollestad.inquiry.RawRow;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class Row {

    protected Row() {
    }

    public abstract void load(@NonNull RawRow row);
}
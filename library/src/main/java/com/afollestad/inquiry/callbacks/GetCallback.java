package com.afollestad.inquiry.callbacks;

import android.support.annotation.Nullable;

import com.afollestad.inquiry.data.Row;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface GetCallback<RowType extends Row> {

    void result(@Nullable RowType[] result);
}

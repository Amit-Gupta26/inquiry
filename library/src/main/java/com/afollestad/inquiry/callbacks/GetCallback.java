package com.afollestad.inquiry.callbacks;

import android.support.annotation.Nullable;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface GetCallback<RowType> {

    void result(@Nullable RowType[] result);
}

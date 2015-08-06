package com.afollestad.inquiry.callbacks;

import com.afollestad.inquiry.data.Row;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface GetCallback<RowType extends Row> {

    void result(RowType[] result);
}

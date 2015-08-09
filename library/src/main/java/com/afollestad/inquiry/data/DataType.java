package com.afollestad.inquiry.data;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class DataType {
    /**
     * The value is a signed integer, stored in 1, 2, 3, 4, 6, or 8 bytes depending on the magnitude of the value.
     * <p/>
     * Translates to short, int, or long in Java (based on what was stored in the column).
     */
    public static final int INTEGER = 1;
    /**
     * Convenience method for those who don't have a lot of SQLite knowledge. Booleans are actually just
     * a 0 or 1 integer value.
     */
    public static final int BOOLEAN = 2;
    /**
     * The value is a floating point value, stored as an 8-byte IEEE floating point number.
     * <p/>
     * Translates to a float or double in Java.
     */
    public static final int REAL = 3;
    /**
     * The value is a text string, stored using the database encoding (UTF-8, UTF-16BE or UTF-16LE).
     * <p/>
     * Translates to a String in Java.
     */
    public static final int TEXT = 4;
    /**
     * The value is a blob of data, stored exactly as it was input.
     * <p/>
     * Translates to byte[] in Java.
     */
    public static final int BLOB = 5;

    public static String name(int dataInt) {
        switch (dataInt) {
            case 1:
            case 2:
                return "INTEGER";
            case 3:
                return "REAL";
            case 4:
                return "TEXT";
            case 5:
                return "BLOB";
        }
        return null;
    }
}

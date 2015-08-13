package com.afollestad.inquiry;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.afollestad.inquiry.annotations.Column;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author Aidan Follestad (afollestad)
 */
class ClassRowConverter {

    private static byte[] serializeObject(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize object of type " + obj.getClass().getName(), e);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException ignored) {
            }
            try {
                bos.close();
            } catch (IOException ignored) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializeObject(byte[] data, Class<T> cls) {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return (T) in.readObject();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize data to type " + cls.getName(), e);
        } finally {
            Utils.closeQuietely(bis);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static String getClassTypeString(Class<?> cls) {
        if (cls.equals(String.class) || cls.equals(char[].class) || cls.equals(Character[].class)) {
            return "TEXT";
        } else if (cls.equals(Float.class) || cls.equals(float.class) ||
                cls.equals(Double.class) || cls.equals(double.class)) {
            return "REAL";
        } else if (cls.equals(Integer.class) || cls.equals(int.class) ||
                cls.equals(Long.class) || cls.equals(long.class) ||
                cls.equals(Boolean.class) || cls.equals(boolean.class)) {
            return "INTEGER";
        } else {
            return "BLOB";
        }
    }

    @Nullable
    private static String getFieldSchema(Field field) {
        Column colAnnotation = field.getAnnotation(Column.class);
        if (colAnnotation == null) return null;
        String colName = field.getName();
        colName += " " + getClassTypeString(field.getType());
        if (colAnnotation.primaryKey())
            colName += " PRIMARY KEY";
        if (colAnnotation.autoIncrement())
            colName += " AUTOINCREMENT";
        if (colAnnotation.notNull())
            colName += " NOT NULL";
        return colName;
    }

    public static String getClassSchema(Class<?> cls) {
        StringBuilder sb = new StringBuilder();
        Field[] fields = cls.getDeclaredFields();
        for (Field fld : fields) {
            final String schema = getFieldSchema(fld);
            if (schema == null) continue;
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(schema);
        }
        if (sb.length() == 0)
            throw new IllegalStateException("Class " + cls.getName() + " has no column fields.");
        return sb.toString();
    }

    private static int cursorTypeToColumnType(int cursorType) {
        switch (cursorType) {
            default:
                return DataType.BLOB;
            case Cursor.FIELD_TYPE_FLOAT:
                return DataType.REAL;
            case Cursor.FIELD_TYPE_INTEGER:
                return DataType.INTEGER;
            case Cursor.FIELD_TYPE_STRING:
                return DataType.TEXT;
        }
    }

    private static void loadFieldIntoRow(Cursor cursor, Field field, Object row, int columnIndex, int columnType) throws Exception {
        if (cursor.isNull(columnIndex)) {
            field.set(row, null);
            return;
        }
        final Class<?> fieldType = field.getType();
        switch (columnType) {
            case DataType.BLOB:
                byte[] blob = cursor.getBlob(columnIndex);
                if (fieldType == byte[].class)
                    field.set(row, blob);
                else if (fieldType == Bitmap.class)
                    field.set(row, BitmapFactory.decodeByteArray(blob, 0, blob.length));
                else
                    field.set(row, deserializeObject(blob, fieldType));
                break;
            case DataType.REAL:
                if (fieldType == float.class || fieldType == Float.class)
                    field.set(row, cursor.getFloat(columnIndex));
                else if (fieldType == double.class || fieldType == Double.class)
                    field.set(row, cursor.getDouble(columnIndex));
                else
                    throw new IllegalStateException(String.format("Column %s of type REAL (float) doesn't match field of type %s",
                            field.getName(), fieldType.getName()));
                break;
            case DataType.INTEGER:
                if (fieldType == short.class || fieldType == Short.class)
                    field.set(row, cursor.getShort(columnIndex));
                else if (fieldType == int.class || fieldType == Integer.class)
                    field.set(row, cursor.getInt(columnIndex));
                else if (fieldType == long.class || fieldType == Long.class)
                    field.set(row, cursor.getLong(columnIndex));
                else if (fieldType == boolean.class || fieldType == Boolean.class)
                    field.set(row, cursor.getInt(columnIndex) == 1);
                else
                    throw new IllegalStateException(String.format("Column %s of type INTEGER (float) doesn't match field of type %s",
                            field.getName(), fieldType.getName()));
                break;
            case DataType.TEXT:
                String text = cursor.getString(columnIndex);
                if (fieldType == String.class || fieldType == CharSequence.class)
                    field.set(row, text);
                else if (fieldType == char[].class || fieldType == Character[].class)
                    field.set(row, text.length() > 0 ? text.toCharArray() : null);
                else if (fieldType == char.class || fieldType == Character.class)
                    field.set(row, text.length() > 0 ? text.charAt(0) : null);
                else
                    throw new IllegalStateException(String.format("Column %s of type REAL (float) doesn't match field of type %s",
                            field.getName(), fieldType.getName()));
                break;
        }
    }

    public static <T> T cursorToCls(Cursor cursor, Class<T> cls) {
        T row = Utils.newInstance(cls);
        for (int columnIndex = 0; columnIndex < cursor.getColumnCount(); columnIndex++) {
            final String columnName = cursor.getColumnName(columnIndex);
            final int columnType = cursorTypeToColumnType(cursor.getType(columnIndex));
            try {
                final Field columnField = cls.getDeclaredField(columnName);
                loadFieldIntoRow(cursor, columnField, row, columnIndex, columnType);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(String.format("No field found in %s for column %s (of type %s)",
                        cls.getName(), columnName, DataType.name(columnType)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return row;
    }

    public static ContentValues clsToVals(Object row, @Nullable String[] projection) {
        try {
            ContentValues vals = new ContentValues();
            Field[] fields = row.getClass().getDeclaredFields();
            int columnCount = 0;
            for (Field fld : fields) {
                if (projection != null && projection.length > 0) {
                    boolean skip = true;
                    for (String proj : projection) {
                        if (proj != null && proj.equalsIgnoreCase(fld.getName())) {
                            skip = false;
                            break;
                        }
                    }
                    if (skip) continue;
                }
                Column colAnn = fld.getAnnotation(Column.class);
                if (colAnn == null) continue;
                columnCount++;
                if (colAnn.autoIncrement()) continue;
                final Class<?> fldType = fld.getType();
                final Object fldVal = fld.get(row);
                if (fldVal == null) continue;

                if (fldType.equals(String.class)) {
                    vals.put(fld.getName(), (String) fldVal);
                } else if (fldType.equals(char[].class) || fldType.equals(Character[].class)) {
                    vals.put(fld.getName(), new String((char[]) fldVal));
                } else if (fldType.equals(Float.class) || fldType.equals(float.class)) {
                    vals.put(fld.getName(), (float) fldVal);
                } else if (fldType.equals(Double.class) || fldType.equals(double.class)) {
                    vals.put(fld.getName(), (double) fldVal);
                } else if (fldType.equals(Short.class) || fldType.equals(short.class)) {
                    vals.put(fld.getName(), (short) fldVal);
                } else if (fldType.equals(Integer.class) || fldType.equals(int.class)) {
                    vals.put(fld.getName(), (int) fldVal);
                } else if (fldType.equals(Long.class) || fldType.equals(long.class)) {
                    vals.put(fld.getName(), (long) fldVal);
                } else if (fldType.equals(char.class) || fldType.equals(Character.class)) {
                    vals.put(fld.getName(), Character.toString((char) fldVal));
                } else if (fldType.equals(Boolean.class) || fldType.equals(boolean.class)) {
                    vals.put(fld.getName(), ((boolean) fldVal) ? 1 : 0);
                } else if (fldType.equals(Bitmap.class)) {
                    vals.put(fld.getName(), bitmapToBytes((Bitmap) fldVal));
                } else if (fldType.equals(Byte[].class) || fldType.equals(byte[].class)) {
                    vals.put(fld.getName(), (byte[]) fldVal);
                } else if (fldVal instanceof Serializable) {
                    vals.put(fld.getName(), serializeObject(fldVal));
                } else {
                    throw new IllegalStateException("Class " + fldType.getName() + " should be marked as Serializable in order to be inserted.");
                }
            }
            if (columnCount == 0)
                throw new IllegalStateException("Class " + row.getClass().getName() + " has no column fields.");
            return vals;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } finally {
            Utils.closeQuietely(stream);
        }
    }
}

package com.afollestad.inquiry.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Aidan Follestad (afollestad)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface Column {

    boolean primaryKey() default false;

    boolean autoIncrement() default false;

    boolean notNull() default false;
}
package com.afollestad.sample;

import com.afollestad.inquiry.annotations.Column;

import java.io.Serializable;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Person implements Serializable {

    public Person() {
        // Default constructor is needed so Inquiry can auto construct instances
    }

    public Person(String name, int age, float rank, boolean admin, Person parent) {
        this.name = name;
        this.age = age;
        this.rank = rank;
        this.admin = admin;
        this.parent = parent;
    }

    @Column(primaryKey = true, notNull = true, autoIncrement = true)
    public long _id;
    @Column
    public String name;
    @Column
    public int age;
    @Column
    public float rank;
    @Column
    public boolean admin;

    @Column
    public Person parent;
}

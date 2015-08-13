# Inquiry

Inquiry is a simple library for Android that simplifies construction and use of SQL content providers.

#### What's SQL?

*Structured Query Language* is a simple to understand command language used to manipulate databases.
There are many variations, owned by different companies. MySQL and PostgreSQL being examples.

#### What's SQLite?

*SQLite* is a "dumbed down" version of SQL. SQLite is accessed directly through a file, rather than over
a network connection (which is actually safer and faster). Unnecessary commands, functions, and data types
were removed from SQLite.

#### What's a Content Provider?

A *content provider* is an Android API used to store data that can be used across processes and apps.
Content providers are accessed via `content://` URIs. This library takes advantage of SQLite functionality
that can be accessed through content providers, to quickly and efficiently store mass amounts of structured
information.

---

# Gradle Dependency

First, add JitPack.io to the repositories list in your app module's build.gradle file:

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

Then, add Inquiry to your dependencies list:

```gradle
dependencies {
    compile 'com.afollestad:inquiry:1.0.0'
}
```

[ ![JitPack Badge](https://img.shields.io/github/release/afollestad/inquiry.svg?label=inquiry) ](https://jitpack.io/#afollestad/inquiry)

---

# Table of Contents

1. [Quick Setup](https://github.com/afollestad/inquiry#quick-setup)
2. [Example Row](https://github.com/afollestad/inquiry#example-row)
3. [Querying Rows](https://github.com/afollestad/inquiry#querying-rows)
    1. [Basics](https://github.com/afollestad/inquiry#basics)
    2. [Where and Projection](https://github.com/afollestad/inquiry#where-and-projection)
    3. [Sorting and Limiting](https://github.com/afollestad/inquiry#sorting-and-limiting)
4. [Inserting Rows](https://github.com/afollestad/inquiry#inserting-rows)
5. [Updating Rows](https://github.com/afollestad/inquiry#updating-rows)
6. [Deleting Rows](https://github.com/afollestad/inquiry#deleting-rows)
7. [Dropping Tables](https://github.com/afollestad/inquiry#dropping-tables)

---

# Quick Setup

When your app starts, you need to initialize Inquiry. You can do so from an `Application` class,
which must be registered in your manifest. You could also put this inside of `onCreate()` in your
main `Activity`.

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Inquiry.init(this, "myDatabase);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Inquiry.deinit();
    }
}
```

`init()` takes a `Context` in the first parameter, and the name of the database that'll you be using
in the second parameter. Think of a database like a file that contains a set of tables (a table is basically
a spreadsheet; it contains rows and columns).

When your app is done with Inquiry, you *should* call `deinit()` to help clean up references.

---

# Example Row

In Inquiry, a row is just an object which contains a set of values that can be read from and written to
a table in your database.

```java
public class Person {

    public Person() {
        // Default constructor is needed so Inquiry can auto construct instances
    }

    public Person(String name, int age, float rank, boolean admin) {
        this.name = name;
        this.age = age;
        this.rank = rank;
        this.admin = admin;
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
}
```

Notice that all the fields are annotated with the `@Column` annotation. If you have fields without that
annotation, they will be ignored by Inquiry.

Notice that the `_id` field contains optional parameters in its annotation:

* `primaryKey` indicates its column is the main column used to identity the row. No other row in the
table can have the same value for that column. This is commonly used with IDs.
* `notNull` indicates that you can never insert null as a value for that column.
* `autoIncrement` indicates that you don't need to manually set the value of this column. Every time
you insert a row into the table, this column will be incremented by one automatically.

---

# Querying Rows

#### Basics

Querying retrieves rows, whether its every row in a table or rows that match a specific criteria.
Here's how you would retrieve all rows from a table called *"people"*:

```java
Person[] result = Inquiry.get()
    .selectFrom("people", Person.class)
    .getAll();
```

If you only needed one row, using `get()` instead of `getAll()` is more efficient:

```java
Person result = Inquiry.get()
    .selectFrom("people", Person.class)
    .get();
```

---

You can also perform the query on a separate thread using a callback:

```java
Inquiry.get()
    .selectFrom("people", Person.class)
    .getAll(new GetCallback<Person>() {
        @Override
        public void result(Person[] result) {
            // Do something with result
        }
    });
```

Inquiry will automatically fill in your `@Column` fields with matching columns in each row of the table.

#### Where and Projection

If you wanted to find rows with specific values in their columns, you can use `where` selection:

```java
Person[] result = Inquiry.get()
    .selectFrom("people", Person.class)
    .where("name = ? AND age = ?", "Aidan", 20)
    .getAll();
```

The first parameter is a string, specifying two conditions that must be true (`AND` is used instead of `OR`).
The question marks are placeholders, which are replaced by the values you specify in the second comma-separated
vararg (or array) parameter.

---

If you wanted, you could skip using the question marks and only use one parameter:

```java
.where("name = 'Aidan' AND age = 20");
```

*However*, using the question marks and filler parameters can be easier to read if you're filling them in
with variables. Plus, this will automatically escape any strings that contain reserved SQL characters.

If you only wanted certain columns in the results to be filled in, you could use projection:

```java
Person[] result = Inquiry.get()
    .selectFrom("people", Person.class)
    .projection(new String[] { "_id", "age" })
    .getAll();
```

Fields not included in projection will be set to their default values (e.g. null for objects,
0 for numbers, false for booleans, etc.).

#### Sorting and Limiting

This code would limit the maximum number of rows returned to 100. It would sort the results by values
in the "name" column, in descending (Z-A, or greater to smaller) order:

```java
Person[] result = Inquiry.get()
    .selectFrom("people", Person.class)
    .limit(100)
    .sort("name DESC")
    .getAll();
```

If you understand SQL, you'll know you can specify multiple sort parameters separated by commas.

```java
.sort("name DESC, age ASC");
```

The above sort value would sort every column by name descending (large to small, Z-A) first, *and then* by age ascending (small to large).

# Inserting Rows

Insertion is pretty straight forward. This inserts three `People` into the table *"people"*:

```java
Person one = new Person("Waverly", 18, 8.9f, false);
Person two = new Person("Natalie", 42, 10f, false);
Person three = new Person("Aidan", 20, 5.7f, true);

long insertedCount = Inquiry.get()
        .insertInto("people", Person.class)
        .values(one, two, three)
        .run();
```

Inquiry will automatically pull your `@Column` fields out and insert them into the table `people`.

Like `getAll()`, `run()` has a callback variation that will run the operation in a separate thread:

```java
Inquiry.get()
    .insertInto("people", Person.class)
    .values(one, two, three)
    .run(new RunCallback() {
        @Override
        public void result(long changedCount) {
            // Do something
        }
    });
```

# Updating Rows

Updating is similar to insertion, however it results in changed rows rather than new rows:

```java
Person two = new Person("Natalie", 42, 10f, false);

long updatedCount = Inquiry.get()
    .update("people", Person.class)
    .values(two)
    .where("name = ?", "Aidan")
    .run();
```

The above will update all rows whose name is equal to *"Aidan"*, setting all columns to the values in the `Person`
object called `two`. If you didn't specify `where()` args, every row in the table would be updated.

# Deleting Rows

Deletion is simple:

```java
int deletedCount = Inquiry.get()
    .deleteFrom("people")
    .where("age = ?", 20)
    .run();
```

The above code results in any rows with their age column set to *20* removed. If you didn't
specify `where()` args, every row in the table would be deleted.

# Dropping Tables

Dropping a table means deleting it. It's pretty straight forward:

```java
Inquiry.get()
    .dropTable("people");
```

Just pass table name, and it's gone.
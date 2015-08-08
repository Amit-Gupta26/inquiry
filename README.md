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
    compile 'com.afollestad:inquiry:0.1.3'
}
```

[ ![JitPack Badge](https://img.shields.io/github/release/afollestad/inquiry.svg?label=inquiry) ](https://jitpack.io/#afollestad/inquiry)

---

# Table of Contents

1. [Pre-setup](https://github.com/afollestad/inquiry#pre-setup)
    1. [Table Schema](https://github.com/afollestad/inquiry#table-schema)
    2. [Row Schema](https://github.com/afollestad/inquiry#row-schema)
    3. [Registering Tables in the Manifest](https://github.com/afollestad/inquiry#registering-tables-in-the-manifest)
3. [Initialization/Deinitialization](https://github.com/afollestad/inquiry#initialization-deinitialization)
4. [Querying Rows](https://github.com/afollestad/inquiry#querying-rows)
    1. [Basics](https://github.com/afollestad/inquiry#basics)
    2. [Where and Projection](https://github.com/afollestad/inquiry#where-and-projection)
    3. [Sorting and Limiting](https://github.com/afollestad/inquiry#sorting-and-limiting)
5. [Inserting Rows](https://github.com/afollestad/inquiry#inserting-rows)
6. [Updating Rows](https://github.com/afollestad/inquiry#updating-rows)
7. [Deleting Rows](https://github.com/afollestad/inquiry#deleting-rows)
8. [Dropping Tables](https://github.com/afollestad/inquiry#dropping-tables)

---

# Pre-setup

Before you can initialize Inquiry, you'll need something to initialize with. Inquiry needs to know
what your database tables look like, and what they hold.

#### Table Schema

A table is like a spreadsheet. It contains rows, each row has columns that separate values with their own data types.

Here's an example table:

```java
public class TestTable extends Table {

    @NonNull
    @Override
    public String databaseName() {
        return "test_database";
    }

    @NonNull
    @Override
    public String tableName() {
        return "test_table";
    }

    @NonNull
    @Override
    public String authority() {
        // This will match a value in your manifest, discussed below
        return "com.myapp.testtable";
    }

    @NonNull
    @Override
    public Column[] columns() {
        return new Column[]{
                new Column("_id", DataType.INTEGER)
                        .autoIncrement()
                        .primaryKey()
                        .notNull(),
                new Column("name", DataType.TEXT),
                new Column("age", DataType.INTEGER),
                new Column("rank", DataType.REAL)
        };
    }
}
```

`databaseName()` is used for the database file that tables are contained in; you can have multiple
tables inside of a database. The `tableName()` is the name of the table that you will use when querying,
inserting, updating, and deleting. The `authority()` is used when you register your table as a content provider
in your app's manifest. `columns()` provides a schema for the columns that are in this table.

#### Row Schema

If you understand SQL or a spreadsheet, you should know what a row is.

Row implementations are simple, you just need to override `load(RawRow)` which is invoked by the library
when it loads the result of a query into an array of `Row` objects.

```java
public class TestRow extends Row {

    public long id = -1;
    public String name;
    public float rank;

    @Override
    public void load(@NonNull RawRow row) {
        id = row.getLong("id");
        name = row.getString("name");
        rank = row.getFloat("rank");
    }
}
```

#### Registering Tables in the Manifest

A `Table` is indirectly a Content Provider. You must register all of your table classes in your app's
`AndroidManifest.xml` file:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.myapp.putpackagehere">

    <application
        android:name=".App"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme">

        ...

        <!-- Set exported to true to allow access from other apps -->
        <provider
            android:name=".TestTable"
            android:authorities="com.myapp.testtable"
            android:exported="false" />

    </application>

</manifest>
```

The name attribute points to your Java class. The authority should match what you return for `authority()`
in your table implementation.

---

# Initialization/Deinitialization

When your app starts, you need to initialize Inquiry. You can do so from an `Application` class,
which must be registered in your manifest. You could also put this inside of `onCreate()` in your
main `Activity`.

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Inquiry.init(this, TestTable.class);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Inquiry.deinit();
    }
}
```

`init()` takes a `Context` in the first parameter. After that, you list all table classes that your app will use,
separated by commas. You could have multiple tables registered like this:

```java
Inquiry.init(this, TableOne.class, TableTwo.class, TableThree.class);
```

When your app is done with Inquiry, you *should* call `deinit()` to help clean up references.

# Querying Rows

#### Basics

Querying retrieves rows, whether its every row in a table or rows that match a specific criteria.
Here's how you would retrieve all rows from a table called *"test_table"*:

```java
TestRow[] result = Inquiry.get()
    .selectFrom("test_table", TestRow.class)
    .getAll();
```

If you only needed one row, using `get()` instead of `getAll()` is more efficient:

```java
TestRow result = Inquiry.get()
    .selectFrom("test_table", TestRow.class)
    .get();
```

You can also perform the query on a separate thread using a callback:

```java
Inquiry.get()
    .selectFrom("test_table", TestRow.class)
    .getAll(new GetCallback<TestRow>() {
        @Override
        public void result(TestRow[] result) {
            // Do something with result
        }
    });
```

#### Where and Projection

If you wanted to find rows with specific values in their columns, you can use `where` selection:

```java
TestRow[] result = Inquiry.get()
    .selectFrom("test_table", TestRow.class)
    .where("name = ? AND age = ?", "Aidan Follestad", 20)
    .getAll();
```

The first parameter is a string, specifying two conditions that must be true (`AND` is used instead of `OR`).
The question marks are placeholders, which are replaced by the values you specify in the second comma-separated
vararg (or array) parameter.

If you wanted, you could skip using the question marks and only use one parameter:

```java
.where("name = 'Aidan' AND age = 20");
```

*However*, using the question marks and filler parameters can be easier to read if you're filling them in
with variables. Plus, this will automatically escape any strings that contain reserved SQL characters.

If you only wanted certain columns in the results to be filled in, you could use projection:

```java
TestRow[] result = Inquiry.get()
    .selectFrom("test_table", TestRow.class)
    .projection(new String[] { "_id", "age" })
    .getAll();
```

Make sure your `Row` class is prepared to handle non-existing columns when you use projection.

#### Sorting and Limiting

This code would limit the maximum number of rows returned to 100. It would sort the results by values
in the "name" column, in descending (Z-A, or greater to smaller) order:

```java
TestRow[] result = Inquiry.get()
    .selectFrom("test_table", TestRow.class)
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

Insertion is pretty straight forward. This inserts two rows into the table *"test_table"*:

```java
RowValues values = new RowValues()
    .put("name", "Aidan")
    .put("age", 20)
    .put("rank", 2.5f);
RowValues values2 = new RowValues()
    .put("name", "Waverly")
    .put("age", 18)
    .put("rank", 8.3f);

int insertedCount = Inquiry.get()
    .insertInto("test_table")
    .values(values, values2)
    .run();
```

The `RowValues` objects contains key-value pairs, the key being the column name from the table you're inserting into.

*Don't forget to call `run()` at the end!*

**Note**: like `getAll()`, `run()` has a callback variation that will run the operation in a separate thread:

```java
Inquiry.get()
    .insertInto("test_table")
    .values(values, values2)
    .run(new RunCallback() {
        @Override
        public void result(int changedCount) {
            // Do something
        }
    });
```

# Updating Rows

Updating is similar to selection, however it results in changes rather than retrieving rows:

```java
RowValues values = new RowValues()
    .put("name", "New Name");

int updatedCount = Inquiry.get()
    .update("test_table")
    .where("name = ?", "Aidan")
    .values(values)
    .run();
```

The above code updates the name column to *"New Name"* in any column which currently has their name set
to *"Aidan"*. If you didn't specify `where()` args, every row in the table would be updated.

*Don't forget to call `run()` at the end!*

# Deleting Rows

Deletion, like updating, is simple:

```java
int deletedCount = Inquiry.get()
    .deleteFrom("test_table")
    .where("age = ?", 20)
    .run();
```

The above code results in any rows with their age column set to *20* to be deleted. If you didn't
specify `where()` args, every row in the table would be deleted.

*Don't forget to call `run()` at the end!*

# Dropping Tables

Dropping a table means deleting it. It's pretty straight forward:

```java
Inquiry.get()
    .dropTable("test_database", "test_table");
```

Just pass the database and table names, and it's gone.
package com.afollestad.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.inquiry.Inquiry;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Inquiry.init(this, "sampleDatabase");

        Person one = new Person("Waverly", 18, 8.9f, false, null);
        Person two = new Person("Natalie", 42, 10f, false, null);
        Person three = new Person("Aidan", 20, 5.7f, true, two);

        Inquiry.get()
                .insertInto("people", Person.class)
                .values(one, two, three)
                .run();


        long deleted = Inquiry.get()
                .deleteFrom("people", Person.class)
                .run();

        Person[] people = Inquiry.get()
                .selectFrom("people", Person.class)
                .getAll();

        Log.v("result", "result");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

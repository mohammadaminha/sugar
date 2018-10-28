package com.example.activities;

import android.app.Activity;
import android.os.Bundle;

import com.example.DbClasses.book;
import com.example.R;

import mohammadaminha.com.sugar.SugarRecord;


public class SugarActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        book book = new book("test", R.drawable.icon);
        SugarRecord.save(book);

    }
}

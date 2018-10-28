package com.example;

import android.app.Application;

import mohammadaminha.com.sugar.SugarContext;
import mohammadaminha.com.sugar.SugarDb;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(getApplicationContext());
        new SugarDb(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}

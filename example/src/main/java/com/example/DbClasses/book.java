package com.example.DbClasses;

import mohammadaminha.com.sugar.SugarRecord;

public class book extends SugarRecord {
    private String title;
    private int icon;

    public book(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }
}

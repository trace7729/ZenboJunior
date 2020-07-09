package com.example.mynewsagg;

import android.app.Application;

public class mynewsaggApp extends Application {
    private static mynewsaggApp mynewsaggAppInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mynewsaggAppInstance = this;
    }
    public static mynewsaggApp getMynewsaggAppInstance(){
        return mynewsaggAppInstance;
    }
}


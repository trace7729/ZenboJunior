package com.example.mynewsagg.util;

import android.content.Context;
import android.net.ConnectivityManager;

import com.example.mynewsagg.mynewsaggApp;


public class UtilityMethods {
    /**
     * Method to detect network connection on the device
     */
    public static boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) mynewsaggApp.getMynewsaggAppInstance()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}

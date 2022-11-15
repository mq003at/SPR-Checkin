package com.spr.selfcheck;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

public class InternetHandler {
    private final Context context;

    public InternetHandler(Context context) {
        this.context = context;
    }

    public boolean checkForInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiAvailable = false;
        boolean isMobileAvailable = false;
        for (Network network : connectivityManager.getAllNetworks()) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiAvailable |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileAvailable |= networkInfo.isConnected();
            }
        }
        return (isMobileAvailable || isWifiAvailable);
    }
}

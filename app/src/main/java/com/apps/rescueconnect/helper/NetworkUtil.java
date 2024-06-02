package com.apps.rescueconnect.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil extends BroadcastReceiver {

    // Constant Variable Declaration
    // Start Region
    public static final String NO_INTERNET_ERROR = "No Internet connection,Please try again.";
    private static final String TAG = NetworkUtil.class.getSimpleName();
    // End Region
    /**
     * Checked Internet Connectivity status
     * and created object of NetworkInfo ie activeNetwork to check connectivity
     *
     * @param context application interface of context
     * @return boolean isConnectivity available returns true else false
     */

    public static boolean getConnectivityStatus(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                haveConnectedWifi = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                haveConnectedMobile = true;
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    /**
     * This method is used to Receive Connection
     * it get details from server based on data sync flag
     * and finally execute EventBus to transport data
     *
     * @param context  application interface of context
     * @param intent  intent object
     * @return void
     */
    @Override
    public void onReceive(Context context, Intent intent) {
    }
}
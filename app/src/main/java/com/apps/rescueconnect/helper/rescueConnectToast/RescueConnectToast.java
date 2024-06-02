package com.apps.rescueconnect.helper.rescueConnectToast;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.tfb.fbtoast.FBToast;

public class RescueConnectToast {

    public static final int RESCUE_CONNECT_TOAST_LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int RESCUE_CONNECT_TOAST_LENGTH_LONG = Toast.LENGTH_LONG;

    public RescueConnectToast() {
    }

    public static void showSuccessToast(Context context, String message, int toastLength) {
        FBToast.successToast(context, message, toastLength, Gravity.CENTER_HORIZONTAL);
    }

    public static void showErrorToast(Context context, String message, int toastLength) {
        FBToast.errorToast(context, message, toastLength, Gravity.CENTER_HORIZONTAL);
    }

    public static void showAlertToast(Context context, String message, int toastLength) {
        FBToast.warningToast(context, message, toastLength, Gravity.CENTER_HORIZONTAL);
    }

    public static void showNormalToast(Context context, String message, int toastLength) {
        FBToast.nativeToast(context, message, toastLength, Gravity.CENTER_HORIZONTAL);
    }

    public static void showInfoToast(Context context, String message, int toastLength) {
        FBToast.infoToast(context, message, toastLength, Gravity.CENTER_HORIZONTAL);
    }

    public static void showErrorToastWithBottom(Context context, String message, int toastLength) {
        FBToast.errorToast(context, message, toastLength, Gravity.BOTTOM);
    }
    public static void showSuccessToastWithBottom(Context context, String message, int toastLength) {
        FBToast.successToast(context, message, toastLength, Gravity.BOTTOM);
    }

}

package com.apps.rescueconnect.helper;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

import com.apps.rescueconnect.BuildConfig;
import com.apps.rescueconnect.model.User;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Utils {
    private static final String TAG = "Utils";

    public static boolean isEmptyField(String value) {
        return value.trim().isEmpty();
    }

    public static String getFieldValue(EditText editTextField) {
        return editTextField.getText().toString().trim();
    }

    public static void saveLoginUserDetails(Context context, User user) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.APP_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String loginUserDetails = gson.toJson(user);
        sharedPreferences.edit().putString(AppConstants.LOGIN_USER_DETAILS, loginUserDetails).apply();
    }

    public static User getLoginUserDetails(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.APP_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String userDetailsString = sharedPreferences.getString(AppConstants.LOGIN_USER_DETAILS, "");
        return gson.fromJson(userDetailsString, User.class);
    }

    public static void removeLoginUserDetails(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.APP_PREFS, MODE_PRIVATE);
        sharedPreferences.edit().remove(AppConstants.LOGIN_USER_DETAILS).apply();
    }

    public static String getSharedPrefsString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.APP_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public static void saveSharedPrefsString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.APP_PREFS, MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static void removeSharedPrefsString(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.APP_PREFS, MODE_PRIVATE);
        sharedPreferences.edit().remove(key).apply();
    }

    public static User getUserDetails(Context context, String mobileNumber) {
        Log.d(TAG, "getUserDetails: userEmail");
        User userMaster = null;
        try {
            ArrayList<User> userList;
            // load tasks from preference
            SharedPreferences prefs = context.getSharedPreferences(AppConstants.APP_PREFS, MODE_PRIVATE);
            Gson gson = new Gson();
            String readString = prefs.getString(AppConstants.USER_LIST, "");
            if (!TextUtils.isEmpty(readString)) {
                Type type = new TypeToken<ArrayList<User>>() {
                }.getType();
                userList = gson.fromJson(readString, type);
                int count = 0;
                assert userList != null;
                for (User userSub : userList) {
                    if (userSub.getMobileNumber().equals(mobileNumber)) {
                        Log.d(TAG, "getUserDetails: userDetails: " + userSub);
                        return userSub;
                    }
                }
            } else {
                return userMaster;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return userMaster;
        }
        return userMaster;
    }

    public static void removeAllDataWhenLogout(Context context) {
        removeLoginUserDetails(context);
        removeSharedPrefsString(context, AppConstants.LOGIN_TOKEN);
        removeSharedPrefsString(context, AppConstants.USER_ROLE);
        removeSharedPrefsString(context, AppConstants.LOGIN_USER_DETAILS);
    }

    public static String getCurrentTimeStampWithSeconds() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAdminSettingsOption(String role) {
        List<String> settingsOptionList = new ArrayList<>();
        if (role.equalsIgnoreCase(AppConstants.ROLE_ADMIN)) {
            settingsOptionList.add(AppConstants.SETTINGS_MY_PROFILE);
        } else {
            settingsOptionList.add(AppConstants.SETTINGS_MY_PROFILE);
            settingsOptionList.add(AppConstants.SETTINGS_UPDATE_MPIN);
        }
        return settingsOptionList;
    }

    public static String applyMaskAndShowLastDigits(String originalText, int digits) {
        String dynamicRegex = "\\w(?=\\w{" + digits + "})";
        return originalText.replaceAll(dynamicRegex, "x");
    }

    public static String getCurrentTimeStampWithSecondsAsId() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    public static BitmapDescriptor getBitmapDescriptor(Context ctx, int id) {
        Drawable vectorDrawable = ContextCompat.getDrawable(ctx, id);
        int h = Objects.requireNonNull(vectorDrawable).getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        MapsInitializer.initialize(ctx);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    public static int getDisplayDPI(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    public static boolean checkLocationPermission(Context context) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Checking location permission");
        int r1 = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int r2 = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (BuildConfig.DEBUG)
            Log.d(TAG, ": " + (r1 == PackageManager.PERMISSION_GRANTED && r2 == PackageManager.PERMISSION_GRANTED));
        return r1 == PackageManager.PERMISSION_GRANTED && r2 == PackageManager.PERMISSION_GRANTED;
    }
}

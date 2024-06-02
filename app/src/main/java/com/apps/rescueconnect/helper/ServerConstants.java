package com.apps.rescueconnect.helper;

import android.util.Base64;

public class ServerConstants {

    public static final String COUNTRY_SELECTED = "IN";

    // getEncodedApiKey result encoded key,
    // splitted into four the part
    static String thePartOne = "UVVsNllWTjVRVVpGZG";
    static String thePartTwo = "tSVU5UVXpTMlV3YWta";
    static String thePartThree = "MFVYVTNaMWR2YUdGTk";
    static String thePartFour = "1uVlhVVE0xWTFOTgo=";

    public static String getDecodedGMPApiKey() {
        return new String(
                Base64.decode(
                        Base64.decode(
                                thePartOne +
                                        thePartTwo +
                                        thePartThree +
                                        thePartFour,
                                Base64.DEFAULT),
                        Base64.DEFAULT));
    }
}

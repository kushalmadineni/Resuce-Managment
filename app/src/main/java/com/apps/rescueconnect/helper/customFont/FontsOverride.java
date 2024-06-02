package com.apps.rescueconnect.helper.customFont;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

public class FontsOverride {

    /**
     * This static method gets fontAssetName and based on that it sets Default Font Style
     *
     * @param context  context
     * @param staticTypefaceFieldName name of typeface file
     * @param fontAssetName       name of font asset
     * @return void
     */
    public static void setDefaultFont(Context context, String staticTypefaceFieldName, String fontAssetName){
        final Typeface regular = Typeface.createFromAsset(context.getAssets(), fontAssetName);
        replaceFont(staticTypefaceFieldName,regular);
    }

    /**
     * This static method gets newTypeface and based on that it replace Font Style
     *
     * @param staticTypefaceFieldName name of typeface file
     * @param newTypeface       typeface type
     * @return void
     */
    protected static void replaceFont(String staticTypefaceFieldName, final Typeface newTypeface){
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

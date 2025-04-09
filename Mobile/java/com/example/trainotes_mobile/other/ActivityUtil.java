package com.example.trainotes_mobile.other;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.icu.text.UnicodeSetIterator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.example.trainotes_mobile.MainActivity;
import com.example.trainotes_mobile.R;

import java.util.Locale;

public class ActivityUtil {
    public static void restartActivities(Context context) {
        if (context instanceof Activity) {
            String selectedDate = MainActivity.selectedDate.toString();
            ((Activity) context).finish();
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("selectedDate", selectedDate);
            context.startActivity(intent);
        }
    }

    public static void restartActivity(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).finish();
            Intent intent = new Intent(context, context.getClass());
            context.startActivity(intent);
        }
    }

    public static void setAppLocale(Context context){
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean isDefaultLanguage = preferences.getBoolean("isDefaultLanguage", true);
        String localeCode = (isDefaultLanguage ? "en" : "ua");

        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(localeCode));
        resources.updateConfiguration(configuration, displayMetrics);
    }

    public static String metricToImperial(String kilogramsString) {
        float kilograms = Float.parseFloat(kilogramsString);
        float pounds = kilograms * 2.20462f;
        pounds = Math.round(pounds * 10.0f) / 10.0f;
        String poundsString = Float.toString(pounds);
        return poundsString;
    }

    public static String imperialToMetric(String poundsString) {
        float pounds = Float.parseFloat(poundsString);
        float kilograms = pounds / 2.20462f;
        kilograms = Math.round(kilograms * 10.0f) / 10.0f;
        String kilogramsString = Float.toString(kilograms);
        return kilogramsString;
    }
}

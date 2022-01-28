package com.yesserly.hideface.utils;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String TAG = "SharedPreferencesHelper";
    private static final String PERSONALIZED_ADS = "AD";

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    /***********************************************************************************************
     * *********************************** Constructor
     */
    @SuppressLint("CommitPrefEdits")
    public SharedPreferencesHelper(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        editor = sharedPreferences.edit();
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    //Ads
    public void setAdPersonalized(boolean isPersonalized) {
        editor.putBoolean(PERSONALIZED_ADS, isPersonalized).apply();
    }

    public boolean isAdPersonalized() {
        return sharedPreferences.getBoolean(PERSONALIZED_ADS, true);
    }
}

package com.yesserly.hideface.base;

import android.content.Context;

import com.yesserly.hideface.utils.GDPR;
import com.yesserly.hideface.utils.SharedPreferencesHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class Modules {

    @Singleton
    @Provides
    public static SharedPreferencesHelper providesSharedPreferences(@ApplicationContext Context context) {
        return new SharedPreferencesHelper(context.getSharedPreferences("BASIC", Context.MODE_PRIVATE));
    }

    @Provides
    @Singleton
    public static GDPR providesGDPR(@ApplicationContext Context context, SharedPreferencesHelper sharedPreferencesHelper){
        return new GDPR(context, sharedPreferencesHelper);
    }
}

package com.yesserly.hideface.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.yesserly.hideface.R;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class GDPR {
    private static final String TAG = "GDPR";

    private ConsentForm form;
    private final SharedPreferencesHelper mSharedPrefs;

    @Inject
    public GDPR(SharedPreferencesHelper mSharedPrefs) {
        this.mSharedPrefs = mSharedPrefs;
    }

    /**
     * GDPR CODE
     */
    public void checkForConsent(Context mContext) {
        Log.d(TAG, "checkForConsent: Checking For Consent");
        ConsentInformation consentInformation = ConsentInformation.getInstance(mContext);
        String[] publisherIds = {mContext.getResources().getString(R.string.PUBLISHER_ID)};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d(TAG, "onConsentInfoUpdated: Successful");
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        Log.d(TAG, "Showing Personalized ads");
                        mSharedPrefs.setAdPersonalized(true);
                        break;
                    case NON_PERSONALIZED:
                        Log.d(TAG, "Showing Non-Personalized ads");
                        mSharedPrefs.setAdPersonalized(false);
                        break;
                    case UNKNOWN:
                        Log.d(TAG, "Requesting Consent");
                        if (ConsentInformation.getInstance(mContext)
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent(mContext);
                        } else {
                            mSharedPrefs.setAdPersonalized(true);
                        }
                        break;
                    default:
                        Log.d(TAG, "onConsentInfoUpdated: Nothing");
                        break;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                Log.d(TAG, "onFailedToUpdateConsentInfo: Failed to update");
                // User's consent status failed to update.
            }
        });
    }

    private void requestConsent(Context mContext) {
        Log.d(TAG, "requestConsent: Requesting Consent");
        URL privacyUrl = null;
        try {
            privacyUrl = new URL(mContext.getResources().getString(R.string.PRIVACY_POLICY_URL));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error_image.
        }
        form = new ConsentForm.Builder(mContext, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        Log.d(TAG, "Requesting Consent: onConsentFormLoaded");
                        showForm();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                        Log.d(TAG, "Requesting Consent: onConsentFormOpened");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d(TAG, "Requesting Consent: onConsentFormClosed");
                        if (userPrefersAdFree) {
                            // Buy or Subscribe
                            Log.d(TAG, "Requesting Consent: User prefers AdFree");
                        } else {
                            Log.d(TAG, "Requesting Consent: Requesting consent again");
                            switch (consentStatus) {
                                case PERSONALIZED:
                                    mSharedPrefs.setAdPersonalized(true);
                                    break;
                                case NON_PERSONALIZED:
                                case UNKNOWN:
                                    mSharedPrefs.setAdPersonalized(false);
                                    break;
                            }
                        }
                        // Consent form was closed.
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d(TAG, "Requesting Consent: onConsentFormError. Error - " + errorDescription);
                        // Consent form error_image.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }


    public void loadInterstitialAd(Context mContext, InterstitialAdLoadCallback callback) {
        AdRequest.Builder adRequest = new AdRequest.Builder();
        if (!mSharedPrefs.isAdPersonalized())
            adRequest.addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle());
        InterstitialAd.load(mContext, mContext.getResources().getString(R.string.INTERSTITIAL_AD_ID), adRequest.build(), callback);
    }

    private Bundle getNonPersonalizedAdsBundle() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        return extras;
    }

    private void showForm() {
        if (form == null) {
            Log.d(TAG, "Consent form is null");
        }
        if (form != null) {
            Log.d(TAG, "Showing consent form");
            form.show();
        } else {
            Log.d(TAG, "Not Showing consent form");
        }
    }
}

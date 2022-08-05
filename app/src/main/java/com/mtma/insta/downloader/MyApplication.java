package com.mtma.insta.downloader;

import android.app.Application;

import androidx.multidex.MultiDex;

import com.facebook.ads.AdSettings;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.util.Arrays;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        MultiDex.install(this);
        MobileAds.initialize(this);


        AdSettings.addTestDevice("4dcf36de-f250-493f-bb73-a1faf0e6dc03");

        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("33BE2250B43518CCDA7DE426D04EE231")).build();
        MobileAds.setRequestConfiguration(configuration);


    }


}

/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import me.digi.sand.app1.R;

public class MainApplication extends android.app.Application {
    public static boolean waitingForInstall;

    @Override
    public void onCreate() {
        super.onCreate();
        waitingForInstall = false;

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}

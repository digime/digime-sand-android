/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app.helpers;

import android.graphics.Color;

import me.digi.sand.app1.BuildConfig;
import me.digi.sand.app1.R;

public class Utils {

    public static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return Color.rgb(r, g, b);
    }

    public static int selectAppropriateKeyFromResources() {
        if (BuildConfig.FLAVOR.equalsIgnoreCase("live")) {
            return R.raw.sand_production;
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("sandbox")) {
            return R.raw.sandbox3;
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("staging")) {
            return R.raw.staging;
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("integration")) {
            return R.raw.integration;
        } else {
            return -1;
        }
    }
}

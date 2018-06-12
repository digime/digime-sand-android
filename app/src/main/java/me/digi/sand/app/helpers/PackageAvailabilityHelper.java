/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app.helpers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

public class PackageAvailabilityHelper {
    private static final String KEY_SESSION_TOKEN = "KEY_SESSION_TOKEN";
    private static final String KEY_APP_ID = "KEY_APP_ID";
    private static final String PERMISSION_ACCESS_INTENT_ACTION = "android.intent.action.DIGI_PERMISSION_REQUEST";
    private static final String PERMISSION_ACCESS_INTENT_TYPE = "text/plain";
    private static final String DIGI_ME_PACKAGE_ID = "me.digi.app3";

    public static boolean checkDigiMeAvailability(@NonNull Activity activity) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(PERMISSION_ACCESS_INTENT_ACTION);
        sendIntent.putExtra(KEY_SESSION_TOKEN, "dummy_data");
        sendIntent.putExtra(KEY_APP_ID, "dummy_data");
        sendIntent.setType(PERMISSION_ACCESS_INTENT_TYPE);

        return verifyIntentCanBeHandled(sendIntent, activity.getPackageManager());
    }

    public static void goToPlayStore(@NonNull Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + DIGI_ME_PACKAGE_ID)));
        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + DIGI_ME_PACKAGE_ID)));
        }
    }

    private static boolean verifyIntentCanBeHandled(Intent intent, PackageManager packageManager) {
        //TODO expose this in the SDK instead
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (activities.size() == 0) {
            return false;
        }
        //SDK should check for signatures we just do a pre-check
        return true;
    }
}

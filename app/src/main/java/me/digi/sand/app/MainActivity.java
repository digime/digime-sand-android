/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.digi.sand.app.adapters.MainPagerAdapter;
import me.digi.sand.app.adapters.NoScrollPager;
import me.digi.sand.app.adapters.SmartFragmentStatePagerAdapter;
import me.digi.sand.app.helpers.PackageAvailabilityHelper;
import me.digi.sand.app.helpers.Utils;
import me.digi.sand.app.processors.CAFileTimestampProcessor;
import me.digi.sand.app.services.SubmitManager;
import me.digi.sand.app1.BuildConfig;
import me.digi.sand.app1.R;
import me.digi.sdk.core.DigiMeClient;
import me.digi.sdk.core.SDKException;
import me.digi.sdk.core.SDKListener;
import me.digi.sdk.core.entities.CAAccounts;
import me.digi.sdk.core.entities.CAFileResponse;
import me.digi.sdk.core.entities.CAFiles;
import me.digi.sdk.core.internal.AuthorizationException;
import me.digi.sdk.core.session.CASession;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends AppCompatActivity implements SDKListener,
        HomeFragment.HomeActionListener,
        InstallDigimeFragment.InstallActionListener,
        PieChartFragment.WeeklyFragmentInteractionListener,
        BarChartFragment.DailyFragmentInteractionListener,
        SurveyFragment.SurveyInteractionListener,
        SubmitManager.FormSubmitListener
{

    private SmartFragmentStatePagerAdapter mainPagerAdapter;
    private NoScrollPager mainPager;
    private final DigiMeClient dgmClient = DigiMeClient.getInstance();
    private final AtomicInteger filesProcessed = new AtomicInteger(0);
    private ProgressDialog dialog;

    private CAFileTimestampProcessor.StatsType selectedStats;

    private static boolean isAppInBackground = false;
    private static boolean isWindowFocused = false;
    private static boolean isBackPressed = false;

    /*
        Activity LifeCycle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mainPager = (NoScrollPager) findViewById(R.id.main_pager);
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mainPager.setAdapter(mainPagerAdapter);
        mainPager.setOffscreenPageLimit(1);

        selectedStats = CAFileTimestampProcessor.StatsType.WEEKLY;
        dialog = new ProgressDialog(this);

        final int keyID = Utils.selectAppropriateKeyFromResources();
        if (keyID > 0) {
            DigiMeClient.getDefaultKeyLoader().getStore().addPKCS12KeyFromResources(this, keyID, null, TextUtils.isEmpty(BuildConfig.ALTERNATE_PASS) ? "digime" : BuildConfig.ALTERNATE_PASS, null);
        }
        DigiMeClient.minRetryPeriod = 600;
        DigiMeClient.maxRetryCount = 10;
        dgmClient.addListener(this);
        SubmitManager.getInstance().addListener(this);
    }

    @Override
    protected void onStart() {
        boolean cameFromBackground = false;
        if (isAppInBackground) {
            cameFromBackground = true;
            isAppInBackground = false;
        }
        super.onStart();
        if (cameFromBackground && MainApplication.waitingForInstall) {
            MainApplication.waitingForInstall = false;
            onStartClicked();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isWindowFocused) {
            isAppInBackground = true;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;
        if (isBackPressed && !hasFocus) {
            isBackPressed = false;
            isWindowFocused = true;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;
        super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dgmClient.getAuthManager().onActivityResult(requestCode, resultCode, data);
    }

    /*
        Listeners
     */

    //Called from initial fragment. Verify that digi.me is installed or otherwise continue with authorization.
    @Override
    public void onStartClicked() {
        if (PackageAvailabilityHelper.checkDigiMeAvailability(this)) {
            dgmClient.authorize(this, null);
        } else {
            mainPager.setCurrentItem(MainPagerAdapter.INSTALL_PAGE);
        }
    }

    //If digi.me is not available we handle redirection to play store here
    @Override
    public void onInstallClicked() {
        dgmClient.authorize(this, null);
    }

    /*
        DigiMe SDK Listener
     */

    @Override
    public void sessionCreated(CASession caSession) {
        Log.d("", caSession.getSessionKey());
    }

    @Override
    public void sessionCreateFailed(SDKException e) {
        Toast.makeText(this, "Failed to create session! Reason: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Logger.getGlobal().log(Level.WARNING, e.getMessage());
        e.printStackTrace();
        mainPager.setCurrentItem(MainPagerAdapter.MAIN_PAGE, false);
    }

    @Override
    public void authorizeSucceeded(CASession caSession) {
        mainPager.setCurrentItem(MainPagerAdapter.SPLASH_PAGE, false);
        ImportFragment frag =  (ImportFragment) mainPagerAdapter.getRegisteredFragment(mainPager.getCurrentItem());
        frag.startAnimating();
        dgmClient.getFileList(null);
    }

    @Override
    public void authorizeDenied(AuthorizationException e) {
        Toast.makeText(this, "Access to data was denied by user!", Toast.LENGTH_SHORT).show();
        Fragment frag =  mainPagerAdapter.getRegisteredFragment(mainPager.getCurrentItem());
        if (frag instanceof ImportFragment) {
            ((ImportFragment) frag).stopAnimating();
        }
        mainPager.setCurrentItem(MainPagerAdapter.MAIN_PAGE, false);
    }

    @Override
    public void authorizeFailedWithWrongRequestCode() { }

    @Override
    public void clientRetrievedFileList(CAFiles caFiles) {
        filesProcessed.set(caFiles.fileIds.size());
        for (String file: caFiles.fileIds) {
            dgmClient.getFileContent(file, null);
        }
        if (filesProcessed.get() == 0) {
            evaluateImportCompletion();
        }
    }

    @Override
    public void clientFailedOnFileList(SDKException e) {
        Toast.makeText(this, "Failed to impport file list! Reason: " + e.getMessage(), Toast.LENGTH_LONG).show();
        Fragment frag =  mainPagerAdapter.getRegisteredFragment(mainPager.getCurrentItem());
        if (frag instanceof ImportFragment) {
            ((ImportFragment) frag).stopAnimating();
        }
        mainPager.setCurrentItem(MainPagerAdapter.MAIN_PAGE, false);
    }

    @Override
    public void contentRetrievedForFile(String s, CAFileResponse caFileResponse) {
        CAFileTimestampProcessor.getInstance().processCAFiles(caFileResponse, s);
        evaluateImportCompletion();
    }

    @Override
    public void jsonRetrievedForFile(String fileId, JsonElement content) {
        Log.d("Tag", content.toString());
    }

    @Override
    public void contentRetrieveFailed(String s, SDKException e) {
        Log.e("Tag", "File failed to retrieve!");
        evaluateImportCompletion();
    }

    @Override
    public void accountsRetrieved(CAAccounts caAccounts) {

    }

    @Override
    public void accountsRetrieveFailed(SDKException e) {

    }

    private void evaluateImportCompletion() {
        int count = filesProcessed.decrementAndGet();
        if (count <= 0) {
            filesProcessed.set(0);
            Fragment frag =  mainPagerAdapter.getRegisteredFragment(mainPager.getCurrentItem());
            if (frag instanceof ImportFragment) {
                ((ImportFragment) frag).stopAnimating();
            }
            goToGraph();
        }
    }

    private void goToGraph() {
        mainPager.setCurrentItem(selectedStats == CAFileTimestampProcessor.StatsType.WEEKLY ? MainPagerAdapter.PIE_CHART_PAGE : MainPagerAdapter.BAR_CHART_PAGE);
        Fragment frag =  mainPagerAdapter.getRegisteredFragment(mainPager.getCurrentItem());
        if (frag instanceof BarChartFragment) {
            ((BarChartFragment) frag).redrawChart();
        } else if (frag instanceof PieChartFragment) {
            ((PieChartFragment) frag).redrawChart();
        }
    }

    @Override
    public void onSurveySubmitSelected(float rating, String reason) {
        if (!SubmitManager.getInstance().submitForm(rating, reason)) {
            Toast.makeText(this, "Please provide a valid answer.", Toast.LENGTH_SHORT).show();
            return;
        }
        dialog.setMessage("Submiting form...");
        dialog.show();
    }

    @Override
    public void onMainMenuClicked() {
        CAFileTimestampProcessor.getInstance().clearStats();
        selectedStats = CAFileTimestampProcessor.StatsType.WEEKLY;
        mainPager.setCurrentItem(MainPagerAdapter.MAIN_PAGE, false);
    }

    @Override
    public void onTakeSurveyClicked() {
        mainPager.setCurrentItem(MainPagerAdapter.SURVEY_PAGE);
    }

    @Override
    public void onFormSubmittedSuccessfully() {
        dialog.hide();
        Toast.makeText(this, "Survey submited successfully!", Toast.LENGTH_SHORT).show();
        mainPager.setCurrentItem(MainPagerAdapter.MAIN_PAGE);
    }

    @Override
    public void onFormSubmitFailed(String error) {
        dialog.hide();
        Toast.makeText(this, "Failed to submit survey! - " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetHourlyStatsClicked() {
        selectedStats = CAFileTimestampProcessor.StatsType.DAILY;
        goToGraph();
    }
}

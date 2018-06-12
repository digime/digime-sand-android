/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import me.digi.sand.app.BarChartFragment;
import me.digi.sand.app.HomeFragment;
import me.digi.sand.app.ImportFragment;
import me.digi.sand.app.PieChartFragment;
import me.digi.sand.app.SurveyFragment;
import me.digi.sand.app.InstallDigimeFragment;

public class MainPagerAdapter extends SmartFragmentStatePagerAdapter {
    public static final int BAR_CHART_PAGE = 3;
    public static final int PIE_CHART_PAGE = 2;
    public static final int INSTALL_PAGE = 5;
    public static final int SPLASH_PAGE = 1;
    public static final int MAIN_PAGE = 0;
    public static final int SURVEY_PAGE = 4;

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return HomeFragment.newInstance();
            case 1:
                return ImportFragment.newInstance();
            case 2:
                return PieChartFragment.newInstance(2);
            case 3:
                return BarChartFragment.newInstance(3);
            case 4:
                return SurveyFragment.newInstance(4);
            case 5:
                return InstallDigimeFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }

}

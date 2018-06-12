/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app.formatters;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class DayAxisValueFormatter implements IAxisValueFormatter {
    private final String[] mDays = new String[]{
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    };
    private final String[] mLongDays = new String[]{
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int day = (int) value;
        return mDays[day % mDays.length];
    }

    public String getLongFormattedValue(float value) {
        int day = (int) value;
        return mLongDays[day % mLongDays.length];
    }
}

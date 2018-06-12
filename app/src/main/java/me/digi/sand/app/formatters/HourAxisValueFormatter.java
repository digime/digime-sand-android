/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app.formatters;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;


public class HourAxisValueFormatter implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return getFormattedValue(Math.round(value));
    }

    public String getFormattedValue(int hourValue) {
        int hour = hourValue;
        String ts;
        if (hour > 12) {
            hour -= 12;
            ts = "pm";
        } else if (hour == 0) {
            hour += 12;
            ts = "am";
        } else if (hour == 12){
            ts = "pm";
        }else{
            ts = "am";
        }

        return String.valueOf(hour) + ts;
    }
}

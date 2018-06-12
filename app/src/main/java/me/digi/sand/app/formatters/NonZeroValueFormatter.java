/*
 * Copyright © 2018 digi.me. All rights reserved.
 */

package me.digi.sand.app.formatters;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class NonZeroValueFormatter implements IValueFormatter
{
    protected DecimalFormat mFormat;

    protected int mDecimalDigits;

    public NonZeroValueFormatter(int digits) {
        setup(digits);
    }

    public void setup(int digits) {

        this.mDecimalDigits = digits;

        StringBuffer b = new StringBuffer();
        for (int i = 0; i < digits; i++) {
            if (i == 0)
                b.append(".");
            b.append("0");
        }

        mFormat = new DecimalFormat("###,###,###,##0" + b.toString());
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        if (value < 1.0) {
            return "";
        }
        return mFormat.format(value);
    }

    public int getDecimalDigits() {
        return mDecimalDigits;
    }
}
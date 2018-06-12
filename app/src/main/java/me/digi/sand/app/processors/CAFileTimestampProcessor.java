/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app.processors;

import android.text.TextUtils;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import me.digi.sand.app.formatters.DayAxisValueFormatter;
import me.digi.sand.app.formatters.HourAxisValueFormatter;
import me.digi.sdk.core.entities.CAContent;
import me.digi.sdk.core.entities.CAFileResponse;

public class CAFileTimestampProcessor {
    private static final String TOTAL_KEY = "total";
    private static final int POST_TYPE_IDENTIFIER = 2;

    private static volatile CAFileTimestampProcessor ourInstance;
    private final ConcurrentHashMap<String, AtomicLong> dailyStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> weeklyStats = new ConcurrentHashMap<>();

    private CAFileTimestampProcessor() {
        prewarmDayLabels();
        prewarmHourLabels();
    }

    public static CAFileTimestampProcessor getInstance() {
        if (ourInstance == null) {
            synchronized (CAFileTimestampProcessor.class) {
                ourInstance = new CAFileTimestampProcessor();
            }
        }
        return ourInstance;
    }

    public void processCAFiles(CAFileResponse files, String fileName) {
        Calendar cal = Calendar.getInstance();
        TimeZone timeZone = cal.getTimeZone();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EE", Locale.UK);
        dayFormat.setTimeZone(timeZone);
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH", Locale.UK);
        hourFormat.setTimeZone(timeZone);

        if (files == null || files.fileContent == null) {
            return;
        }

        for (CAContent content: files.fileContent) {
            if (!isMyPost(content, fileName)) {
                continue;
            }
            Date dateRepresentation = new Date(content.createdDate);
            String day = dayFormat.format(dateRepresentation);
            increment(weeklyStats, day);
            String hour = hourFormat.format(dateRepresentation);
            increment(dailyStats, hour);
        }
    }

    //Get the day with maximum posts
    public String getMaxDay() {
        long max = 0;
        String selectedDay = "Monday";
        DayAxisValueFormatter formatter = new DayAxisValueFormatter();
        for (String key : weeklyStats.keySet()) {
            if (key.equals(TOTAL_KEY)) { continue; }
            if (weeklyStats.get(key).get() > max) {
                max = weeklyStats.get(key).get();
                selectedDay = formatter.getLongFormattedValue(dayToFloat(key));
            }
        }
        return selectedDay;
    }

    //Get hour slot with max posts
    public String getMaxHourRange() {
        long max = 0;
        String selectedRange = "No posts";
        HourAxisValueFormatter formatter = new HourAxisValueFormatter();
        for (String key : dailyStats.keySet()) {
            if (key.equals(TOTAL_KEY)) { continue; }
            if (dailyStats.get(key).get() > max) {
                max = dailyStats.get(key).get();
                int rangeStart = Integer.valueOf(key);
                int rangeEnd = rangeStart + 1 > 23 ? 0 : rangeStart + 1;
                selectedRange = formatter.getFormattedValue(rangeStart) + " and " + formatter.getFormattedValue(rangeEnd);
            }
        }
        return selectedRange;
    }

    public BarDataSet getHourlyDataSet() {
        List<BarEntry> entries = new ArrayList<>();
        for (String key : dailyStats.keySet()) {
            if (key.equals(TOTAL_KEY)) { continue; }
            entries.add(new BarEntry(Float.valueOf(key), dailyStats.get(key).get()));
        }

        return new BarDataSet(entries, "Hours");
    }

    public PieDataSet getDailyDataSet() {
        List<PieEntry> entries = new ArrayList<>();
        for (String key : weeklyStats.keySet()) {
            if (key.equals(TOTAL_KEY)) { continue; }
            entries.add(new PieEntry(weeklyStats.get(key).get(), key));
        }
        Collections.sort(entries, new Comparator<PieEntry>() {
            @Override
            public int compare(PieEntry o1, PieEntry o2) {
                return Math.round(dayToFloat(o1.getLabel()) - dayToFloat(o2.getLabel()));
            }
        });

        return new PieDataSet(entries, "");
    }

    private Float dayToFloat(String day) {
        String lowDay = day.toLowerCase();
        switch (lowDay) {
            case "mon":
                return 0f;
            case "tue":
                return 1f;
            case "wed":
                return 2f;
            case "thu":
                return 3f;
            case "fri":
                return 4f;
            case "sat":
                return 5f;
            case "sun":
                return 6f;
            default:
                return 0f;
        }
    }

    private void prewarmDayLabels() {
        DayAxisValueFormatter formatter = new DayAxisValueFormatter();
        for (int i = 0; i < 7; i++) {
            weeklyStats.putIfAbsent(formatter.getFormattedValue(i, null), new AtomicLong(0));
        }
    }

    private void prewarmHourLabels() {
        for (int i = 0; i < 24; i++) {
            dailyStats.putIfAbsent(String.format(Locale.getDefault(),"%02d", i), new AtomicLong(0));
        }
    }

    private void increment(Map<String, AtomicLong> map, String label) {
        AtomicLong count = map.get(label);
        if (count == null) {
            count = new AtomicLong();
            map.put(label, count);
        }
        count.incrementAndGet();
        if (!label.equals(TOTAL_KEY)) {
            increment(map, TOTAL_KEY);
        }
    }

    private boolean isMyPost(CAContent content, String fileName) {
        if (TextUtils.isEmpty(content.personEntityId)) {
            return true;
        }
        int type = -1;
        String[] identifiers = identifiers(fileName);
        if (identifiers.length >= 5)
            type = Integer.parseInt(identifiers[4]);

        String[] idParts = content.personEntityId.split("_");
        return (idParts.length < 3 || idParts[1].equals(idParts[2])) && type == POST_TYPE_IDENTIFIER;
    }

    private String[] identifiers(String fileName) {
        String fileNameWithoutExt = fileName;
        int extension = fileName.lastIndexOf('.');
        if (extension != -1) {
            fileNameWithoutExt = fileName.substring(0, extension);
        }
        return fileNameWithoutExt.split("_");
    }

    public void clearStats() {
        dailyStats.clear();
        weeklyStats.clear();
        prewarmDayLabels();
        prewarmHourLabels();
    }

    public enum StatsType {
        DAILY,
        WEEKLY;

        @SuppressWarnings("unused")
        public static CAFileTimestampProcessor.StatsType fromInteger(int x) {
            switch(x) {
                case 0:
                    return DAILY;
                case 1:
                    return WEEKLY;
            }
            return DAILY;
        }
    }
}

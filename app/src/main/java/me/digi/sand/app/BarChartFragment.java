/*
 * Copyright (c) 2009-2018 digi.me Limited. All rights reserved.
 */

package me.digi.sand.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import me.digi.sand.app.formatters.HourAxisValueFormatter;
import me.digi.sand.app.formatters.NonZeroValueFormatter;
import me.digi.sand.app.processors.CAFileTimestampProcessor;
import me.digi.sand.app1.R;

public class BarChartFragment extends Fragment {
    private BarChart mChart;
    private TextView mAnalysisText;

    private static final int[] PASTEL_COLORS = {
            Color.rgb(64, 89, 128), Color.rgb(149, 165, 124), Color.rgb(217, 184, 162),
            Color.rgb(191, 134, 134), Color.rgb(179, 48, 80), Color.rgb(193, 37, 82),
            Color.rgb(255, 102, 0), Color.rgb(245, 199, 0), Color.rgb(106, 150, 31),
            Color.rgb(179, 100, 53)
    };

    private DailyFragmentInteractionListener mListener;

    public BarChartFragment() {
    }

    public static BarChartFragment newInstance(@SuppressWarnings("SameParameterValue") int page) {
        BarChartFragment fragment = new BarChartFragment();
        Bundle args = new Bundle();
        args.putInt("pageNum", page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //noinspection UnusedAssignment
            int page = getArguments().getInt("pageNum");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bar_chart, container, false);
        mChart = view.findViewById(R.id.barChart);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setClipValuesToContent(true);

        mChart.getDescription().setEnabled(false);

        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        mChart.setFitBars(true);
        IAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(8);
        xAxis.setValueFormatter(xAxisFormatter);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(11f);
        leftAxis.setAxisMinimum(0f);

        mChart.getAxisRight().setEnabled(false);
        mChart.getLegend().setEnabled(false);

        mAnalysisText = view.findViewById(R.id.stat_analysis);

        Button mainMenuButton = view.findViewById(R.id.hourly_activity);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onMainMenuClicked();
                }
            }
        });

        return view;
    }

    @SuppressLint("SetTextI18n")
    public void redrawChart() {
        BarDataSet set = CAFileTimestampProcessor.getInstance().getHourlyDataSet();
        set.setColors(PASTEL_COLORS);
        set.setDrawValues(true);
        set.setValueFormatter(new NonZeroValueFormatter(0));
        BarData data = new BarData(set);
        data.setBarWidth(0.95f);
        mChart.setData(data);
        mChart.invalidate();

        String maxHour = CAFileTimestampProcessor.getInstance().getMaxHourRange();
        mAnalysisText.setText("You're most active between " + maxHour);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DailyFragmentInteractionListener) {
            mListener = (DailyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DailyFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface DailyFragmentInteractionListener {
        void onMainMenuClicked();
        void onTakeSurveyClicked();
    }
}

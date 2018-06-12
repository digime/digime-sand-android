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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import me.digi.sand.app.formatters.NonZeroValueFormatter;
import me.digi.sand.app.helpers.Utils;
import me.digi.sand.app.processors.CAFileTimestampProcessor;
import me.digi.sand.app1.R;

public class PieChartFragment extends Fragment {
    private PieChart mChart;
    private TextView mAnalysisText;

    private static final int[] DAY_COLORS = {
            Utils.rgb("#f6d500"), Utils.rgb("#c0cafa"), Utils.rgb("#d1e21a"), Utils.rgb("#f2a800"), Utils.rgb("#9575ba"), Utils.rgb("#ea593f"), Utils.rgb("#cfc096")
    };

    private PieChartFragment.WeeklyFragmentInteractionListener mListener;

    public PieChartFragment() { }

    public static PieChartFragment newInstance(@SuppressWarnings("SameParameterValue") int page) {
        PieChartFragment fragment = new PieChartFragment();
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

        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        mChart = view.findViewById(R.id.pieChart);
        mChart.setUsePercentValues(false);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setDrawHoleEnabled(true);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setHoleColor(Utils.rgb("#56CCF2"));
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        mChart.getLegend().setEnabled(true);
        Legend l = mChart.getLegend();
        l.setFormSize(15f);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(5f);
        mChart.setDrawEntryLabels(false);
//        mChart.setEntryLabelColor(Color.WHITE);
//        mChart.setEntryLabelTextSize(12f);

        Button surveyButton = view.findViewById(R.id.take_survey_button);
        surveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onGetHourlyStatsClicked();
                }
            }
        });

        mAnalysisText = view.findViewById(R.id.stat_hourly_analysis);

        return view;
    }

    @SuppressLint("SetTextI18n")
    public void redrawChart() {
        PieDataSet set = CAFileTimestampProcessor.getInstance().getDailyDataSet();
        set.setDrawIcons(false);

        set.setColors(DAY_COLORS);

        PieData data = new PieData(set);
        data.setValueFormatter(new NonZeroValueFormatter(0));
        data.setValueTextSize(19f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        mChart.highlightValues(null);
        mChart.invalidate();

        String maxDay = CAFileTimestampProcessor.getInstance().getMaxDay();
        mAnalysisText.setText(maxDay + " " + getString(R.string.concat_text));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WeeklyFragmentInteractionListener) {
            mListener = (WeeklyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WeeklyFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface WeeklyFragmentInteractionListener {
        void onGetHourlyStatsClicked();
    }
}

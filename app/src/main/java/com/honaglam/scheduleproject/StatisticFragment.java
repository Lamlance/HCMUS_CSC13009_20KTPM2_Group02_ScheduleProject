package com.honaglam.scheduleproject;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StatisticFragment extends Fragment {

    private static final int MAX_X_SHOW = 7;
    private static final int MAX_X_VALUE = 30;
    private static final int MAX_Y_VALUE = 50;
    private static final int MIN_Y_VALUE = 5;
    private static final int GROUPS = 2;
    private static final String GROUP_1_LABEL = "Work time";
    private static final String GROUP_2_LABEL = "Break time";
    private static final float BAR_SPACE = 0.2f;
    private static final float BAR_WIDTH = 0.25f;

    private static final double WORK_TIME = 358.7;

    private BarChart barChart;
    private PieChart pieChart;
    private TextView txtTotalTime;

    private MainActivity activity;


    private List<ReminderTaskDB.TimerStatsData> data;


    public static StatisticFragment newInstance() {
        StatisticFragment fragment = new StatisticFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public StatisticFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.activity = (MainActivity) getActivity();
        float workHours = 0;

        if (this.activity != null) {
            this.data = this.activity.get30StatsBeforeToday();
            Collections.reverse(this.data);
            workHours = this.data.stream().mapToLong(e -> e.workDur).sum();
            workHours = workHours / (1000 * 60 * 60);
        }

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_statistic, container, false);

        BarChart barChart = linearLayout.findViewById(R.id.barChart);
        PieChart pieChart = linearLayout.findViewById(R.id.pieChart);
        TextView txtTotalTime = linearLayout.findViewById(R.id.txtTotalTime);

        BarData data = createBarChartData();
        this.barChart = barChart;
        configureChartAppearance();
        prepareChartData(data);

        PieData pieData = createPieCharData();
        this.pieChart = pieChart;
        configurePieChartAppearance();
        preparePieChartData(pieData);

        txtTotalTime.setText("You have focused " + String.format("%.2f", workHours) + " hours in 30 days recently.");
        return linearLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private BarData createBarChartData() {
        Random rng = new Random();

        ArrayList<BarEntry> values1 = new ArrayList<>();
        ArrayList<BarEntry> values2 = new ArrayList<>();

        for (int i = 0; i < this.data.size(); i++) {
            values1.add(new BarEntry(i, this.data.get(i).workDur));
            values2.add(new BarEntry(i, this.data.get(i).shortDur + this.data.get(i).longDur));
        }

        BarDataSet set1 = new BarDataSet(values1, GROUP_1_LABEL);
        BarDataSet set2 = new BarDataSet(values2, GROUP_2_LABEL);

        set1.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        set2.setColor(ColorTemplate.MATERIAL_COLORS[1]);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);

        return new BarData(dataSets);
    }
    private void configureChartAppearance() {
        barChart.setPinchZoom(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(true);
        barChart.setDrawValueAboveBar(true);

        barChart.getDescription().setEnabled(false);

        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < this.data.size(); i++) {
            int date = this.data.get(i).date;
            int month = this.data.get(i).month;
            labels.add(String.format("%d/%d", date, month));
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(MAX_X_VALUE);
        barChart.setVisibleXRangeMaximum(MAX_X_SHOW);


    }
    private void prepareChartData(BarData data) {
        barChart.setData(data);

        barChart.getBarData().setBarWidth(BAR_WIDTH);

        float groupSpace = 1f - ((BAR_SPACE + BAR_WIDTH) * GROUPS);
        barChart.groupBars(0, groupSpace, BAR_SPACE);

        barChart.invalidate();
    }

    private PieData createPieCharData() {
        List<PieEntry> entries = new ArrayList<>();

        long workTime = this.data.stream().mapToLong(e -> e.workDur).sum();
        long breakTime = this.data.stream().mapToLong(e -> (e.longDur + e.shortDur)).sum();


        float workPercent = (float) workTime / (workTime + breakTime);
        workPercent = Math.round(workPercent * 100);
        float breakPercent = 100 - workPercent;

        entries.add(new PieEntry(workPercent, "Work"));
        entries.add(new PieEntry(breakPercent, "Break"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        return new PieData(dataSet);
    }

    private void configurePieChartAppearance() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Work/ Break");
    }

    private void preparePieChartData(PieData data) {
        pieChart.setData(data);
        pieChart.invalidate();
    }
}

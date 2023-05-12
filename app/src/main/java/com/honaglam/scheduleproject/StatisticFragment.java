package com.honaglam.scheduleproject;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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
import com.github.mikephil.charting.components.LegendEntry;
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
import com.honaglam.scheduleproject.Repository.StatsRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class StatisticFragment extends Fragment {

  private static final int MAX_X_SHOW = 7;
  private static final int MAX_X_VALUE = 30;

  private static final int MILIS_TO_MINS = 60 * 1000;
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
  Integer txtPrimaryColor = null;

  private @Nullable List<ReminderTaskFireBase.TimerStats> data = null;
  static StatsRepository statsRepository;

  public static StatisticFragment newInstance(String userId) {
    StatisticFragment fragment = new StatisticFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    if(statsRepository == null){
        statsRepository = new StatsRepository(userId);
    }

    return fragment;
  }

  public StatisticFragment() {

  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.activity = (MainActivity) getActivity();

        /*
        if (this.activity != null) {
            //this.data = this.activity.get30StatsBeforeToday();
            // Collections.reverse(this.data);
            workHours = this.data.stream().mapToLong(e -> e.workDur).sum();
            workHours = workHours / (1000 * 60 * 60);
        }
         */


    // set text primary color
    if (txtPrimaryColor == null) {
      TypedValue textTypedValue = new TypedValue();
      Resources.Theme textTheme = container.getContext().getTheme();
      textTheme.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, textTypedValue, true);
      txtPrimaryColor = textTypedValue.data;
    }

    statsRepository.SetGetStatsCompleted((stats) -> {
      data = new LinkedList<>(stats);
      reDrawChartsAndText();
    });

    return (LinearLayout) inflater.inflate(R.layout.fragment_statistic, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    this.barChart = view.findViewById(R.id.barChart);
    this.pieChart = view.findViewById(R.id.pieChart);
    txtTotalTime = view.findViewById(R.id.txtTotalTime);

    statsRepository.get30Stats30DaysBefore();
  }

  private void reDrawChartsAndText(){
    float workHours = 0;
    //Collections.reverse(this.data);

    workHours = this.data.stream().mapToLong(e -> e.workDur).sum();
    workHours = workHours / (1000 * 60 * 60); //Millis -> hour

    Log.i("STATS","Data size " + data.size());
    Log.i("STATS","Work hour " + workHours);

    BarData barChartData = createBarChartData();
    configureChartAppearance();
    prepareChartData(barChartData);

    PieData pieData = createPieCharData();
    configurePieChartAppearance();
    preparePieChartData(pieData);

    txtTotalTime.setText("You have focused " + String.format("%.2f", workHours) + " hours in 30 days recently.");
    txtTotalTime.setTextColor(txtPrimaryColor);
  }

  private BarData createBarChartData() {
    if(data == null){
      return null;
    }

    Random rng = new Random();

    ArrayList<BarEntry> values1 = new ArrayList<>();
    ArrayList<BarEntry> values2 = new ArrayList<>();

    for (int i = 0; i < this.data.size(); i++) {
      ReminderTaskFireBase.TimerStats stats = this.data.get(i);

      values1.add(new BarEntry(i, (float) stats.workDur / (float) MILIS_TO_MINS));
      values2.add(new BarEntry(i, (float) (this.data.get(i).shortDur + this.data.get(i).longDur) / MILIS_TO_MINS));
    }


    BarDataSet set1 = new BarDataSet(values1, GROUP_1_LABEL);
    BarDataSet set2 = new BarDataSet(values2, GROUP_2_LABEL);
    set1.setColor(ColorTemplate.MATERIAL_COLORS[0]);
    set2.setColor(ColorTemplate.MATERIAL_COLORS[1]);

    set1.setDrawValues(false);
    set2.setDrawValues(false);

    ArrayList<IBarDataSet> dataSets = new ArrayList<>();
    dataSets.add(set1);
    dataSets.add(set2);

    return new BarData(dataSets);
  }

  private void configureChartAppearance() {
    barChart.setPinchZoom(false);
    barChart.setDrawBarShadow(false);
    barChart.setDrawGridBackground(true);
    barChart.setDrawValueAboveBar(false);

    barChart.getDescription().setEnabled(false);

    ArrayList<String> labels = new ArrayList<>();

    Calendar calendar = Calendar.getInstance();
    for (int i = 0; i < this.data.size(); i++) {
      calendar.setTimeInMillis(this.data.get(i).createDate);

      int date = calendar.get(Calendar.DATE);
      int month = calendar.get(Calendar.MONTH);
      labels.add(String.format("%d/%d", date, month + 1));
    }

    XAxis xAxis = barChart.getXAxis();
    xAxis.setGranularity(1f);
    xAxis.setDrawGridLines(true);
    xAxis.setDrawAxisLine(false);
    xAxis.setCenterAxisLabels(false);
    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
    xAxis.setTextColor(txtPrimaryColor);
    YAxis leftAxis = barChart.getAxisLeft();
    leftAxis.setDrawGridLines(true);
    leftAxis.setDrawAxisLine(true);
    leftAxis.setSpaceTop(35f);
    leftAxis.setAxisMinimum(0f);
    leftAxis.setTextColor(txtPrimaryColor);

    barChart.getAxisRight().setEnabled(false);
    barChart.getXAxis().setAxisMinimum(0);
    barChart.getXAxis().setAxisMaximum(MAX_X_VALUE);
    barChart.setVisibleXRangeMaximum(MAX_X_SHOW);


  }

  private void prepareChartData(BarData data) {
    barChart.setData(data);
    barChart.getBarData().setBarWidth(BAR_WIDTH);
    Legend k = barChart.getLegend();
    k.setTextColor(txtPrimaryColor);
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
    Legend l = pieChart.getLegend();
    l.setTextColor(txtPrimaryColor);
    pieChart.invalidate();
  }
}

package com.toedro.fao.ui.charts;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.toedro.fao.App;
import com.toedro.fao.R;
import com.toedro.fao.db.StepsQueryResult;
import com.toedro.fao.ui.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class ChartsFragment extends Fragment {

    BarChart barChartViewStep, barChartViewCal;
    LinearLayout ChartsGraphsLayoutSteps, ChartsGraphsLayoutCalories;
    MaterialButtonToggleGroup materialButtonToggleGroup;
    com.google.android.material.textfield.TextInputLayout textInputFrom, textInputTo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_charts, container, false);

        // Get objects from layout
        ChartsGraphsLayoutSteps     = (LinearLayout) root.findViewById(R.id.StepsGraphsLayout);
        ChartsGraphsLayoutCalories  = (LinearLayout) root.findViewById(R.id.CalGraphsLayout);
        barChartViewCal             = (BarChart) root.findViewById(R.id.BarCalChart);
        barChartViewStep            = (BarChart) root.findViewById(R.id.BarStepChart);
        textInputFrom               = (TextInputLayout) root.findViewById(R.id.chartsTextFieldDateStart);
        textInputTo                 = (TextInputLayout) root.findViewById(R.id.chartsTextFieldDateEnd);
        materialButtonToggleGroup   = (MaterialButtonToggleGroup) root.findViewById(R.id.toggleButtonGroup);

        // Set default date values
        LocalDate defaultDateTo     = LocalDate.now();
        LocalDate defaultDateFrom   = LocalDate.now().minusDays(5);
        textInputTo.getEditText().setText(defaultDateTo.format(DateTimeFormatter.ofPattern(getString(R.string.date_layout_UI))));
        textInputFrom.getEditText().setText(defaultDateFrom.format(DateTimeFormatter.ofPattern(getString(R.string.date_layout_UI))));

        Calendar calendarFrom   = Calendar.getInstance();
        // Set initial values for the first time the user click in the from date selection
        /*
        calendarFrom.set(Calendar.YEAR, defaultDateFrom.getYear());
        calendarFrom.set(Calendar.MONTH, defaultDateFrom.getMonthValue());
        calendarFrom.set(Calendar.DAY_OF_MONTH, defaultDateFrom.getDayOfMonth());
        */
        Calendar calendarTo     = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener dateFrom = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendarFrom.set(Calendar.YEAR, year);
                calendarFrom.set(Calendar.MONTH, monthOfYear);
                calendarFrom.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
                generateChart();
            }
            private void updateLabel() {
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_layout_UI), Locale.US);
                textInputFrom.getEditText().setText(sdf.format(calendarFrom.getTime()));
            }
        };

        final DatePickerDialog.OnDateSetListener dateTo = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendarTo.set(Calendar.YEAR, year);
                calendarTo.set(Calendar.MONTH, monthOfYear);
                calendarTo.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
                generateChart();
            }
            private void updateLabel() {
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.date_layout_UI), Locale.US);
                textInputTo.getEditText().setText(sdf.format(calendarTo.getTime()));

                // If the new To date is before the From date
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_layout_UI));
                LocalDate dateFrom  = LocalDate.parse(textInputFrom.getEditText().getText(), dateFormatter);
                LocalDate dateTo    = LocalDate.parse(textInputTo.getEditText().getText(), dateFormatter);

                if(dateFrom.isAfter(dateTo)) { // Then there is a problem!
                    textInputFrom.getEditText().setText(dateTo.minusDays(5).format(dateFormatter));
                }
            }
        };

        textInputTo.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(), dateTo,
                        calendarTo.get(Calendar.YEAR),
                        calendarTo.get(Calendar.MONTH),
                        calendarTo.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });
        textInputFrom.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(), dateFrom,
                        calendarFrom.get(Calendar.YEAR),
                        calendarFrom.get(Calendar.MONTH),
                        calendarFrom.get(Calendar.DAY_OF_MONTH));
                try {
                    dialog.getDatePicker().setMaxDate(new SimpleDateFormat(getString(R.string.date_layout_UI))
                            .parse(textInputTo.getEditText().getText().toString()).getTime());
                    dialog.show();
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing the TO date.", e);
                }
            }
        });

        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                generateChart();
            }
        });

        // Show calories by default
        materialButtonToggleGroup.setSelectionRequired(true);
        materialButtonToggleGroup.check(R.id.toggleCal);

        return root;
    }

    private void generateChart() {
        if (materialButtonToggleGroup.getCheckedButtonId() == R.id.toggleSteps) {
            loadBarData(barChartViewStep);
            barChartViewStep.setFitBars(true);
            barChartViewStep.getDescription().setText("");
            barChartViewStep.getLegend().setEnabled(false);
            ChartsGraphsLayoutCalories.setVisibility(View.GONE);
            ChartsGraphsLayoutSteps.setVisibility(View.VISIBLE);
        } else if(materialButtonToggleGroup.getCheckedButtonId() == R.id.toggleCal) {
            loadBarData(barChartViewCal);
            barChartViewCal.setFitBars(true);
            barChartViewCal.getDescription().setText("");
            barChartViewCal.getLegend().setEnabled(false);
            ChartsGraphsLayoutCalories.setVisibility(View.VISIBLE);
            ChartsGraphsLayoutSteps.setVisibility(View.GONE);
        }
        barChartViewStep.invalidate();
        barChartViewCal.invalidate();
    }

    private void loadBarData(BarChart chart){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_layout_UI));
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();

        // Graphical setup of the chart
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setGranularity(1f);
        chart.getAxisLeft().setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        chart.getAxisLeft().setSpaceTop(15f);
        chart.getAxisLeft().setAxisMinimum(0f);

        // Get data from the DB
        List<StepsQueryResult> stepsByDay = App.getDBInstance().stepDAO().getSteps(
                Utils.generateDateIntervals(
                        LocalDate.parse(textInputFrom.getEditText().getText(), dateFormatter),
                        LocalDate.parse(textInputTo.getEditText().getText(), dateFormatter)));

        // If the query returns no data
        if(stepsByDay.size() == 1 && stepsByDay.get(0).getSteps() == 0) {
            Snackbar.make(getView(), R.string.charts_error, Snackbar.LENGTH_LONG).show();
            chart.setData(null);
            return;
        }

        int i = 0;
        for (StepsQueryResult entry : stepsByDay) {
            BarEntry val = new BarEntry(i++, Float.valueOf(
                    materialButtonToggleGroup.getCheckedButtonId() == R.id.toggleSteps ?
                            entry.getSteps().toString() :
                            String.valueOf(Utils.convertStepsToCal(entry.getSteps(), getActivity(), getContext()))
            ));
            xVals.add(entry.getDay());
            yVals.add(val);
        }

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xVals));
        BarDataSet barDataSet = new BarDataSet(yVals, getString(R.string.charts_label_plot));
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);
        ArrayList<IBarDataSet> dataSet = new ArrayList<IBarDataSet>();
        dataSet.add(barDataSet);

        BarData barData = new BarData(dataSet);
        barData.setValueTextSize(10f);
        barData.setBarWidth(0.9f);

        chart.setData(barData);
    }
}
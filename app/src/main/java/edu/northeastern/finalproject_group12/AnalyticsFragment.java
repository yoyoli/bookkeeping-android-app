package edu.northeastern.finalproject_group12;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AnalyticsFragment extends Fragment {

    private SQLiteControllerHelper dbHelper;
    private TextView selectText, tvTotalExpense, tvAverageExpense, tvMaxExpense, tvBalance;
    private Button btnSelectYear;
    private Button btnSelectMonthYear;

    private CombinedChart combinedChart;
    private PieChart pieChart;
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private int currentYear;
    private int currentMonth;
    private boolean isMonthSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        dbHelper = new SQLiteControllerHelper(getContext(), "transactions.db", null, 1);

        selectText = view.findViewById(R.id.selectText);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        tvAverageExpense = view.findViewById(R.id.tvAverageExpense);
        tvMaxExpense = view.findViewById(R.id.tvMaxExpense);
        btnSelectYear = view.findViewById(R.id.button_year);
        btnSelectMonthYear = view.findViewById(R.id.button_month);

        combinedChart = view.findViewById(R.id.combined_chart);
        pieChart = view.findViewById(R.id.pie_chart);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvBalance = view.findViewById(R.id.tvBalance);

        btnSelectYear.setOnClickListener(v -> showYearPicker());
        btnSelectMonthYear.setOnClickListener(v -> showMonthYearPicker());

        if (savedInstanceState != null) {
            currentYear = savedInstanceState.getInt("selectedYear");
            currentMonth = savedInstanceState.getInt("selectedMonth");
            isMonthSelected = savedInstanceState.getBoolean("isMonthSelected");

            if (isMonthSelected) {
                updateStatsForMonth(currentYear, currentMonth);
            } else {
                updateStatsForYear(currentYear);
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            currentYear = calendar.get(Calendar.YEAR);
            currentMonth = calendar.get(Calendar.MONTH) + 1;

            updateStatsForMonth(currentYear, currentMonth);
            isMonthSelected = true;
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedYear", currentYear);
        outState.putInt("selectedMonth", currentMonth);
        outState.putBoolean("isMonthSelected", isMonthSelected);
    }


    private void showYearPicker() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.year_picker_dialog, null);
        dialog.setView(dialogView);

        final NumberPicker yearPicker = dialogView.findViewById(R.id.year_picker);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMaxValue(year + 76);
        yearPicker.setMinValue(year - 124);
        yearPicker.setValue(year);

        dialog.setTitle("Select Year");
        dialog.setPositiveButton("OK", (dialogInterface, i) -> {
            int selectedYear = yearPicker.getValue();

            updateStatsForYear(selectedYear);
        });
        dialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    private void showMonthYearPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.month_year_picker_dialog, null);
        final NumberPicker yearPicker = dialogView.findViewById(R.id.year_picker);
        final NumberPicker monthPicker = dialogView.findViewById(R.id.month_picker);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMaxValue(year + 76);
        yearPicker.setMinValue(year - 124);
        yearPicker.setValue(year);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1);

        builder.setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedYear = yearPicker.getValue();
                    int selectedMonth = monthPicker.getValue();
                    updateStatsForMonth(selectedYear, selectedMonth);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void updateStatsForYear(int year) {
        currentYear = year;
        isMonthSelected = false;

        double total = dbHelper.getTotalExpenseForYear(String.valueOf(year));
        double avg = dbHelper.getAverageExpenseForYear(String.valueOf(year));
        double max = dbHelper.getMaxExpenseForYear(String.valueOf(year));
        double yearlyBudget = dbHelper.getMonthlyBudget() * 12;
        double yearBalance = yearlyBudget - total;

        selectText.setText(String.format(Locale.US, "Year: %d", year));
        tvTotalExpense.setText(String.format(Locale.US, "Total: %.2f", total));
        tvAverageExpense.setText(String.format(Locale.US, "Average per month: %.2f", avg));
        tvMaxExpense.setText(String.format(Locale.US, "High: %.2f", max));
        if (yearlyBudget > 0) {
            tvBalance.setText(String.format(Locale.US, "Balance: %.2f", yearBalance));
            tvBalance.setVisibility(View.VISIBLE);
        } else {
            tvBalance.setVisibility(View.GONE);
        }

        // draw combined chart
        List<Entry> lineEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            double monthlyExpense = dbHelper.getTotalExpenseForMonth(String.valueOf(year), String.format(Locale.US, "%02d", i + 1));
            lineEntries.add(new Entry(i, (float) monthlyExpense));
            barEntries.add(new BarEntry(i, (float) monthlyExpense));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Monthly Expenses");
        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                if (entry.getY() == (long) entry.getY()) {
                    return String.format(Locale.US, "%d", (long) entry.getY());
                } else {
                    return String.format(Locale.US, "%.2f", entry.getY());
                }
            }
        });

        BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly Expenses");
        barDataSet.setColor(Color.parseColor("#ADD8E6"));
        barDataSet.setDrawValues(false);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(new LineData(lineDataSet));
        combinedData.setData(new BarData(barDataSet));

        combinedChart.setData(combinedData);
        combinedChart.invalidate();

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12, true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getMonthsLabels()));

        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = combinedChart.getAxisRight();
        rightAxis.setAxisMinimum(0f);

        combinedChart.getAxisLeft().removeAllLimitLines();

        // Max expense
        LimitLine limitLine = new LimitLine((float) max, "");
        limitLine.setLineWidth(2f);
        limitLine.setLineColor(Color.GRAY);
        combinedChart.getAxisLeft().addLimitLine(limitLine);

        // Average expense line
        LimitLine avgLimitLine = new LimitLine((float) avg, "");
        avgLimitLine.setLineWidth(1f);
        avgLimitLine.setLineColor(Color.LTGRAY);
        avgLimitLine.enableDashedLine(10f, 10f, 0f);
        combinedChart.getAxisLeft().addLimitLine(avgLimitLine);

        combinedChart.getDescription().setEnabled(false);
        combinedChart.getLegend().setEnabled(false);

        // Update category expenses
        List<CategoryExpense> categoryExpenses = dbHelper.getCategoryExpensesForYear(String.valueOf(year));
        categoryAdapter = new CategoryAdapter(categoryExpenses);
        recyclerView.setAdapter(categoryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        updatePieChart(categoryExpenses);
    }

    private void updatePieChart(List<CategoryExpense> categoryExpenses) {
        List<PieEntry> pieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Dynamically generate colors
        for (CategoryExpense expense : categoryExpenses) {
            if (expense != null) {
                pieEntries.add(new PieEntry((float) expense.getTotalAmount(), expense.getCategory()));
                colors.add(generateRandomColor());
            }
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(colors);
        pieDataSet.setValueFormatter(new PercentFormatter(pieChart));
        pieDataSet.setValueTextSize(12f);

        // Enable drawing values outside the pie chart
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueLinePart1Length(0.2f);
        pieDataSet.setValueLinePart2Length(0.8f);
        pieDataSet.setValueLineColor(Color.BLACK);
        pieDataSet.setValueLineWidth(1f);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        // Customize the legend to ensure all categories are displayed
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(12f);
        legend.setFormSize(12f);

        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawEntryLabels(false); // Hide entry labels on the pie chart itself
    }

    // Function to generate random color
    private int generateRandomColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private List<String> getMonthsLabels() {
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    private void updateStatsForMonth(int year, int month) {
        currentYear = year;
        currentMonth = month;
        isMonthSelected = true;

        String yearStr = String.valueOf(year);
        String monthStr = String.format(Locale.US, "%02d", month);

        double total = dbHelper.getTotalExpenseForMonth(yearStr, monthStr);
        double avg = dbHelper.getAverageExpenseForMonth(yearStr, monthStr);
        double max = dbHelper.getMaxDailyExpenseForMonth(yearStr, monthStr);
        double monthlyBudget = dbHelper.getMonthlyBudget();
        double monthBalance = monthlyBudget - total;

        selectText.setText(String.format(Locale.US, "Year: %d Month: %d", year, month));
        tvTotalExpense.setText(String.format(Locale.US, "Total: %.2f", total));
        tvAverageExpense.setText(String.format(Locale.US, "Average per day: %.2f", avg));
        tvMaxExpense.setText(String.format(Locale.US, "High: %.2f", max));

        if (monthlyBudget > 0) {
            tvBalance.setText(String.format(Locale.getDefault(), "Balance: %.2f", monthBalance));
            tvBalance.setVisibility(View.VISIBLE);
        } else {
            tvBalance.setVisibility(View.GONE);
        }

        // draw combined chart
        List<Entry> lineEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        int daysInMonth = getDaysInMonth(year, month);
        for (int i = 1; i <= daysInMonth; i++) {
            double dailyExpense = dbHelper.getDailyExpenseForMonth(yearStr, monthStr, String.format(Locale.US, "%02d", i));
            lineEntries.add(new Entry(i, (float) dailyExpense));
            barEntries.add(new BarEntry(i, (float) dailyExpense));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Daily Expenses");
        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                if (entry.getY() == (long) entry.getY()) {
                    return String.format(Locale.US, "%d", (long) entry.getY());
                } else {
                    return String.format(Locale.US, "%.2f", entry.getY());
                }
            }
        });

        BarDataSet barDataSet = new BarDataSet(barEntries, "Daily Expenses");
        barDataSet.setColor(Color.parseColor("#ADD8E6"));
        barDataSet.setDrawValues(false);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(new LineData(lineDataSet));
        combinedData.setData(new BarData(barDataSet));

        combinedChart.setData(combinedData);
        combinedChart.invalidate();

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(daysInMonth, false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getDaysLabels(daysInMonth)));
        xAxis.setTextSize(8f);

        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = combinedChart.getAxisRight();
        rightAxis.setAxisMinimum(0f);

        combinedChart.getAxisLeft().removeAllLimitLines();

        // Max expense
        LimitLine limitLine = new LimitLine((float) max, "");
        limitLine.setLineWidth(2f);
        limitLine.setLineColor(Color.GRAY);
        combinedChart.getAxisLeft().addLimitLine(limitLine);

        // Average expense line
        LimitLine avgLimitLine = new LimitLine((float) avg, "");
        avgLimitLine.setLineWidth(1f);
        avgLimitLine.setLineColor(Color.LTGRAY);
        avgLimitLine.enableDashedLine(10f, 10f, 0f);
        combinedChart.getAxisLeft().addLimitLine(avgLimitLine);

        combinedChart.getDescription().setEnabled(false);
        combinedChart.getLegend().setEnabled(false);

        List<CategoryExpense> categoryExpenses = dbHelper.getCategoryExpensesForMonth(yearStr, monthStr);
        categoryAdapter = new CategoryAdapter(categoryExpenses);
        recyclerView.setAdapter(categoryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        updatePieChart(categoryExpenses);
    }

    private List<String> getDaysLabels(int daysInMonth) {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i <= daysInMonth; i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    private int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}


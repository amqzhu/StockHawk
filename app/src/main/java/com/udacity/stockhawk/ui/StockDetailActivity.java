package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockDetailActivity extends AppCompatActivity {
    private TextView mTickerTV;
    private LineChart mChart;
    private TextView mEmptyTV;
    private String history;
    private String[] lines;
    private List<Entry> mEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        mTickerTV = (TextView) findViewById(R.id.stock_detail_ticker);
        mChart = (LineChart) findViewById(R.id.stock_detail_chart);
        mEmptyTV = (TextView) findViewById(R.id.stock_detail_empty);

        Intent intentThatStartedThisActivity = getIntent();

        // get extra info if present in intent
        // set company ticker and graph if info is present, otherwise set empty view
        if (intentThatStartedThisActivity.hasExtra(getString(R.string.stock_detail_symbol))) {
            mEmptyTV.setVisibility(View.GONE);
            mTickerTV.setVisibility(View.VISIBLE);

            // get stock ticker from MainActivity
            String ticker = intentThatStartedThisActivity.getStringExtra(getString(R.string.stock_detail_symbol));
            mTickerTV.setText(ticker);

            // get historical data for stock
            Cursor historyCursor = getContentResolver().query(Contract.Quote.makeUriForStock(ticker),
                    new String[]{Contract.Quote.COLUMN_HISTORY},
                    null,
                    null,
                    null);

            // graph historical data if exists
            if(historyCursor.moveToFirst()) {
                mChart.setVisibility(View.VISIBLE);

                history = historyCursor.getString(0);

                // make a String array of (timestamp, price)
                lines = history.split("\\r?\\n");
                mEntries = new ArrayList<Entry>();

                // add each (timestamp, price) from last entry to the first entry
                // NOTE: MUST add from smallest x value to largest x value
                for (int i = lines.length-1; i >= 0; i--) {
                    String line = lines[i];
                    // split data into timestamp and price
                    String dat[] = line.split(", ");
                    mEntries.add(new Entry(Float.parseFloat(dat[0]), Float.parseFloat(dat[1])));
                }

                LineDataSet dataSet = new LineDataSet(mEntries, getString(R.string.stock_price));
                LineData lineData = new LineData(dataSet);

                // set x axis to bottom of chart
                // set special formatter for x axis labels
                XAxis xAxis = mChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new DayFormatter());

                // clear description label
                Description description = new Description();
                description.setText("");
                mChart.setDescription(description);

                // draw data
                mChart.setData(lineData);
                mChart.invalidate();

            } else {
                mChart.setVisibility(View.GONE);
            }
        } else {
            mEmptyTV.setVisibility(View.VISIBLE);
            mTickerTV.setVisibility(View.GONE);
        }
    }

    // special day formatter to convert timestamp to human-readable time
    class DayFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Date date = new Date((long) value);
            return DateFormat.getDateInstance().format(date);
        }
    }
}
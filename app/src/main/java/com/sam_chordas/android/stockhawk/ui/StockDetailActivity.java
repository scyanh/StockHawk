package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Collections;

public class StockDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CURSOR_LOADER_ID = 0;
    private static final String TAG = "StockDetailActivity";
    private Cursor mCursor;
    private LineChartView lineChartView;
    private LineSet mLineSet;
    int maxRange,minRange,step;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mLineSet = new LineSet();
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        //initLineChart();
        Intent intent = getIntent();
        Bundle args = new Bundle();
        args.putString(getResources().getString(R.string.string_symbol), intent.getStringExtra(getResources().getString(R.string.string_symbol)));
        setTitle(intent.getStringExtra(getResources().getString(R.string.string_symbol)));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = QuoteColumns._ID + " DESC LIMIT 10";
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(getResources().getString(R.string.string_symbol))},
                //null);
                sortOrder);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "zz in onLoadFinished");
        mCursor = data;
        findRange(mCursor);
        initLineChart();
        fillLineSet();
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void fillLineSet(){
        Log.i(TAG, "zz in fillLineSet");
        //mCursor.moveToFirst();
        mCursor.moveToLast();
        for (int i = 0; i < mCursor.getCount(); i++){
            float price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            if(i==mCursor.getCount()-1){
                mLineSet.addPoint("Today", price);
            }else if(i==0){
                mLineSet.addPoint("Last 10 days", price);
            }else{
                mLineSet.addPoint(" ", price);
            }

            //mCursor.moveToNext();
            mCursor.moveToPrevious();
        }
        mLineSet.setColor(getResources().getColor(R.color.line_set))
                .setDotsStrokeThickness(Tools.fromDpToPx(2))
                .setDotsStrokeColor(getResources().getColor(R.color.line_stroke))
                .setDotsColor(getResources().getColor(R.color.line_dots));
        lineChartView.addData(mLineSet);
        lineChartView.show();
    }

    private void initLineChart() {
        Log.i(TAG, "zz in initLineChart");
        Log.i(TAG, "zz minRange="+minRange+" maxRange="+maxRange);
        //step = Math.round((maxRange*1.0f - minRange*1.0f)/10);
        //Log.i(TAG, "zz step="+step);

        Paint gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.line_paint));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(1f));
        lineChartView.setBorderSpacing(1)
                //.setAxisBorderValues(minRange-100, maxRange+100, 50)
                //.setAxisBorderValues(minRange, maxRange, step)
                .setAxisBorderValues(minRange, maxRange)
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(getResources().getColor(R.color.line_labels))
                .setXAxis(false)
                .setYAxis(false)
                .setBorderSpacing(Tools.fromDpToPx(5))
                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
    }
    public void findRange(Cursor mCursor)
    {
        Log.i(TAG, "zz in findRange");
        ArrayList<Float> mArrayList = new ArrayList<Float>();
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            // The Cursor is now set to the right position
            mArrayList.add(Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE))));
        }
        maxRange = Math.round(Collections.max(mArrayList))+1;
        minRange = Math.round(Collections.min(mArrayList))-1;
        //if(minRange>100)
        //    minRange = minRange-100;
        //if(maxRange-minRange>10)
        //    step = Math.round((maxRange*1.0f - minRange*1.0f)/10);
        //if(step==0)
            //step=10;
        step = Math.round((maxRange*1.0f - minRange*1.0f)/10);
        Log.i(TAG, "zz minRange="+minRange+" maxRange="+maxRange);
        Log.i(TAG, "zz step="+step);

    }
}
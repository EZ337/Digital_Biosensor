package com.example.myapplication.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.myapplication.R;
import com.example.myapplication.datasystem.DataStore;
import com.example.myapplication.util.ActivityTransitions;
import com.example.myapplication.util.SelectDataSetContract;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;


/**
 * Activity for displaying trends from multiple data captures.
 */
public class DataGraphViewActivity extends AppCompatActivity
{

    /**
     * DataSets to display is loaded here
     */
    private ArrayList<DataStore.DataSet> loadedDSets = new ArrayList<>();

    /**
     *
     */
    private ActivityResultLauncher<DataGraphViewActivity> selectDataLauncher =
            registerForActivityResult(new SelectDataSetContract(), new selectDataCallBack());


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // disable title bar
        getSupportActionBar().hide();

        // set the main layout
        setContentView(R.layout.activity_datagraphview);

        // set orientation of the device
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ImageButton addDataSetsBtn = findViewById(R.id.GraphViewAddDataSetsBtn);
        addDataSetsBtn.setOnTouchListener(new OnAddDataSetsBtn());
    }


    /**
     * Launch the ActivityForResult for user to select a list of DataSets.
     */
    private void switchToBrowseSavedDataActivity()
    {
        selectDataLauncher.launch(this);
    }

    /**
     * https://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
     * @return
     */
    private int unique_color()
    {
        final double golden_ratio_conjugate = 0.618033988749895;
        double h = Math.random();
        h += golden_ratio_conjugate;
        h %= 1;
        return hsvToRGB(h, 0.5, 0.95);
    }

    /**
     * https://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
     * @param h
     * @param s
     * @param v
     * @return
     */
    private int hsvToRGB(double h, double s, double v)
    {
        int h_i = (int) (h*6);
        double f = h*6 - h_i;
        double p = v * (1 - s);
        double q = v * (1 - f*s);
        double t = v * (1 - (1 - f) * s);

        double r=0,g=0,b=0;
        if (h_i == 0) { r=v; g=t; b=p; }
        if (h_i == 1) { r=q; g=v; b=p; }
        if (h_i == 2) { r=p; g=v; b=t; }
        if (h_i == 3) { r=p; g=q; b=v; }
        if (h_i == 4) { r=t; g=p; b=v; }
        if (h_i == 5) { r=v; g=p; b=q; }

        return Color.rgb((int) (r*256),(int) (g*256),(int) (b*256));
    }

    /**
     * Called after datasets are loaded.
     * Display the loaded datasets.
     */
    private void displayDataSets()
    {
        XYPlot plot = findViewById(R.id.GraphView_Plot);
        plot.clear();

        // get datasets into a 2D array to iterator by column major
        // make the array is normal (all rows have same sizes)
        ArrayList<ArrayList<DataStore.DataSet.DataSetElement>> arr2D = new ArrayList<>();
        ArrayList<DataStore.DataSet> RowSets = new ArrayList<>();    // for names;
        for (DataStore.DataSet ds : loadedDSets)
        {
            RowSets.add(ds);

            Iterator<DataStore.DataSet.DataSetElement> it = ds.getIterator();
            ArrayList<DataStore.DataSet.DataSetElement> row = new ArrayList<>();
            while (it.hasNext())
            {
                row.add(it.next());
            }
            arr2D.add(row);
        }

        // skip first column (skip control series)
        for (int col = 1; col < arr2D.get(0).size(); col++)
        {
            ArrayList<Double> seriesNumbers = new ArrayList<>();
            for (int row = 0; row < arr2D.size(); row++)
            {
                seriesNumbers.add(arr2D.get(row).get(col).getComparativeValue());
            }

            String seriesTitle = String.format("x%d", col);
            XYSeries series = new SimpleXYSeries(
                    seriesNumbers, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, seriesTitle);

            // series formatting
            LineAndPointFormatter seriesFormat = new LineAndPointFormatter(unique_color(), Color.GREEN, null, null);
            seriesFormat.getLinePaint().setPathEffect(new DashPathEffect(new float[]{
                    PixelUtils.dpToPix(20),
                    PixelUtils.dpToPix(15)}, 0));

            plot.addSeries(series, seriesFormat);
        }


//        int count = 1;
//        for (DataStore.DataSet ds : loadedDSets)
//        {
//            ArrayList<Double> seriesNumbers = new ArrayList<>();
//            Iterator<DataStore.DataSet.DataSetElement> it = ds.getIterator();
//            while (it.hasNext())
//            {
//                DataStore.DataSet.DataSetElement elem = it.next();
//                seriesNumbers.add(elem.getComparativeValue());
//            }
//
//            XYSeries series = new SimpleXYSeries(
//                    seriesNumbers, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, String.format("Series %d", count++));
//
//            // series formatting
//            LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.RED, Color.GREEN, null, null);
//            seriesFormat.getLinePaint().setPathEffect(new DashPathEffect(new float[]{
//                    PixelUtils.dpToPix(20),
//                    PixelUtils.dpToPix(15)}, 0));
//
//            plot.addSeries(series, seriesFormat);
//
//            plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
//                @Override
//                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
//                    int i = Math.round(((Number) obj).floatValue());
//                    return toAppendTo.append(domainLabels[i]);
//                }
//                @Override
//                public Object parseObject(String source, ParsePosition pos) {
//                    return null;
//                }
//            });
//        }
    }


    /**
     * Process the data from selectDataLauncher
     * Empty and fill loadedDset with new datasets
     */
    private class selectDataCallBack implements ActivityResultCallback<ArrayList<String>>
    {
        @Override
        public void onActivityResult(ArrayList<String> result)
        {
            loadedDSets.clear();

            for (String uKey : result)
            {
                DataStore.DataSet dSet = DataStore.PrimaryDataStore
                        .loadDSet(uKey, ActivityTransitions.FROM_DATA_GRAPH_VIEW_ACTIVITY);
                loadedDSets.add(dSet);
            }

            displayDataSets();
        }
    }


    /**
     * The user clicked on button to add datasets to display.
     */
    private class OnAddDataSetsBtn implements View.OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
                switchToBrowseSavedDataActivity();
                return true;
            }
            return false;
        }
    }
}

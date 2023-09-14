

package com.bae.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.bae.myapplication.R;
import com.bae.myapplication.datasystem.DataBundlingVisitor;
import com.bae.myapplication.datasystem.DataStore;
import com.bae.myapplication.util.ActivityTransitions;
import com.bae.myapplication.balloons.DataAnalysisBalloons;
import com.bae.myapplication.util.DataCaptureModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Thinh Nguyen
 * Displays the sample results.
 * Attributed from https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
 */
public class DataAnalysisActivity extends AppCompatActivity {
    /**
     * Title for export menu
     */
    private static final String EXPORT_DATA_CHOOSER_TITLE = "Export Results";

    // TextLabels for each sample
    private static final int LISTVIEW_ACTUAL = R.id.resultsListView;
    private static final int LISTVIEW_LAYOUT_ID = R.layout.listview_results_item;
    private static final int LISTVIEW_TEXTVIEW = R.id.sampleLabel;
    private static final int LISTVIEW_SAMPLE_LABEL = R.id.sampleLabel;
    private static final int LISTVIEW_RGB_LABEL = R.id.RGBLabel;
    private static final int LISTVIEW_RPOINT_LABEL = R.id.RPointLabelTitle;
    private static final int LISTVIEW_RPOINTSTD_LABEL = R.id.RPointSTDLabel;
    private static final int LISTVIEW_TRANSFORMEDVALUE_LABEL = R.id.TransformedValueLabel;
    private static final int LISTVIEW_COMPARISONVALUE_LABEL = R.id.ComparativeValueLabel;


    private static final int EXIT_BTN = R.id.discardBtn;
    private static final int EXPORT_DATA_BTN = R.id.exportDataBtn;

    // references to save confirmation panel
    private static final int SAVE_RESULTS_LAYOUT = R.id.SaveResultsFrameConstraintLayout;
    private static final int SAVE_RESULTS_BTN = R.id.saveResultsBtn;
    private static final int SAVE_RESULTS_CANCEL_BTN = R.id.saveResultsCancelBtn;
    private static final int SAVE_RESULTS_CONFIRM_BTN = R.id.saveResultsConfirmBtn;
    private static final int SAVE_RESULTS_FILENANME_EDITTEXT = R.id.editTextFilename;

    private ConstraintLayout saveResultsCL;
    private EditText saveResultsFilenameEditText;

    // Information passed from DataCaptureActivity
    private SampleDataModelAdapter sampleDataModels;

    private DataCaptureModule loadedDataCaptureModule;

    private DataAnalysisBalloons balloons;

    /**
     * Init of the results activity.
     *
     * @param savedInstanceState Previously saved state.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_results);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // variable inits
        // Views for actions
        Button newImageBtn = findViewById(EXIT_BTN);
        Button dataExportBtn = findViewById(EXPORT_DATA_BTN);

        saveResultsCL = findViewById(SAVE_RESULTS_LAYOUT);
        Button saveResultsBtn = findViewById(SAVE_RESULTS_BTN);
        Button saveResultsCancelBtn = findViewById(SAVE_RESULTS_CANCEL_BTN);
        Button saveResultsConfirmBtn = findViewById(SAVE_RESULTS_CONFIRM_BTN);
        saveResultsFilenameEditText = findViewById(SAVE_RESULTS_FILENANME_EDITTEXT);

        // attach event listeners
        newImageBtn.setOnClickListener(new ExitOnClickListener());
        dataExportBtn.setOnClickListener(new ExportOnClickListener());
        saveResultsBtn.setOnClickListener(new SaveResultsMenuOnClickListener());
        saveResultsCancelBtn.setOnClickListener(new SaveResultsCancelOnClickListener());
        saveResultsConfirmBtn.setOnClickListener(new SaveResultsConfirmOnClickListener());

        // load the dataset
        Bundle extras = getIntent().getExtras();
        String uKey = (String) extras.get(DataBundlingVisitor.KEY_EXTRA_STRING);
        ActivityTransitions from = (ActivityTransitions) extras.getSerializable(ActivityTransitions.extraKey);
        loadedDataCaptureModule = (DataCaptureModule) DataStore.primaryDataStore.loadModule(uKey, from);

        // only show save button if from data capture activity
        if (from == ActivityTransitions.FROM_DATA_CAPTURE_ACTIVITY) {
            saveResultsBtn.setVisibility(View.VISIBLE);
        } else {
            saveResultsBtn.setVisibility(View.GONE);
        }

        Log.d("DEBUG", "ResultsActivity onCreate is running.");

        sampleDataModels = new SampleDataModelAdapter(this);

        // create the ListView and ArrayAdapter
        convertData();
        displayData();

        // create balloons for tooltips
        balloons = new DataAnalysisBalloons(this);
        setBalloonListeners();
    }

    /**
     * Set OnClickListeners for tooltip buttons
     */
    private void setBalloonListeners() {
        ImageButton b1 = findViewById(R.id.avgrgbTip);
        ImageButton b2 = findViewById(R.id.avgrgbTip2);
        ImageButton b3 = findViewById(R.id.avgrgbTip3);
        ImageButton b4 = findViewById(R.id.avgrgbTip4);

        b1.setOnClickListener(new avgRGBOnClickListener(b1));
        b2.setOnClickListener(new avgRValOnClickListener(b2));
        b3.setOnClickListener(new avgRValSTDOnClickListener(b3));
        b4.setOnClickListener(new RatioOnClickListener(b4));
    }

    /**
     * Tooltip listener
     */
    private class avgRGBOnClickListener implements View.OnClickListener {
        private ImageButton button;

        public avgRGBOnClickListener(ImageButton button) {
            this.button = button;
        }

        @Override
        public void onClick(View v) {
            balloons.getAvgRGBBalloon().showAlignBottom(button);
        }
    }

    /**
     * Tooltip listener
     */
    private class avgRValOnClickListener implements View.OnClickListener {
        private ImageButton button;

        public avgRValOnClickListener(ImageButton button) {
            this.button = button;
        }

        @Override
        public void onClick(View v) {
            balloons.getAvgRValBalloon().showAlignBottom(button);
        }
    }

    /**
     * Tooltip listener
     */
    private class avgRValSTDOnClickListener implements View.OnClickListener {
        private ImageButton button;

        public avgRValSTDOnClickListener(ImageButton button) {
            this.button = button;
        }

        @Override
        public void onClick(View v) {
            balloons.getAvgRValSTDBalloon().showAlignBottom(button);
        }
    }

    /**
     * Tooltip listener
     */
    private class RatioOnClickListener implements View.OnClickListener {
        private ImageButton button;

        public RatioOnClickListener(ImageButton button) {
            this.button = button;
        }

        @Override
        public void onClick(View v) {
            balloons.getRatioBalloon().showAlignBottom(button);
        }
    }

    /**
     * Transition to MainActivity to restart the process.
     */
    private class ExitOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    /**
     * Convert data from module into the array data model for ListView
     */
    private void convertData() {
        Iterator<DataCaptureModule.Element> dsIterator = loadedDataCaptureModule.getIterator();
        while (dsIterator.hasNext()) {
            sampleDataModels.add(dsIterator.next());
        }
    }

    /**
     * Display data into listview.
     */
    private void displayData() {
        ListView resultsListView = findViewById(LISTVIEW_ACTUAL);
        resultsListView.setAdapter(sampleDataModels);
    }


    private class SampleDataModelAdapter extends ArrayAdapter<DataCaptureModule.Element> {
        /**
         * Constructor
         *
         * @param context Activity context
         */
        public SampleDataModelAdapter(DataAnalysisActivity context) {
            super(context, LISTVIEW_LAYOUT_ID, LISTVIEW_TEXTVIEW);
        }

        /**
         * This method converts the SampleDataModel into a View that can be displayed.
         *
         * @param position    Position of data element (which sample?)
         * @param convertView The view to display
         * @param parent      Parent of the View (ViewGroup)
         * @return View that has been inflated with model information
         */
        @SuppressLint("ResourceType")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            super.getView(position, convertView, parent);

            // inflate the view with the xml template
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(LISTVIEW_LAYOUT_ID, parent, false);
            }

            // this is to set the attributes of the convertView
            DataCaptureModule.Element sampleData = getItem(position);
            TextView sampleLabel = convertView.findViewById(LISTVIEW_SAMPLE_LABEL);
            TextView RGBLabel = convertView.findViewById(LISTVIEW_RGB_LABEL);
            TextView RPointLabel = convertView.findViewById(LISTVIEW_RPOINT_LABEL);
            TextView RPointSTDLabel = convertView.findViewById(LISTVIEW_RPOINTSTD_LABEL);
            TextView transformedValueLabel = convertView.findViewById(LISTVIEW_TRANSFORMEDVALUE_LABEL);
            TextView comparisonValueLabel = convertView.findViewById(LISTVIEW_COMPARISONVALUE_LABEL);


            // Setting the labels
            if (position == 0) {
                sampleLabel.setText("Control");
            } else {
                sampleLabel.setText("x" + position);
            }
            RGBLabel.setText(String.format("(%.0f, %.0f, %.0f)", sampleData.getAvgR(), sampleData.getAvgG(), sampleData.getAvgB()));
            RPointLabel.setText(String.format("%.2f", sampleData.getRPoint()));
            RPointSTDLabel.setText(String.format("%.2E", sampleData.getRPointSTD()));
            transformedValueLabel.setText(String.format("%.2f", sampleData.getTransformedValue()));
            comparisonValueLabel.setText(String.format("%.2f", sampleData.getComparativeValue()));

            return convertView;
        }
    }

    private void saveResultsReset() {
        saveResultsFilenameEditText.setText("");
        saveResultsCL.setVisibility(View.GONE);
    }

    /**
     * Open save results menu
     */
    private class SaveResultsMenuOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //TODO Unfinished. You cannot save
            //Intent openMapIntent = new Intent(DataAnalysisActivity.this, maps.class);
            //startActivity(openMapIntent);
            // And then we open the Save Results (Gonna need to do this later)
            saveResultsCL.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Cancel the act of saving results.
     */
    private class SaveResultsCancelOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            saveResultsReset();
        }
    }

    /**
     *
     */
    private class SaveResultsConfirmOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String inputFileName = saveResultsFilenameEditText.getText().toString();
            DataStore.primaryDataStore.putModule(loadedDataCaptureModule, ActivityTransitions.FROM_DATA_ANALYSIS_ACTIVITY, inputFileName);
            saveResultsReset();
        }
    }

    /**
     *
     */
    private class ExportOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String rawCSV = loadedDataCaptureModule.getCSV();

            try {
                // create a temporary CSV file
                File dir = DataStore.primaryDataStore.getTempDir();
                File csvFile = File.createTempFile("resultsExport", ".csv", dir);
                FileOutputStream OStream = new FileOutputStream(csvFile);
                OStream.write(rawCSV.getBytes());
                Uri csvFileUri = FileProvider.getUriForFile(getApplicationContext(),
                        "com.bae.myapplication.fileprovider",
                        csvFile);

                // send as an intent
                Intent exportDataIntent = new Intent(Intent.ACTION_SEND);
                exportDataIntent.setType("text/csv");
                exportDataIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                exportDataIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                exportDataIntent.putExtra(Intent.EXTRA_STREAM, csvFileUri);
                startActivity(Intent.createChooser(exportDataIntent, EXPORT_DATA_CHOOSER_TITLE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

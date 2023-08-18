package com.bae.myapplication.activities;

import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.bae.myapplication.controller.MenuController;
import com.bae.myapplication.datasystem.DataStore;
import com.bae.myapplication.R;
import com.bae.myapplication.util.ActivityTransitions;

/**
 * @author Thinh Nguyen
 * 5/31/2022
 * <p>
 * Android SDK documentation references:
 * https://developer.android.com/training/camera/photobasics
 * https://developer.android.com/reference/androidx/core/content/FileProvider#getUriForFile(android.content.Context,%20java.lang.String,%20java.io.File)
 */
public class MenuActivity extends AppCompatActivity {

    private static boolean tutorial_mode = false;

    private boolean isTutorialModeOn() {
        return tutorial_mode;
    }

    private void setTutorialMode(boolean b) {
        tutorial_mode = b;
    }

    // since the thread starts in MenuActivity, the view actually creates the controller.
    private MenuController controller;

    /**
     * Initialize Activity by defining variables and attaching UI listeners.
     * Also initializes DataStore if PrimaryDataStore does not exist.
     *
     * @param savedInstanceState saved instances for this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // add listeners to buttons
        findViewById(R.id.menuTakePictureBtn).setOnClickListener(new TakePictureOnClickListener());
        findViewById(R.id.menuLoadPictureBtn).setOnClickListener(new LoadPictureOnClickListener());
        findViewById(R.id.menuSavedDataBtn).setOnClickListener(new SavedDataOnClickListener());
        findViewById(R.id.menuDataGraphViewBtn).setOnClickListener(new GraphDataOnClickListener());
        ((Switch) findViewById(R.id.TutorialSwitch)).setOnCheckedChangeListener(new TutorialOnCheckListener());

        // initiate DataStore if does not exist
        if (DataStore.primaryDataStore == null) {
            DataStore.primaryDataStore = new DataStore(getApplicationContext());
        }

        // initialize tooltipmemory
        if (DataStore.primaryToolTipMemory == null) {
            DataStore.primaryDataStore.loadToolTipMemory();
        }

        controller = new MenuController(this);
    }

    /**
     * Listener for request to take a picture.
     */
    private class TakePictureOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            controller.takePicture();
        }
    }

    /**
     * Listener for request to load a picture from gallery
     */
    private class LoadPictureOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            controller.loadPicture();
        }
    }


    /**
     * Listener for activity switch to SavedDataBrowsing
     */
    private class SavedDataOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent changeActivity = new Intent(MenuActivity.this, SavedDataBrowsingActivity.class);
            changeActivity.putExtra(ActivityTransitions.extraKey, ActivityTransitions.FROM_MENU);
            startActivity(changeActivity);
        }
    }

    /**
     * Listener for activity switch to DataGraphViewActivity
     */
    private class GraphDataOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent changeActivity = new Intent(MenuActivity.this, DataGraphViewActivity.class);
            startActivity(changeActivity);
        }
    }

    /**
     * Start Map Activity
     */
    public void startMapsActivity(View v){
        Intent changeActivity = new Intent(MenuActivity.this, maps.class);
        startActivity(changeActivity);
    }

    /**
     * Listener for tutorial mode switch
     */
    private class TutorialOnCheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            setTutorialMode(b);
        }
    }
}

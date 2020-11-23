package com.toedro.fao.ui.home;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.toedro.fao.App;
import com.toedro.fao.R;
import com.toedro.fao.db.Step;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HomeFragment extends Fragment {

    private TextView stepsCountTextView;
    private TextView kindOfCountTextView;
    private MaterialButton StartStop;

    //TODO change this to get 0 = steps, 1 = CAL; based on settings --> bind value with settings
    public int type = 0;
    static int stepsCompleted = 0;

    // ACC sensors
    private Sensor mSensorACC;
    private SensorManager mSensorManager;
    private SensorEventListener listener;
    // Step Detector sensor
    private Sensor mSensorStepDetector;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        MaterialButton buttonScanBarcode = (MaterialButton) root.findViewById(R.id.buttonScanBarcode);
        buttonScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).navigate(R.id.action_nav_homepage_to_scanBarcodeFragment);
            }
        });

        stepsCountTextView = (TextView) root.findViewById(R.id.stepsCount);
        kindOfCountTextView = (TextView) root.findViewById(R.id.kindOfCount);
        stepsCountTextView.setText(String.valueOf(stepsCompleted)); // ???
        if(type == 0)
            kindOfCountTextView.setText(getResources().getString(R.string.settings_hp_label_steps) + " done today"); //if steps
        else //1
            kindOfCountTextView.setText(getResources().getString(R.string.settings_hp_label_kcal) + " burned today"); //if Cal
        stepsCompleted = App.getDBInstance().stepDAO().getDaySteps(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        //  Get an instance of the sensor manager.
        mSensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorACC = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // Step detector instance
        mSensorStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        listener = new StepCounterListener(stepsCountTextView);

        // Toggle button
        StartStop = (MaterialButton) root.findViewById(R.id.buttonStartStopStepcounter);
        StartStop.setChecked(true); //default stop counter
        StartStop.addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                if (button.getId() == R.id.buttonStartStopStepcounter && !isChecked) {
                    if (mSensorACC != null) {
                        // Register the ACC listener
                        mSensorManager.registerListener(listener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);
                    } else {
                        Toast.makeText(getContext(), R.string.acc_not_available, Toast.LENGTH_SHORT).show();
                    }
                    // Check if the Step detector sensor exists
                    if (mSensorStepDetector != null) {
                        // Register the ACC listener
                        mSensorManager.registerListener(listener, mSensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
                    } else {
                        Toast.makeText(getContext(), R.string.step_not_available, Toast.LENGTH_SHORT).show();
                    }
                    //change text button
                    StartStop.setText(R.string.home_stop_stepcounter);
                } else if (button.getId() == R.id.buttonStartStopStepcounter && isChecked) {
                    // Unregister the listener
                    mSensorManager.unregisterListener(listener);
                    StartStop.setText(R.string.home_start_stepcounter);
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();
        mSensorManager.unregisterListener(listener);
    }

    class StepCounterListener implements SensorEventListener {

        private long lastUpdate = 0;

        public int mACCStepCounter = HomeFragment.stepsCompleted;
        public double ACC_CalCounted = convertCal(mACCStepCounter);

        ArrayList<Integer> mACCSeries = new ArrayList<Integer>();
        ArrayList<String> mTimeSeries = new ArrayList<String>();

        private double accMag = 0d;
        private int lastXPoint = 1;
        int stepThreshold = 10;

        // Android step detector
        public int mAndroidStepCounter = 0;

        TextView stepsCountTextView;

        // Variables for charts
        public String timestamp;
        public String day;
        public String hour;

        // Constructor, get the database, TextView as args
        public StepCounterListener(TextView tv){
            stepsCountTextView = tv;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {

                // Case of the ACC
                case Sensor.TYPE_LINEAR_ACCELERATION:

                    // Get x,y,z
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    //////////////////////////// -- PRINT ACC VALUES -- ////////////////////////////////////
                    // Timestamp
                    long timeInMillis = System.currentTimeMillis() + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;

                    // Convert the timestamp to date
                    SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                    jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                    String date = jdf.format(timeInMillis);

                    // Get the date, the day and the hour
                    timestamp = date;
                    day = date.substring(0,10);
                    hour = date.substring(11,13);

                    ////////////////////////////////////////////////////////////////////////////////////////

                    /// STEP COUNTER ACC ////
                    accMag = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                    //Update the Magnitude series
                    mACCSeries.add((int) accMag);
                    //Update the time series
                    mTimeSeries.add(timestamp);


                    // Calculate ACC peaks and steps
                    peakDetection();

                    break;

                // case Step detector
                case Sensor.TYPE_STEP_DETECTOR:

                    // Calculate the number of steps
                    countSteps(event.values[0]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //
        }


        public void peakDetection() {
            int windowSize = 20;

            /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer, Mladenov et al.
             */
            int highestValX = mACCSeries.size(); // get the length of the series
            if (highestValX - lastXPoint < windowSize) { // if the segment is smaller than the processing window skip it
                return;
            }

            List<Integer> valuesInWindow = mACCSeries.subList(lastXPoint,highestValX);
            List<String> timesInWindow = mTimeSeries.subList(lastXPoint,highestValX);

            lastXPoint = highestValX;

            int forwardSlope = 0;
            int downwardSlope = 0;

            List<Integer> dataPointList = new ArrayList<Integer>();
            List<String> timePointList = new ArrayList<String>();


            for (int p =0; p < valuesInWindow.size(); p++){
                dataPointList.add(valuesInWindow.get(p)); // ACC Magnitude data points
                timePointList.add(timesInWindow.get(p)); // Timestamps
            }

            for (int i = 0; i < dataPointList.size(); i++) {
                if (i == 0) {
                }
                else if (i < dataPointList.size() - 1) {
                    forwardSlope = dataPointList.get(i + 1) - dataPointList.get(i);
                    downwardSlope = dataPointList.get(i)- dataPointList.get(i - 1);

                    if (forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i) > stepThreshold ) {

                        // Update the number of steps
                        mACCStepCounter += 1;
                        Log.d("ACC STEPS: ", String.valueOf(mACCStepCounter));

                        // Update the TextView
                        if(type == 0) {
                            stepsCountTextView.setText(String.valueOf(mACCStepCounter));
                            kindOfCountTextView.setText(getResources().getString(R.string.settings_hp_label_steps) + " done today"); //if steps
                        }else { //1
                            stepsCountTextView.setText(String.valueOf(ACC_CalCounted));
                            kindOfCountTextView.setText(getResources().getString(R.string.settings_hp_label_kcal) + " burned today"); //if Cal
                        }
                        // Insert the data in the DB
                        App.getDBInstance().stepDAO().addStep(new Step(day, hour, timePointList.get(i)));
                    }

                }
            }
        }
        //TODO delete this and replace everything from database
        private double convertCal(int steps) {
            //TODO get correct equation
            return steps*0.5;
        }


        // Calculate the number of steps from the step detector
        private void countSteps(float step) {

            //Step count
            mAndroidStepCounter += (int) step;
            Log.d("NUM STEPS ANDROID", "Num.steps: " + String.valueOf(mAndroidStepCounter));
        }
    }
}
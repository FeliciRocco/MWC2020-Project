package com.toedro.fao.ui.home;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.toedro.fao.App;
import com.toedro.fao.R;
import com.toedro.fao.stepcounter.StepCounterListener;
import com.toedro.fao.ui.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private MaterialButton buttonStartStop;

    MaterialButtonToggleGroup materialButtonToggleGroup;

    // ACC sensors
    private Sensor mSensorACC;
    // Step Detector sensor
    private Sensor mSensorStepDetector;
    // Listener and SensorManager
    private SensorManager mSensorManager;
    private StepCounterListener listener;

    private HomeViewPagerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        //button to add ingredients
        MaterialButton buttonAddIngredient = (MaterialButton) root.findViewById(R.id.buttonAddIngredient);
        buttonAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).navigate(R.id.action_nav_homepage_to_ingredientsFragment);
            }
        });
        // Button to go scanning barcode
        MaterialButton buttonScanBarcode = (MaterialButton) root.findViewById(R.id.buttonScanBarcode);
        buttonScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).navigate(R.id.action_nav_homepage_to_scanBarcodeFragment);
            }
        });

        // Initial data of the view pager
        int stepsCompleted = App.getDBInstance().stepDAO().getDaySteps(
                new SimpleDateFormat(getString(R.string.date_layout_DB))
                        .format(new Date()));
        List data = new ArrayList<HomePagerData>();
        data.add(new HomePagerData(R.drawable.ic_calories,
                String.valueOf(Utils.convertStepsToCal(stepsCompleted, getActivity(), getContext())),
                getString(R.string.home_calories_description)));
        data.add(new HomePagerData(R.drawable.ic_steps,
                String.valueOf(stepsCompleted),
                getString(R.string.home_steps_description)));
        // View Pager to show steps and calories
        ViewPager2 viewPager = root.findViewById(R.id.homeViewPager);
        adapter = new HomeViewPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        adapter.setData(data);

        TabLayout progressPager = root.findViewById(R.id.progressViewPager);
        new TabLayoutMediator(progressPager, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

            }
        }).attach();

        // Button to get a recipe
        MaterialButton buttonWannaEat = (MaterialButton) root.findViewById(R.id.buttonWannaEat);
        buttonWannaEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Navigation.findNavController(v).navigate(R.id.action_nav_homepage_to_wannaEatFragment);

                /*
                App.getDBInstance().caloriesDao().addCalories(new Calories(170.0, new SimpleDateFormat(getString(R.string.date_layout_DB))
                        .format(new Date()), String.valueOf(Calendar.getInstance().getTimeInMillis() - 5000)));

                App.getDBInstance().caloriesDao().addCalories(new Calories(750.0, new SimpleDateFormat(getString(R.string.date_layout_DB))
                        .format(new Date()), String.valueOf(Calendar.getInstance().getTimeInMillis())));


                 */
                /*
                //Log.d("CAL", App.getDBInstance().recipeDAO().getCalories("9EiSp5POUJRXLuESvAtD").toString());
                List<Recipe> recipeList = App.getDBInstance().recipeDAO().getRecipes();
                double BMR = Utils.calculateBMR(getActivity(), getContext());
                //Calendar rightNow = Calendar.getInstance();
                Calendar c = Calendar.getInstance();
                long now = c.getTimeInMillis();
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                //end todo
//              long lastMeal = App.getDBInstance().CaloriesDao().getLastMeal(); //todo if this return null --> use calendar
                //or save data on app installation with id = value = 0

                long passed = now - c.getTimeInMillis();
                //long passed = now - lastMeal; //TODO test
                long secondsPassed = passed / 1000;
                Log.d("BMR", String.valueOf(BMR));
                double calories = Utils.convertStepsToCal(App.getDBInstance().stepDAO().getDaySteps(
                        new SimpleDateFormat(getString(R.string.date_layout_DB))
                                .format(new Date())), getActivity(), getContext()) + (BMR * (double)secondsPassed/(double)(24*3600));
                Log.d("BMR", String.valueOf(((double)(secondsPassed)/(double)(24*3600))));
                App.getDBInstance().CaloriesDao().addCalories(new Calories(calories, now));
                //get min distance
                double distances[] = new double[recipeList.size()];
                //for(Recipe recipe: recipeList) { //iterate with iterator
                for (int i = 0; i < recipeList.size(); i++) {
                    if(App.getDBInstance().recipeDAO().getCalories(recipeList.get(i).getId()) != null)
                        distances[i] = Math.abs(calories - App.getDBInstance().recipeDAO().getCalories(recipeList.get(i).getId()));
                    Log.d("Distances/CALories", String.valueOf(calories).toString() + ' ' +String.valueOf(
                            App.getDBInstance().recipeDAO().getCalories(recipeList.get(i).getId())).toString()
                            +' ' +String.valueOf(distances[i]).toString());
                }
                //minimum
                int minPos = 0;
                double minValue = distances[0];
                for (int i = 1; i < distances.length; i++) {
                    if (distances[i] < minValue) {
                        minValue = distances[i];
                        minPos = i;
                    }
                }
                Toast.makeText(getContext(), "Best suited recipe = " + recipeList.get(minPos).getName(), Toast.LENGTH_LONG).show();

                 */
            }
        });

        buttonStartStop     = (MaterialButton) root.findViewById(R.id.buttonStartStopStepcounter);

        //  Get an instance of the sensor manager
        mSensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        // Get an instance of Accelerometer
        mSensorACC = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // Get an instance of Step Detector
        mSensorStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // Get listener instance, and check if it is already activated
        listener = StepCounterListener.getInstance(stepsCompleted, adapter, getContext(), getActivity());
        if(listener.isActive()) {
            buttonStartStop.setChecked(true);
            buttonStartStop.setText(R.string.home_stop_stepcounter);
            buttonStartStop.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_stop));
        }

        buttonStartStop.addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialButton button, boolean isChecked) {
                if (isChecked) {
                    // Check if the Step detector sensor exists
                    if (mSensorStepDetector != null) {
                        // Register the Step Detector listener
                        listener.activate();
                        mSensorManager.registerListener(listener, mSensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
                    } else if (mSensorACC != null) {
                        // Register the ACC listener
                        listener.activate();
                        mSensorManager.registerListener(listener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);
                    } else {
                        Snackbar.make(getView(), R.string.home_sensors_not_available, Snackbar.LENGTH_LONG).show();
                        button.setChecked(false);
                        return;
                    }
                    // Change button text and icon
                    buttonStartStop.setText(R.string.home_stop_stepcounter);
                    buttonStartStop.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_stop));
                } else {
                    // Unregister the listener
                    listener.deactivate();
                    mSensorManager.unregisterListener(listener);
                    // Change button text and icon
                    buttonStartStop.setText(R.string.home_start_stepcounter);
                    buttonStartStop.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));
                }
            }
        });

        return root;
    }
}

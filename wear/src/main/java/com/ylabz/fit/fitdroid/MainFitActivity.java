package com.ylabz.fit.fitdroid;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainFitActivity extends WearableActivity implements MessageApi.MessageListener, SensorEventListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    private static final String VOICE_TRANSCRIPTION_MESSAGE_PATH = "path";

    private BoxInsetLayout mContainerView;
    //private TextView mTextView;
    private TextView mClockView;

    private static final String TAG = "MainActivity";
    private TextView mTextViewStepCount;
    private TextView mTextViewStepCalories;
    private TextView mTextViewHeart;
    private ImageView mDroid;
    private ImageView mHeart;
    private  // beat the heart
    final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fit);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        //mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

        mTextViewStepCount = (TextView) findViewById(R.id.step);
        mTextViewStepCalories =(TextView) findViewById(R.id.stepC);
        mTextViewHeart  =(TextView) findViewById(R.id.heart);
        mDroid = (ImageView)findViewById(R.id.imageAndroid);
        mHeart = (ImageView)findViewById(R.id.imageHeart);
        mDroid.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);



        // beat the heart
        // final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        mHeart.startAnimation(animation);



    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorManager mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Sensor mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // Calories missing from my wear device.
        // Sensor mStepDetectCal = mSensorManager.getDefaultSensor(Sensor.TYPE_CAL);

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sen:sensors){
            String TAG = "tag";
            Log.i(TAG, sen + "");
        }

        mDroid.setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_ATOP);
        mTextViewStepCount.setTextColor(Color.RED);
        //Log.i(TAG, mHeartRateSensor + " heartGood");
        //Log.i(TAG, mStepCountSensor + " step");
        //Log.i(TAG, mStepDetectCal + " calories");

        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void changeColor(int num_steps) {
        //164, 198, 57
        mDroid.setColorFilter(Color.rgb(164, 198, 57), PorterDuff.Mode.SRC_ATOP);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(VOICE_TRANSCRIPTION_MESSAGE_PATH)) {
            Intent startIntent = new Intent(this, MainFitActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startIntent.putExtra("VOICE_DATA", messageEvent.getData());
            startActivity(startIntent);
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private String currentTimeStr() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(c.getTime());
    }

    private void updateDisplay() {
       if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
            mTextViewStepCount.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextViewStepCount.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            mClockView.setVisibility(View.GONE);
        }
    }





    @Override
    public void onSensorChanged(SensorEvent event) {


        String msgT = "" + (int)event.values[0];

        String TAG = "tag";
        Log.i(TAG, "--------------------------");
        Log.i(TAG, msgT);
        Log.i(TAG, ""+ event.sensor.getType());
        Log.i("live","--------------");




        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            int h_rate = (int)event.values[0];
            animation.setDuration(h_rate / 60 * 1000); //beats per second
            String msg = "heart: " + h_rate;
            mTextViewHeart.setText(msg);
            Log.d(TAG, msg);
        }
        else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int steps = (int)event.values[0];
            String msg = "Count: " + steps;
            mTextViewStepCount.setText(msg);
            mTextViewStepCount.setTextColor(Color.rgb(0, 153, 51));
            changeColor(steps);


            Log.d(TAG, msg);
        }
        else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            String msg = "Detected at " + currentTimeStr();
            //mTextViewStepCal.setText(msg);
            Log.d(TAG, msg);
        }
        else
            Log.d(TAG, "Unknown sensor type");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
    }
}

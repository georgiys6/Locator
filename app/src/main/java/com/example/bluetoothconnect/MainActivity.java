package com.example.bluetoothconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;


    TextView tv;
    TextView tvLocationGPS;
    TextView tvLocationNet;
    TextView tvTitleGPS;
    TextView tvTitleNet;
    TextView tvSquare;
    TextView tvRangeTime;
    TextView tvStep;
    Button start;
    Button stop;

    TextView textSG;
    TextView textHG;
    TextView textFG;
    TextView textFN;
    TextView textSN;
    TextView textHN;

    Location netLocationStart;
    Location netLocationEnd;
    Location gpsLocationStart;
    Location gpsLocationEnd;

    float distanceNet = 0;
    float distanceGPS = 0;

    Timer myTimer;

    Date dateGPSStart;
    Date dateGPSEnd;
    Date dateNetStart;
    Date dateNetEnd;
    int i = 0;
    boolean flag;
    boolean mode = false;
    final Handler myHandler = new Handler();
    private double мagnitudePrevious = 0;
    double hG;
    double fG;
    double hN;
    double fN;
    int counter = 0;
    private Integer stepCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        tv = (TextView) findViewById(R.id.gps);
        tvLocationGPS = (TextView) findViewById(R.id.coordination);
        tvLocationNet = (TextView) findViewById(R.id.coordinationNet);
        tvTitleGPS = (TextView) findViewById(R.id.gps);
        tvTitleNet = (TextView) findViewById(R.id.net);
        tvSquare = (TextView) findViewById(R.id.square);
        tvRangeTime = (TextView) findViewById(R.id.rangeTime);
        tvStep = (TextView) findViewById(R.id.step);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        textHG = findViewById(R.id.hG);
        textFG = findViewById(R.id.fG);
        textSG = findViewById(R.id.sG);
        textHN = findViewById(R.id.hN);
        textFN = findViewById(R.id.fN);
        textSN = findViewById(R.id.sN);

        start = (Button) findViewById(R.id.buttonStart);
        stop = (Button) findViewById(R.id.buttonStop);


    }

    private void UpdateGUI() {
        i++;
        myHandler.post(myRunnable);
    }


    final Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            tv.setText(String.valueOf(i));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        myTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showInfo();
                    }
                });
            }
        };

        myTimer.schedule(task, 1, 500);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                0, locationListener);


        sensorManager.registerListener(listener,stepCounterSensor,SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            checkEnabled();
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }
    };


        int stepsint = 0;
        int stepsToAvg = 15;
        float[] valuesAccel = new float[3];
        float[] valuesAccelMass = new float[3 * stepsToAvg];
        float[] valuesAccelAvg = new float[3 * stepsToAvg];
        int j = 0;
        SensorEventListener listener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (mode == true) {
                    switch (event.sensor.getType()) {
                        case Sensor.TYPE_ACCELEROMETER:
                            for (int i = 0; i < 3; i++) {
                                valuesAccel[i] = event.values[i];
                                valuesAccelMass[j] = valuesAccel[i];
                                j += 1;
                                if (j == (stepsToAvg * 3)) {
                                    j = 0;
                                }
                            }
                            break;
                    }
                }
            }

    };

    private void showLocation(Location location) { //maybe private
        if (flag) {
            if (location == null) {
                return;
            }
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                tvLocationGPS.setText(formatLocation(location));
            }

            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                tvLocationNet.setText(formatLocation(location));
            }
        }
    }



    private String formatLocation(Location location) {
        if (location == null) {
            return "";
        }
        return String.format("Широта = %1$.4f, долгота = %2$.4f",
                location.getLatitude(), location.getLongitude());

    }

    private void checkEnabled() {  //maybe private
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            tvTitleGPS.setText("GPS: " + getString(R.string.enabled));
         else
            tvTitleGPS.setText("GPS: " + getString(R.string.disabled));

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            tvTitleNet.setText("Net: " + getString(R.string.enabled));
        else
            tvTitleNet.setText("Net: " + getString(R.string.disabled));


    }
    


    public void onStartClick(View view) {
        counter++;
        stepCount = 0;
        mode = true;
        flag = true;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            netLocationStart = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
            showLocation(netLocationStart);
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsLocationStart = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
            showLocation(gpsLocationStart);
        }

        start.setEnabled(false);
        stop.setEnabled(true);

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                UpdateGUI();
            }
        }, 0, 1000);


    }

    public void onStopClick(View view) {
        mode = false;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            netLocationEnd = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            showLocation(netLocationEnd);
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsLocationEnd = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            showLocation(gpsLocationEnd);
        }

        stop.setEnabled(false);
        start.setEnabled(true);
        flag = false;

        myTimer.cancel();
        i = 0;
        try {
            long seconds = getNetTime();
            distanceNet = getNetDistance();
            tvSquare.setText(distanceNet + " м, " + seconds + " с");



            seconds = getGPSTime();
            distanceGPS = getGPSDistance();
            tvRangeTime.setText(distanceGPS + " м, " + seconds + " с");
            calc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showInfo() {
        valuesAccelAvg = calculateAvg(valuesAccelMass);
        stepsint += calculateSteps(valuesAccelAvg, valuesAccel);
        tvStep.setText("Шагов: " + stepsint);
    }

    public int calculateSteps(float[] valuesAccelAvg, float[] valuesAccel){
        float srav = (float) 2.8;
        float sqrt = (float) Math.sqrt(Math.pow(valuesAccel[0] - valuesAccelAvg[0], 2) + Math.pow(valuesAccel[1] - valuesAccelAvg[1], 2) + Math.pow(valuesAccel[2] - valuesAccelAvg[2], 2));
        if(sqrt > srav){
            return 1;
        }
        return 0;
    }

    public float[] calculateAvg(float[] valuesAccelMass){
        float avgx = 0;
        float avgy = 0;
        float avgz = 0;
        for (int i = 0; i < (stepsToAvg * 3); i+=3){
            avgx += valuesAccelMass[i];
            avgy += valuesAccelMass[i+1];
            avgz += valuesAccelMass[i+2];
        }
        float[] mass = new float[3];
        mass[0] = avgx / stepsToAvg;
        mass[1] = avgy / stepsToAvg;
        mass[2] = avgz / stepsToAvg;
        return mass;
    }

    void calc (){
        if(counter == 1){
            hG = distanceGPS;
            hN = distanceNet;
            textHG.setText(String.format(Locale.getDefault(), "%1$.4f", hG));
            textHN.setText(String.format(Locale.getDefault(), "%1$.4f", hN));

        } else {
            counter = 0;

            fG = distanceGPS;
            fN = distanceNet;
            double sG = hG/2 * fG/2 * Math.PI;
            double sN = hN/2 * fN/2 * Math.PI;

            textFG.setText(String.format(Locale.getDefault(), "%1$.4f", fG));
            textFN.setText(String.format(Locale.getDefault(), "%1$.4f", fN));
            textSG.setText(String.format(Locale.getDefault(), "%1$.4f", sG));
            textSN.setText(String.format(Locale.getDefault(), "%1$.4f", sN));
        }
    }


    private float getGPSDistance(){
        return gpsLocationStart.distanceTo(gpsLocationEnd);
    }
    private float getNetDistance(){
        return netLocationStart.distanceTo(netLocationEnd);
    }
    private long getGPSTime(){
        return TimeUnit.MILLISECONDS.toSeconds(gpsLocationEnd.getTime() - gpsLocationStart.getTime());
    }
    private long getNetTime(){
        return TimeUnit.MILLISECONDS.toSeconds(netLocationEnd.getTime() - netLocationStart.getTime());
    }


}

package mcteam08.assign.assign02;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;

public class PositionRecordService extends Service implements LocationListener {
    final private static String TAG = PositionRecordService.class.getCanonicalName();
    final private static String FILENAME = "targetLocationRecord.gpx";
    final private static long MIN_TIME = 5000; // 5 seconds
    final private static int MIN_TIME_ACC = 5000000; // 5 seconds
    final private static long MIN_DISTANCE = 1; // 1 meter
    final private static double EARTH_RADIUS = 6371004; //meter
    final private static double DEGREE_TO_RAD_FACTOR = Math.PI / 180.0;

    private LocalTime startTime;
    private LocalTime currentTime;
    private LocalTime endTime;
    private long elaspedTime;
    private double distance;
    private double avgSpeed; // meter per second

    private PositionRecordServiceImpl impl;
    private double[] currentLocation = new double[2];
    private double[] startLocation = new double[2];
    boolean isFristLocation;
    LocationManager locationManager;
    private File output;

    // default constructor
    public PositionRecordService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service created");
        super.onCreate();
        impl = new PositionRecordServiceImpl();
        elaspedTime = 0;
        distance = 0;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check whether GPS is enabled
        if (!gpsEnabled) {
            // App bind to this service should enable this GPS
            Log.i(TAG, "GPS not enabled! Please enable it!");
        } else {
            if ((Build.VERSION.SDK_INT >= 23) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED)) {
                // App bind to this service should set the permission
                Log.i(TAG, "GPS permission not granted! Please authorize it!");
            } else {
                Log.i(TAG, "GPS permission granted!");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE,this);
            }
        }

        // TODO: step-1 crate GPX file with prefix

        // create an output file
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File outputFile = new File(path.getCanonicalPath(), FILENAME);

        }  catch (IOException e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        locationManager.removeUpdates(this);
        // TODO: step-3 add post-fix to GPX and finish file.
        super.onDestroy();

    }

    private class PositionRecordServiceImpl extends IPositionRecordService.Stub {

        @Override
        public double getLongitude() throws RemoteException {
            return currentLocation[0];
        }

        @Override
        public double getLatitude() throws RemoteException {
            return currentLocation[1];
        }

        /** Distance and average speed arr only updated when activity
         * performs PRC call;
         * */
        @Override
        public double getAverageSpeed() throws RemoteException {
            distance = distanceCalculation(startLocation, currentLocation);
            elaspedTime = elapsedTimeCalculation(startTime, currentTime);
            avgSpeed = averageSpeedCalculation(distance, elaspedTime);
            return avgSpeed;
        }

        @Override
        public double getDistance() throws RemoteException {
            distance = distanceCalculation(startLocation, currentLocation);
            return distance;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service bound");

        // record the start time
        initState();
        startTime =  LocalTime.now();
        currentTime = startTime;
        return impl;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Service unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (isFristLocation) {
            startLocation[0] = location.getLongitude();
            startLocation[1] = location.getLatitude();
            isFristLocation = false;
        }

        currentLocation[0] = location.getLongitude();
        currentLocation[1] = location.getLatitude();

        writeToGPXFile(currentLocation[0], currentLocation[1]);
        // update current Time and calculated elasped time in second
        currentTime = LocalTime.now();

        // TODO: step-2 add treck to GPX
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // Formula reference: https://www.cnblogs.com/ycsfwhh/archive/2010/12/20/1911232.html
    private double distanceCalculation(double[] locationStart, double[] locationCurrent) {
        // DONE: update current location
        double lon1 = locationStart[0] * DEGREE_TO_RAD_FACTOR;
        double lat1 = (90 - locationStart[1]) * DEGREE_TO_RAD_FACTOR;
        double lon2 = locationCurrent[0] * DEGREE_TO_RAD_FACTOR;
        double lat2 = (90 - locationCurrent[1]) * DEGREE_TO_RAD_FACTOR;
        return EARTH_RADIUS * Math.acos(Math.sin(lat1) * Math.sin(lat2) * Math.cos(lon1 - lon2) + Math.cos(lat1) * Math.cos(lat2));
    }

    private double averageSpeedCalculation(double traveledDistance, long timeInSeconds) {
        return traveledDistance / timeInSeconds;
    }


    private void writeToGPXFile(double longitude, double latitude) {
        // TODO: finish this writer
    }

    private int elapsedTimeCalculation(LocalTime start, LocalTime end) {
        int startTimeInSecond = timeInSecond(start);
        int endTimeInSecond = timeInSecond(end);

        return (endTimeInSecond - startTimeInSecond);
    }

    private int timeInSecond(LocalTime time) {
        return time.getSecond() + time.getMinute() * 60 + time.getHour() * 60 * 60;
    }

    /** reset time, distance speed*/
    private void initState() {
        startTime = LocalTime.now();
        currentTime = startTime;
        endTime = startTime;
        distance = 0;
        elaspedTime = 0;
        avgSpeed = 0;
        isFristLocation = true;
    }
}

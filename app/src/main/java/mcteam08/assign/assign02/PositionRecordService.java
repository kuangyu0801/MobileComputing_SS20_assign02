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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class PositionRecordService extends Service implements LocationListener {
    final private static String TAG = PositionRecordService.class.getCanonicalName();
    final private static long MIN_TIME = 5000; // 5 seconds
    final private static int MIN_TIME_ACC = 5000000; // 5 seconds
    final private static long MIN_DISTANCE = 1; // 1 meter

    private PositionRecordServiceImpl impl;
    double[] dataLocation = new double[2];
    LocationManager locationManager;

    // default constructor
    public PositionRecordService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service created");
        super.onCreate();
        impl = new PositionRecordServiceImpl();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

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
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        locationManager.removeUpdates(this);
        super.onDestroy();

    }

    private class PositionRecordServiceImpl extends IPositionRecordService.Stub {

        @Override
        public double getLongitude() throws RemoteException {
            return dataLocation[0];
        }

        @Override
        public double getLatitude() throws RemoteException {
            return dataLocation[1];
        }

        @Override
        public double getAverageSpeed() throws RemoteException {
            return 0;
        }

        @Override
        public double getDistance() throws RemoteException {
            return 0;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service bound");
        return impl;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Service unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        dataLocation[0] = location.getLongitude();
        dataLocation[1] = location.getLatitude();
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

    private double distanceCalculation() {
        double toRad = Math.PI / 180.0;
        double lon1 = dataLocation[0] * toRad;
        double lat1 = (90 - dataLocation[1]) * toRad;
        // TODO: update current location
        double lon2 = 0;
        double lat2 = 0;

        final double EARTH_RADIUS = 6371004; //meter
        return EARTH_RADIUS * Math.acos(Math.sin(lat1) * Math.sin(lat2) * Math.cos(lon1-lon2) + Math.cos(lat1) * Math.cos(lat2));
    }

    private double avgspeedCalculation() {
        // TODO: add timer
        return 0;
    }

    private void writeToGpxFile() {
        // TODO: finish this timer

    }
}

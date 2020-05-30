package mcteam08.assign.assign02;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    final private static String TAG = MainActivity.class.getCanonicalName();
    final private static int MY_PERMISSION_REQUEST = 1; // request
    final private static String[] MY_ALL_REQUIRED_PERMSSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    final private static String ACTION_SERVICE_ACTIVE =  BuildConfig.APPLICATION_ID + "SERVICE_ACTIVE";
    final private static String ACTION_SERVICE_INACTIVE =  BuildConfig.APPLICATION_ID + "SERVICE_INACTIVE";

    private Button bStart, bStop, bUpdate;
    private TextView tvLongitude, tvLatitude, tvDistance, tvAvgSpeed;
    private IPositionRecordService serviceProxy;
    private Intent i0;
    private IntentFilter iFilterActive, iFilterInactive;
    private ServiceBroadcastReceiver sReceiver = new ServiceBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Activity created");

        setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            enableLocationSettings();

        // DONE: permission is not granted!
        checkAllPermissions();
        checkExternalMounted();
        checkExternalPermission();
        checkExternalStorageReadable();
        checkExternalStorageWritable();

        tvLongitude = findViewById(R.id.textViewLon);
        tvLatitude = findViewById(R.id.textViewLat);
        tvDistance = findViewById(R.id.textViewDistance);
        tvAvgSpeed = findViewById(R.id.textViewAvgSpeed);
        bStart = findViewById(R.id.button_start);
        bStop = findViewById(R.id.button_stop);
        bUpdate = findViewById(R.id.button_update);

        // service intent
        i0 = new Intent(this, PositionRecordService.class);

        // register the receiver on creation
        iFilterActive = new IntentFilter(ACTION_SERVICE_ACTIVE);
        iFilterInactive = new IntentFilter(ACTION_SERVICE_INACTIVE);
        LocalBroadcastManager.getInstance(this).registerReceiver(sReceiver, iFilterActive);
        LocalBroadcastManager.getInstance(this).registerReceiver(sReceiver, iFilterInactive);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "Activity started");
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Start Service clicked");
                startService(i0);
                bindService();
                // DONE: how to use bindService(i0, this, BIND_AUTO_CREATE); inside this class
                // keyword "this" would refer to class OnClickListener not ServiceConnection
                // 这个问题我研究过，没找着方法。如果只单纯用startService呢
                // we can use MainActivity.this to indicated the activity
                // bindService(i0, MainActivity.this, BIND_AUTO_CREATE);
            }
        });

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Stop Service clicked");
                unbindService();
                stopService(i0);
            }
        });

        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double avgSpeed = serviceProxy.getAverageSpeed();
                    Double distance = serviceProxy.getDistance();
                    Double latitude = serviceProxy.getLatitude();
                    Double longitude = serviceProxy.getLongitude();

                    tvLongitude.setText(longitude.toString());
                    tvLatitude.setText(latitude.toString());
                    tvDistance.setText(distance.toString());
                    tvAvgSpeed.setText(avgSpeed.toString());

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "Activity destroyed");
        unbindService(this);
        // unregister receiver on destroy
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sReceiver);
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "Service connected");
        serviceProxy = IPositionRecordService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "Service disconnected");
        serviceProxy = null;
    }

    private void enableLocationSettings() {
        new AlertDialog.Builder(this)
                .setTitle("Enable GPS")
                .setMessage("GPS currently disabled. Do you want to enable GPS?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(settingsIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void bindService() {
        Log.i(TAG, "Bind to Service");
        bindService(i0, this, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        Log.i(TAG, "Unbind from Service");
        unbindService(this);
    }

    private boolean checkExternalPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {

            Log.i(TAG, "READ permission denied");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "WRTIE permission denied");
            return false;
        }
        Log.i(TAG, "permission granted");
        return true;
    }

    private boolean checkExternalMounted() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.i(TAG, "SD card mounted");
            return true;
        }
        Log.i(TAG, "SD card not mounted");
        return false;
    }

    public boolean checkExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.i(TAG, "External storage writable");
            return true;
        }
        Log.i(TAG, "External storage not writable");
        return false;
    }

    /** Checks if external storage is available to at least read */
    public boolean checkExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.i(TAG, "External storage readable");
            return true;
        }
        Log.i(TAG, "External storage not readable");
        return false;
    }

    private boolean checkAllSelfPermissions() {
        boolean isAllPermissionGranted = true;
        for (int i = 0; i < MY_ALL_REQUIRED_PERMSSIONS.length; i +=1) {
            if (ActivityCompat.checkSelfPermission(this, MY_ALL_REQUIRED_PERMSSIONS[i])
                    == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permisson: " + MY_ALL_REQUIRED_PERMSSIONS[i] + "granted!");
            } else {
                Log.i(TAG, "Permisson: " + MY_ALL_REQUIRED_PERMSSIONS[i] + "not granted!");
                isAllPermissionGranted = false;
            }
        }
        return isAllPermissionGranted;
    }

    /**
     * The method is used to check all permissions set at once
     * */
    private void checkAllPermissions() {
        if (checkAllSelfPermissions() && Build.VERSION.SDK_INT >= 23) {
            Toast.makeText(MainActivity.this,
                    "You have already been granted all permission!",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestAllRPermissions();
        }
    }
    private boolean shouldShowAllRequestPermissionRationale() {
        boolean shouldShownAllRationale = true;

        for (int i = 0; i < MY_ALL_REQUIRED_PERMSSIONS.length; i +=1) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, MY_ALL_REQUIRED_PERMSSIONS[i])) {
                shouldShownAllRationale = false;
            }
        }
        return shouldShownAllRationale;
    }

    /**
     * The method is used to acquire permission set at once
     * */
    private void requestAllRPermissions() {
        // Permission has not been granted and must be requested.
        if (shouldShowAllRequestPermissionRationale()) {
            // create a dialog notify the use why permission is needed
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("These permissions are needed for GPS and Read/Write File")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    MY_ALL_REQUIRED_PERMSSIONS, MY_PERMISSION_REQUEST);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, MY_ALL_REQUIRED_PERMSSIONS, MY_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

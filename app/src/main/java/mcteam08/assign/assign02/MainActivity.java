package mcteam08.assign.assign02;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    final private static String TAG = MainActivity.class.getCanonicalName();
    final private static int MY_PERMISSION_REQUEST_FINE_LOCATION = 1; // request

    private Button bStart, bStop, bUpdate;
    private TextView tvLongitude, tvLatitude, tvDistance, tvAvgSpeed;
    private IPositionRecordService serviceProxy;
    private Intent i0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Activity created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            enableLocationSettings();
        requestLocationPermission();

        tvLongitude = findViewById(R.id.textViewLon);
        tvLatitude = findViewById(R.id.textViewLat);
        tvDistance = findViewById(R.id.textViewDistance);
        tvAvgSpeed = findViewById(R.id.textViewAvgSpeed);
        bStart = findViewById(R.id.button_start);
        bStop = findViewById(R.id.button_stop);
        bUpdate = findViewById(R.id.button_update);

    }

    @Override
    protected void onStart() {
        Log.i(TAG, "Activity started");
        super.onStart();
        i0 = new Intent(this, PositionRecordService.class);
        startService(i0);
        bindService(i0, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "Activity destroyed");
        unbindService(this);
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

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSION_REQUEST_FINE_LOCATION);
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
}

package mcteam08.assign.assign02;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

public class ServiceBroadcastReceiver extends BroadcastReceiver {
    final private static String TAG = ServiceBroadcastReceiver.class.getCanonicalName();
    final private static String ACTION_SERVICE_ACTIVE =  BuildConfig.APPLICATION_ID + "SERVICE_ACTIVE";
    final private static String ACTION_SERVICE_INACTIVE =  BuildConfig.APPLICATION_ID + "SERVICE_INACTIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Broadcast intent received");
        String intentAction = intent.getAction();
        if (intentAction.equals(ACTION_SERVICE_ACTIVE)) {
            Toast.makeText(context, "Service is now active", Toast.LENGTH_SHORT).show();
        } else if (intentAction.equals(ACTION_SERVICE_INACTIVE)){
            Toast.makeText(context, "Service is now inactive", Toast.LENGTH_SHORT).show();
        }
    }
}

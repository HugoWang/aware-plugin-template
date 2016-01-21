package com.aware.plugin.template;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {
    private String ACTION_AWARE_LOCATION_TYPE_INDOOR = "ACTION_AWARE_LOCATION_TYPE_INDOOR";
    private String ACTION_AWARE_LOCATION_TYPE_OUTDOOR = "ACTION_AWARE_LOCATION_TYPE_OUTDOOR";
    private String EXTRA_ELAPSED = "elapsed_time";
    private double indoor_elapsed = 0; //indoor time counter
    private double outdoor_elapsed = 0; //outdoor time counter
    @Override
    public void onCreate() {
        super.onCreate();
        if( DEBUG ) Log.d(TAG, "Template plugin running");

        //Initialize our plugin's settings
        if( Aware.getSetting(this, Settings.STATUS_PLUGIN_TEMPLATE).length() == 0 ) {
            Aware.setSetting(this, Settings.STATUS_PLUGIN_TEMPLATE, true);
        }

        //Activate any sensors/plugins you need here
        Aware.setSetting(this, Aware_Preferences.STATUS_SCREEN, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_LIGHT, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LIGHT, 20000);
        Aware.setSetting(this, Aware_Preferences.STATUS_APPLICATIONS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_MAGNETOMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_MAGNETOMETER, 20000);
        Aware.setSetting(this, Aware_Preferences.STATUS_TELEPHONY, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 20000);

        //Any active plugin/sensor shares its overall context using broadcasts
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context = new Intent(ACTION_AWARE_LOCATION_TYPE_INDOOR);
                context.putExtra(EXTRA_ELAPSED, indoor_elapsed);
                sendBroadcast(context);

                Intent contextR = new Intent(ACTION_AWARE_LOCATION_TYPE_OUTDOOR);
                context.putExtra(EXTRA_ELAPSED, outdoor_elapsed);
                sendBroadcast(contextR);
            }
        };

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        //DATABASE_TABLES =
        //TABLES_FIELDS =
        //CONTEXT_URIS = new Uri[]{ }

        //Ask AWARE to apply your settings
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
        TAG = "Template";
        DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if( DEBUG ) Log.d(TAG, "Template plugin terminated");
        Aware.setSetting(this, Settings.STATUS_PLUGIN_TEMPLATE, false);

        //Deactivate any sensors/plugins you activated here
        Aware.setSetting(this, Aware_Preferences.STATUS_SCREEN, false);
        Aware.setSetting(this, Aware_Preferences.STATUS_LIGHT, false);
        Aware.setSetting(this, Aware_Preferences.STATUS_APPLICATIONS, false);
        Aware.setSetting(this, Aware_Preferences.STATUS_MAGNETOMETER, false);
        Aware.setSetting(this, Aware_Preferences.STATUS_TELEPHONY, false);
        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, false);

        //Ask AWARE to apply your settings
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }
}

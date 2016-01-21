package com.aware.plugin.template;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.providers.Light_Provider;
import com.aware.providers.Linear_Accelerometer_Provider;
import com.aware.providers.Magnetometer_Provider;
import com.aware.providers.Telephony_Provider;
import com.aware.ui.Stream_UI;
import com.aware.utils.IContextCard;


public class ContextCard implements IContextCard {

    //Set how often your card needs to refresh if the stream is visible (in milliseconds)
    private int refresh_interval = 1 * 1000; //1 second = 1000 milliseconds

    //DEMO: we are demo'ing a counter incrementing in real-time
    //private int counter = 0;

    private Handler uiRefresher = new Handler(Looper.getMainLooper());
    private Runnable uiChanger = new Runnable() {
        @Override
        public void run() {
            //counter++;

            //Modify card's content here once it's initialized
            if( card != null ) {
                //DEMO display the counter value
                //counter_txt.setText(""+counter);

                Cursor light_data = sContext.getContentResolver().query(Light_Provider.Light_Data.CONTENT_URI, null, null, null, Light_Provider.Light_Data.TIMESTAMP + " DESC LIMIT 1");
                if( light_data != null && light_data.moveToFirst() ) {
                    double light_val = light_data.getDouble(light_data.getColumnIndex(Light_Provider.Light_Data.LIGHT_LUX));
                    light.setText(light_val + " lux");
                    if(light_val > 900){
                        light_result = "strong";
                    }
                    else{
                        light_result = "weak";
                    }
                }
                if( light_data != null && ! light_data.isClosed()) light_data.close();

                Cursor magnetometer_data = sContext.getContentResolver().query(Magnetometer_Provider.Magnetometer_Data.CONTENT_URI, null, null, null, Magnetometer_Provider.Magnetometer_Data.TIMESTAMP + " DESC LIMIT 1");
                if(magnetometer_data != null){
                    magnetometer_data.moveToFirst();
                    double magnetometer_val0 = magnetometer_data.getDouble(magnetometer_data.getColumnIndex(Magnetometer_Provider.Magnetometer_Data.VALUES_0));
                    double magnetometer_val1 = magnetometer_data.getDouble(magnetometer_data.getColumnIndex(Magnetometer_Provider.Magnetometer_Data.VALUES_1));
                    double magnetometer_val2 = magnetometer_data.getDouble(magnetometer_data.getColumnIndex(Magnetometer_Provider.Magnetometer_Data.VALUES_2));
                    double mag_value = Math.sqrt(magnetometer_val0*magnetometer_val0 + magnetometer_val1*magnetometer_val1 + magnetometer_val2*magnetometer_val2);
                    magnetometer.setText(mag_value + " uT");
                    if(mag_value > 60){
                        mag_result = "strong";
                    }
                    else{
                        mag_result = "weak";
                    }
                }
                if(magnetometer_data != null && !magnetometer_data.isClosed())magnetometer_data.close();

                Cursor telephony_data =sContext.getContentResolver().query(Telephony_Provider.GSM_Data.CONTENT_URI, null, null, null, Telephony_Provider.GSM_Data.TIMESTAMP + " DESC LIMIT 1");
                if(telephony_data !=null && telephony_data.moveToFirst()){
                    Double tel_val = telephony_data.getDouble(telephony_data.getColumnIndex(Telephony_Provider.GSM_Data.SIGNAL_STRENGTH));
                    telephony.setText(tel_val + " asu");
                    if(tel_val < 17){
                        gsm_result = "weak";
                    }
                    else{
                        gsm_result = "strong";
                    }
                }
                if( telephony_data != null && ! telephony_data.isClosed()) telephony_data.close();

                Cursor acc_data = sContext.getContentResolver().query(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.CONTENT_URI, null, null, null, Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP + " DESC LIMIT 1");
                if(acc_data != null){
                    acc_data.moveToFirst();
                    double acc_val0 = acc_data.getDouble(acc_data.getColumnIndex(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0));
                    double acc_val1 = acc_data.getDouble(acc_data.getColumnIndex(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1));
                    double acc_val2 = acc_data.getDouble(acc_data.getColumnIndex(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2));
                    double acc_value = Math.sqrt(acc_val0*acc_val0 + acc_val1*acc_val1 + acc_val2*acc_val2);
                    accelerometer.setText(acc_value + "");
                    if(acc_value < 1.3){
                        acc_result = "slow";
                    }
                    else{
                        acc_result = "fast";
                    }
                }
                if(acc_data != null && !acc_data.isClosed())acc_data.close();

                if(light_result.equals("strong")){
                    result.setText("OUTDOOR");
                    is_result = "outdoor";
                }
                else if(light_result.equals("weak") && gsm_result.equals("strong")){
                    if(acc_result.equals("fast")){
                        result.setText("OUTDOOR");
                        is_result = "outdoor";
                    }
                    else{
                        result.setText("OUTDOOR_BUS");
                        is_result = "outdoor";
                    }
                }
                else{
                    if(mag_result.equals("strong")){
                        result.setText("OUTDOOR_NIGHT");
                        is_result = "outdoor";
                    }
                    else{
                        result.setText("INDOOR");
                        is_result = "indoor";
                    }
                }

                if(is_result.equals("indoor")){
                    if(last_timestamp == 0) last_timestamp = System.currentTimeMillis();
                    stay_time += System.currentTimeMillis() - last_timestamp;
                    last_timestamp = System.currentTimeMillis();
                }
                if(is_result.equals("outdoor")){
                    counter_txt.setText(stay_time + " ms");
                }
            }

            //Reset timer and schedule the next card refresh
            uiRefresher.postDelayed(uiChanger, refresh_interval);
        }
    };

    //Empty constructor used to instantiate this card
    public ContextCard(){};

    //You may use sContext on uiChanger to do queries to databases, etc.
    private Context sContext;

    //Declare here all the UI elements you'll be accessing
    private View card;
    private TextView counter_txt;
    private TextView light;
    private TextView magnetometer;
    private TextView telephony;
    private TextView accelerometer;
    private TextView result;
    private String is_result;
    private String light_result;
    private String mag_result;
    private String gsm_result;
    private String acc_result;

    private static long last_timestamp = 0;
    private static long stay_time = 0;

    //Used to load your context card
    private LayoutInflater sInflater;

    @Override
    public View getContextCard(Context context) {
        sContext = context;

        //Tell Android that you'll monitor the stream statuses
        IntentFilter filter = new IntentFilter();
        filter.addAction(Stream_UI.ACTION_AWARE_STREAM_OPEN);
        filter.addAction(Stream_UI.ACTION_AWARE_STREAM_CLOSED);
        context.registerReceiver(streamObs, filter);

        //Load card information to memory
        sInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        card = sInflater.inflate(R.layout.card, null);

        //Initialize UI elements from the card
        //DEMO only
        counter_txt = (TextView) card.findViewById(R.id.counter);
        light = (TextView) card.findViewById(R.id.light_value);
        magnetometer = (TextView) card.findViewById(R.id.mag_value);
        telephony = (TextView) card.findViewById(R.id.tel_value);
        accelerometer = (TextView) card.findViewById(R.id.acc_value);
        result = (TextView) card.findViewById(R.id.result);

        //Begin refresh cycle
        uiRefresher.postDelayed(uiChanger, refresh_interval);

        //Return the card to AWARE/apps
        return card;
    }

    //This is a BroadcastReceiver that keeps track of stream status. Used to stop the refresh when user leaves the stream and restart again otherwise
    private StreamObs streamObs = new StreamObs();
    public class StreamObs extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( intent.getAction().equals(Stream_UI.ACTION_AWARE_STREAM_OPEN) ) {
                //start refreshing when user enters the stream
                uiRefresher.postDelayed(uiChanger, refresh_interval);

                //DEMO only, reset the counter every time the user opens the stream
            }
            if( intent.getAction().equals(Stream_UI.ACTION_AWARE_STREAM_CLOSED) ) {
                //stop refreshing when user leaves the stream
                uiRefresher.removeCallbacks(uiChanger);
                uiRefresher.removeCallbacksAndMessages(null);
            }
        }
    }
}

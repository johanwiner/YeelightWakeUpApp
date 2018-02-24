package com.example.johanstationar.alarmclock1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

/**
 * Created by JohanStationar on 2018-02-11.
 */
public class Alarm_receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("We are in alarm reciever", "Yey");

        //Create an intent to the RingtonePlayingSerivce.
        Intent serviceIntent = new Intent(context, RingtonePlayingService.class);

        //Start the Ringtone service.
        context.startService(serviceIntent);
    }
}

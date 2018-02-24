package com.example.johanstationar.alarmclock1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by JohanStationar on 2018-02-11.
 */
public class Alarm_receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("We are in alarm reciever", "Yey");

        //Get extra string from intent
        String extraString = intent.getExtras().getString("extra");
        Log.e("alarm reciever, extras", extraString);

        //Create an intent to the RingtonePlayingSerivce.
        Intent serviceIntent = new Intent(context, RingtonePlayingService.class);

        /* ------- Increase the volume for alarms to 1/3 of max volume. ------ */
        //TODO - Make this a setting in the app instead.
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
        // Remeber what the user's volume was set to before we change it.
        int userVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);


        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC/3),
                0);

        //Pass on the extra string from main to RingtonePlayingService.
        assert (extraString == "alarm on" || extraString == "alarm off");

        serviceIntent.putExtra("extra", extraString);

        //Start the Ringtone service.
        context.startService(serviceIntent);

        /*
            // reset the volume to what it was before we changed it.
            mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND);
            mp.stop();
            mp.reset();
        */
    }
}

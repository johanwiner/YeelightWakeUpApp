package com.example.johanstationar.alarmclock1;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by JohanStationar on 2018-02-15.
 */
public class RingtonePlayingService extends android.app.Service {

    MediaPlayer media_song;
    int my_startId = 0; // 1 = Alarm on, 0 = Alarm off.

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /* Does not seem to be run */
        Log.e("We are in RingtonePlayingService", "11111");
        return null;
    }

    @Override
    public void onCreate() {
        //Create an instance of the media player.
        media_song = MediaPlayer.create(this, R.raw.shinedownsecondchanceshort);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("We are in RingtonePlayingService", "Yejj");

        //Get the extra string values.
        String state = intent.getExtras().getString("extra");

        /* ----- Set the internal state of the alarm, on/off ----- */
        if (state.equalsIgnoreCase("alarm on")) {
            my_startId = 1;
        } else if (state.equalsIgnoreCase("alarm off")) {
            my_startId = 0;
        } else {
            my_startId = 0;
        }

        Log.e("RingtonePlayingService: state", Integer.toString(my_startId));
        Log.e("media_song.isPlaying", Boolean.toString(media_song.isPlaying()));

        if (!media_song.isPlaying() && (my_startId == 1)) {
            //There is no music playing and the user presses the alarm on button.
            //Start the song:
            media_song.start();
            //this.isRunning = true;
            Log.e("111", Boolean.toString(media_song.isPlaying()));
        } else if (media_song.isPlaying() && my_startId == 0) {
            //Music is playing and the user presses the alarm off button.
            Log.e("Turn of sounding alarm", "");
            media_song.stop();
            media_song.reset();
            media_song.release();

            //Set up a fresh one.
            media_song = MediaPlayer.create(this, R.raw.shinedownsecondchanceshort);
            //this.isRunning = false;
        } else if (!media_song.isPlaying() && my_startId == 0) {
            //There is no music playing and the user presses the alarm off button.
            Log.e("333", "AWDAWDAWD");
        } else if (media_song.isPlaying() && my_startId == 1) {
            //There is music playing and the user presses the alarm on button.
            Log.e("444", "DAWDAW");
        } else {
            Log.e("Should not get here", "aqXDAS");
            assert false;
        }





        //stopService (intent);

        return START_NOT_STICKY;
    }

    //Should stop the music.
    public boolean stopService (Intent service) {
        stopSelf();
        return true;
    }


    public void onDestroy() {
        //Tell the user that we have stopped.
        Log.e("RingtonePlayingService: has been destroyed", "ye");

        Toast.makeText(this, "onDestroy called", Toast.LENGTH_SHORT).show();
    }
}

package com.example.johanstationar.alarmclock1;

import android.app.Service;
import android.content.Intent;
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /* Does not seem to be run */
        Log.e("We are in RingtonePlayingService", "11111");
        return null;
    }

    @Override
    public void onCreate() {
        /* THIS IS RUN */
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("We are in RingtonePlayingService", "Yejj");

        //Create an instance of the media player.
        media_song = MediaPlayer.create(this, R.raw.shinedownsecondchanceshort);
        //Start the song:
        media_song.start();

        return START_NOT_STICKY;
    }

    public void onDestroy() {
        //Tell the user that we have stopped.
        Toast.makeText(this, "onDestroy called", Toast.LENGTH_SHORT).show();
    }
}

package com.example.johanstationar.alarmclock1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    TimePicker alarmTimePicker;
    TextView alarmStatusTextbox;
    Context context;
    Button startAlarmButton;
    Button stopAlarmButton;
    Calendar calender;
    Intent myIntent; //Path to Alarm_receiver.java
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Don't know why we need this.
        this.context = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /* --------------- Initialise ------------- */
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Initialise time picker
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);

        alarmStatusTextbox = findViewById(R.id.alarmStatusTextbox);

        //Create an instance of a calender.
        calender = Calendar.getInstance();

        //Create intent to Alarm_receiver class.
        myIntent = new Intent(this.context, Alarm_receiver.class);

        //Initialise set alarm button
        startAlarmButton = findViewById(R.id.startAlarmButton);
        startAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                calender.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getHour());
                calender.set(Calendar.MINUTE, alarmTimePicker.getMinute());

                //Put extra string into intent, that says "alarm on".
                myIntent.putExtra("extra", "alarm on");

                setAlarmText("Alarm On");

                //Create a pending intent that delays the intent
                //until the specified calender time.
                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
                        myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                //Set the alarm manager.
                alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(),
                        pendingIntent);
            }
        });


        //Initialise stop alarm button
        stopAlarmButton = findViewById(R.id.stopAlarmButton);
        stopAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlarmText("Alarm Off");

                //Cancel the alarm
                Log.e("Turned off an pending alarm intent", "");


                //If user presses Alarm Off before alarm on.
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);

                    //Put extra sting into intent, that says "alarm off".
                    myIntent.putExtra("extra", "alarm off");

                    //Stop the ringtone
                    sendBroadcast(myIntent);
                }
            }

        });
    }

    /*
    * @brief Function for setting the text of alarmStatusTextbox
    * @param s  String to set the in the alarmStatusTextbox.
    */
    public void setAlarmText(String s) {
        alarmStatusTextbox.setText(s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

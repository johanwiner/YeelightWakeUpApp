package com.example.johanstationar.alarmclock1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.util.Calendar;
import java.net.InetAddress;

/*   Android import   */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
/* End Android import */

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    TimePicker alarmTimePicker;
    TextView alarmStatusTextbox;
    TextView timeTextView;
    TextView editText; /* Yeelight */
    Context context;
    Button startAlarmButton;
    Button stopAlarmButton;
    Calendar calender;
    Intent myIntent; //Path to Alarm_receiver.java
    PendingIntent pendingIntent;

    WifiManager wifi;
    WifiManager.WifiLock lock; //Lock for keeping wifi  receptor alive

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Main: onCreate was called.", "");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Don't know why we need this.
        this.context = this;
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/

        /* --------------- Initialise ------------- */
        editText = findViewById(R.id.editText); /* Yeelight*/
        editText.setText("Yeelight app info");


        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Initialise time picker
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);

        //alarmStatusTextbox = findViewById(R.id.alarmStatusTextbox);
        timeTextView = findViewById(R.id.textView);

        //Create intent to Alarm_receiver class.
        myIntent = new Intent(this.context, Alarm_receiver.class);

        //Initialise set alarm button
        startAlarmButton = findViewById(R.id.startAlarmButton);
        startAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Create an instance of a calender.
                calender = Calendar.getInstance();

                Log.e("Alarm On was clicked.", "");

                //Reset calender to this day.
                calender.set(Calendar.HOUR_OF_DAY,
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                calender.set(Calendar.MINUTE,
                        Calendar.getInstance().get(Calendar.MINUTE));

                calender.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getHour());
                calender.set(Calendar.MINUTE, alarmTimePicker.getMinute());

                Log.e("Time " + calender.getTime().toString(), " ");

                long diff = calender.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

                if (diff <= 0) {
                    calender.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getHour() + 24);
                    calender.set(Calendar.MINUTE, alarmTimePicker.getMinute());
                }

                //Put extra string into intent, that says "alarm on".
                myIntent.putExtra("extra", "alarm on");

                String tmpTime = calender.getTime().toString();
                tmpTime = tmpTime.substring(0,19);

                //setAlarmText("Alarm On:     " + tmpTime);

                setAlarmTimeText("Alarm On:  " + tmpTime);

                //Create a pending intent that delays the intent
                //until the specified calender time.
                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,
                        myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                //Set the alarm manager.
                alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);

                //calender.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getHour() - 24);

                //Wait for Yeelight broadcast message
                String s = udp("Waiting");


            }
        });

        //Initialise stop alarm button
        stopAlarmButton = findViewById(R.id.stopAlarmButton);
        stopAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //setAlarmText("Alarm Off");
                setAlarmTimeText("Alarm Off");

                //Cancel the alarm
                Log.e("Turned off an pending alarm intent", "weeee");

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

    @Override
    public void onResume(){
        super.onResume();
        //editText.setText(udp("u").replaceAll("[^0-9]",""));
        editText.setText("Resumed");
    }

    /*
    * @brief Function for setting the text of alarmStatusTextbox
    * @param s  String to set the in the alarmStatusTextbox.
    */
    public void setAlarmTimeText(String s) {
        timeTextView.setText(s);
    }

    protected String udp(String msg){

        Log.e("Sending: ", msg);

        int broadcast_port = 1900;
        String broadcast_ip = "255.255.255.255";

        String re = "";

        wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null){
            Toast.makeText(getApplicationContext(), "WifiLock create!", Toast.LENGTH_LONG).show();
            lock = wifi.createWifiLock("mylock");
            lock.acquire();
        }

        try{
            /* Yeelight sends broadcast message over UDP */
            /* Listen for yeelight broadcasting message */
            byte[] buf = new byte[100];
            DatagramPacket dgp = new DatagramPacket(buf, buf.length);
            DatagramSocket s2 = new DatagramSocket(null);
            s2.setBroadcast( true );
            InetSocketAddress address = new InetSocketAddress(broadcast_ip, broadcast_port);
            s2.bind(address);
            Log.e("Receiving data?: ", "waiting...");
            editText.setText("Receiving data?: ...");
            s2.receive (dgp);
            Log.e("Data received", "!!");
            editText.setText("Data received!!");
            s2.close();
        /*
            DatagramSocket s = new DatagramSocket();
            s = new DatagramSocket(broadcast_port);
            s.setBroadcast( true );
            byte[] buf = new byte[100];
            DatagramPacket dgp = new DatagramPacket(buf, buf.length);
            Log.e("Receiving data?: ", "waiting...");
            editText.setText("Receiving data?: ...");
            s.receive (dgp);
            Log.e("Data received", "!!");
            editText.setText("Data received!!");
            s.close();
            */
            /*
            byte[] buf = msg.getBytes();
            InetAddress a = InetAddress.getByName(broadcast_ip);
            DatagramPacket p = new DatagramPacket(buf, buf.length, a, broadcast_port);
            s.send(p);
            if(msg.charAt(0) == 'u'){
                s.setSoTimeout(1000);
                buf = new byte[16];
                DatagramPacket rp = new DatagramPacket(buf, buf.length);
                s.receive(rp);
                re = new String(rp.getData());
            }
            s.close();
            */
        }catch(Exception e){
            Log.e("Exception thrown, dude!!! ", "err", e);
        }

        //Release wifi receptor lock.
        lock.release();

        return re;
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

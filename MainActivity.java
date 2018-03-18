package com.example.johanstationar.alarmclock1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Calendar;
import java.net.InetAddress;

/*   Android import   */
import java.net.DatagramPacket;
import java.util.HashMap;
/* End Android import */

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    TimePicker alarmTimePicker;
    TextView alarmStatusTextbox;
    TextView timeTextView;
    TextView editText;
    Context context;
    Button startAlarmButton;
    Button stopAlarmButton;
    Calendar calender;
    Intent myIntent; //Path to Alarm_receiver.java
    PendingIntent pendingIntent;

    /* Yeelight specific data. */
    int broadcast_port = 1982;
    String broadcast_ip = "239.255.255.250";

    private BufferedOutputStream mBos;
    private Socket mSocket;
    private BufferedReader mReader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Main: onCreate was called.", "");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Johan Ju
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


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
                tmpTime = tmpTime.substring(0, 19);

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
                //String s = receive_udp("Waiting");

                //establish_connection();
                searchDevice();
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
    public void onResume() {
        super.onResume();
        //editText.setText(receive_udp("u").replaceAll("[^0-9]",""));
        editText.setText("Resumed");
    }

    /*
    * @brief Function for setting the text of alarmStatusTextbox
    * @param s  String to set the in the alarmStatusTextbox.
    */
    public void setAlarmTimeText(String s) {
        timeTextView.setText(s);
    }

    /*
    * @brief Function for sending UDP messages.
    *
    * @note  This is the search response, the location will tell you the IP and port ************************************
    *           the bulb is listening, you should establish a TCP socket to this
    *           location and then send command through that socket.
    *
    *           The dynamic discover used a SSDP like protocol. You can send a
    *           multi-cast request to local network and will get response from
    *           bulbs. The request and response are encoded in HTTP format but
    *           are transferred through UDP.
    *
    *           After discovering the bulb, you can establish a TCP connection
    *           to the bulb and control and monitor the bulb through control
    *           requests which is encoded in JSON.
    *
    * @param s: String to send.
    */
    protected String establish_connection () {
        Log.e("establish_connection", "here");

        String re = "";


        /* 1 - Send udp multicast. */
        /* 2 - Receive udp package containing IP. */
        /* 3 - Set up TCP connection to the IP. */

        Log.e("mSocket is Connected", "here");
            try{
                String msg = "u\r\n";

            DatagramSocket ss = new DatagramSocket();
            byte[] buf = msg.getBytes();
            //InetAddress a = InetAddress.getByName("192.168.0.4");
            InetAddress a = InetAddress.getByName("192.168.1.170");
            DatagramPacket p = new DatagramPacket(buf, buf.length, a, 55443);
            Log.e("Sending packet", "here");
            ss.send(p);
            Log.e("Packet sent", "here");
            if(msg.charAt(0) == 'u'){
                ss.setSoTimeout(3000);
                buf = new byte[16];
                DatagramPacket rp = new DatagramPacket(buf, buf.length);
                ss.receive(rp);
                re = new String(rp.getData());
                Log.e("Packet received ", re);

            }
            ss.close();
        }catch(Exception e){
            Log.e("test", "err", e);
        }

        try {


            // join a Multicast group and send the group salutations
            InetAddress group = InetAddress.getByName(broadcast_ip);
            MulticastSocket s = new MulticastSocket(broadcast_port);
            s.joinGroup(group);


            while (true) {
                //Loop in case we have no internet connection.
                try {
                    // Enabling connecting to sockets in mainThread
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    //Connection here:
                    mSocket = new Socket(InetAddress.getByName(broadcast_ip), broadcast_port);
                    mSocket.setKeepAlive(true);
                    mBos = new BufferedOutputStream(mSocket.getOutputStream());
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                    if (mSocket.isConnected()) {
                        Log.e("mSocket is Connected", "here");
                    } else {
                        Log.e("mSocket is not Connected", "here");
                    }

                    while (true) {
                        Log.e("Todo: Break while true", "here");
                        try {
                            String value = mReader.readLine();
                            Log.e("est_c read value = ", value);
                            break;
                        } catch (Exception e) {
                            //Empty
                        }
                    }
                    //We break out from the internet connection while-loop.
                    break;
                } catch (Exception e) {
                    Log.e("estab_conn", "Exception at connect");
                    e.printStackTrace();
                }
            }


            /*
            String msg = "Hello";
            DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
                   group, broadcast_port);
            s.send(hi);
            */
            /*Fungerar ok, senast testad mot router broadcasts 2018-03-04 */
            // Yeelight sends broadcast message over UDP
            // Listen for yeelight broadcasting message
            byte[] buf = new byte[1000];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            Log.e("Receiving data?: ", "waiting...");
            editText.setText("Receiving data?: ...");
            s.receive(recv);
            Log.e("Data received", "!!");
            editText.setText("Done receiving");
            s.leaveGroup(group);
        } catch (Exception e) {
                Log.e("estab_conn", "Exception at connect");
                e.printStackTrace();
        }
        return re;
    }

    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";

    private void searchDevice() {
        Log.e("Call to searchDevice", " was made.");

        /*
        mDeviceList.clear();
        mAdapter.notifyDataSetChanged();
        mSeraching = true;
        mSearchThread = new Thread(new Runnable() {
            @Override
            public void run() {
    */
                try {
                    DatagramSocket mDSocket = new DatagramSocket();
                    DatagramPacket dpSend = new DatagramPacket(message.getBytes(),
                            message.getBytes().length, InetAddress.getByName(broadcast_ip),
                            broadcast_port);
                    //mDSocket.send(dpSend);
                    //mHandler.sendEmptyMessageDelayed(MSG_STOP_SEARCH,2000);
                    //while (mSeraching) {

                    while (true) {
                        Log.e("Entering while looop in searchDevice", ".");
                        android.os.SystemClock.sleep(1000);

                        mDSocket.send(dpSend);

                        //TODO Break out of the loop some time
                        byte[] buf = new byte[1024];
                        DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);

                        mDSocket.receive(dpRecv);
                        Log.e("Received a packet in searchDevice", ".");

                        byte[] bytes = dpRecv.getData();
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < dpRecv.getLength(); i++) {
                            // parse /r
                            if (bytes[i] == 13) {
                                continue;
                            }
                            buffer.append((char) bytes[i]);
                        }
                        Log.d("socket", "got message:" + buffer.toString());
                        if (!buffer.toString().contains("yeelight")) {
                            //mHandler.obtainMessage(0, "收到一条消息,不是Yeelight灯泡").sendToTarget();
                            return;
                        }
                        String[] infos = buffer.toString().split("\n");
                        HashMap<String, String> bulbInfo = new HashMap<String, String>();
                        for (String str : infos) {
                            int index = str.indexOf(":");
                            if (index == -1) {
                                continue;
                            }
                            String title = str.substring(0, index);
                            String value = str.substring(index + 1);
                            bulbInfo.put(title, value);
                        }
                        /*
                        if (!hasAdd(bulbInfo)){
                            mDeviceList.add(bulbInfo);
                        }
                        */
                    }
                    //mHandler.sendEmptyMessage(MSG_DISCOVER_FINISH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }

    /*
    * @brief Function for receiving UDP messages.
    * @param s  String to sent.
    */
    protected String receive_udp(String msg) {

        String re = "";

        try {
            // join a Multicast group and send the group salutations
            InetAddress group = InetAddress.getByName(broadcast_ip);
            MulticastSocket s = new MulticastSocket(broadcast_port);
            s.joinGroup(group);

/*
            //Fungerar ok, senast testad mot router broadcasts 2018-03-04
            // Yeelight sends broadcast message over UDP
            // Listen for yeelight broadcasting message
            byte[] buf = new byte[1000];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            Log.e("Receiving data?: ", "waiting...");
            editText.setText("Receiving data?: ...");
            s.receive(recv);
            Log.e("Data received", "!!");
            editText.setText("Done receiving");
            s.leaveGroup(group);

*/
            //Fungerar ok.
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
            //Fungerar ok.
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

        } catch (Exception e) {
            Log.e("Exception thrown, dude!!! ", "err", e);
        }

        return re;
    }


//End of discovery function.

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

package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.CallLog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DistressActivity extends Activity {
	/** Called when the activity is first created. */
    private static Button distressButton;
    private static Button reportButton;
    private String distressSignal;
    ParseUser currentUser = ParseUser.getCurrentUser();
    private String ICE = currentUser.get("ICE").toString();
    private String role = currentUser.get("Role").toString();
    private double[] gps_coordinates = new double[2];
    private final Handler myHandler = new Handler();
    private Timer timer;
    String currentDateTimeString;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
        setUI();
		ParseAnalytics.trackAppOpenedInBackground(getIntent());
	}

    public void setUI()
    {
        reportButton = (Button) findViewById(R.id.reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),DistressListActivity.class);
                startActivity(i);
            }
        });
        distressButton = (Button) findViewById(R.id.buttonDistress);
        distressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDistress();
                distressButton.setEnabled(false);
                //timer = new Timer();
                //timer.schedule(new UpdateTimeTask(), 1000, 50000);
            }
        });
    }
    private final Runnable myRunnable = new Runnable() {
        public void run()
        {
            distressButton.setEnabled(true);
        }
    };
    public class UpdateTimeTask extends TimerTask {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            myHandler.post(myRunnable);
        }

    }
    public void sendDistress()
    {
        String address1 = null;
        String address2 = null;
        String city = null;

        if(hasActiveInternetConnection())
        {
            ParseQuery pushQuery1 = ParseInstallation.getQuery();
            ParseQuery pushQuery2 = ParseInstallation.getQuery();
            pushQuery1.whereEqualTo("channels", "User"); // Set the channel
            ArrayList<ParseQuery> roles = new ArrayList<ParseQuery>();
            roles.add(pushQuery1);
            //roles.add("User");

            // Send push notification to query
            //gps_coordinates = getLocation();
            gps_coordinates = getLocation();
            getReverseGeoCoding geocode = new getReverseGeoCoding(Double.toString(gps_coordinates[0]),Double.toString(gps_coordinates[1]));
            geocode.getAddress();
            address1 = geocode.getAddress1();
            address2 = geocode.getAddress1();
            city = geocode.getCity();
            //distressSignal = "\"Distress Initiation Name: \"+ currentUser.get(\"Name\").toString() + \"\\nDistress Initiatior: \" + role + \"\\nDistress Address: \" + address1 + \" \" + address2 + \" \" + city + \" \" + \"\\nDistress Signal From:\" + gps_coordinates[0] + \"-\" + gps_coordinates[1]";
            if(role == "Authority") {
                pushQuery2.whereEqualTo("channels", role);
                roles.add(pushQuery2);
                ParsePush push2 = new ParsePush();
                push2.setQuery(pushQuery2);
                currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                push2.setMessage(currentDateTimeString + "\nDistress Initiation Name: " + currentUser.get("Name").toString() + "\nDistress Initiatior: " + role + "\nDistress Address: " + address1 + " " + address2 + " " + city + " " + "\nDistress Signal From:" + gps_coordinates[0] + "-" + gps_coordinates[1]);
                push2.sendInBackground();
            }
            else {
                ParsePush push = new ParsePush();
                push.setQuery(pushQuery2);
                currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                push.setMessage(currentDateTimeString + "\nDistress Initiation Name: " + currentUser.get("Name").toString() + "\nDistress Initiatior: " + role + "\nDistress Address: " + address1 + " " + address2 + " " + city + " " + "\nDistress Signal From:" + gps_coordinates[0] + "-" + gps_coordinates[1]);
                push.sendInBackground();
            }
            currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            ParseObject pushData = new ParseObject("PushData");
            pushData.put("PusherName", currentUser.get("Name"));
            pushData.put("PusherRole", currentUser.get("Role"));
            pushData.put("PusherCNIC", currentUser.get("CNIC"));
            pushData.put("PushMessage", currentDateTimeString + "  Distress Initiation Name: " + currentUser.get("Name").toString() + "  Distress Initiatior: " + role + "  Distress Address: " + address1 + " " + address2 + " " + city + " " + "  Distress Signal From:" + gps_coordinates[0] + "-" + gps_coordinates[1]);
            pushData.put("PushCount", 0);
            pushData.saveInBackground();
        }
        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        sendSMS("Distress Initiation Name: " + currentUser.get("Name").toString() + "\nDistress Initiatior: " + role + "\nDistress Address: " + address1 + " " + address2 + " " + city + " " + "\nDistress Signal From:" + gps_coordinates[0] + "-" + gps_coordinates[1]);
    }


    //sends sms to everyone
    public void sendSMS(String distressSignal)
    {
        HashSet<String> callLog = new HashSet<String>();
        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = DistressActivity.this.managedQuery(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int num = 0;
        while(managedCursor.moveToFirst() && num < 10){
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int countryCode = managedCursor.getColumnIndex(CallLog.Calls.COUNTRY_ISO);
            if(managedCursor.getString(countryCode).equals("PK"))
            {
                callLog.add(managedCursor.getString(number));
                num++;
            }
        }
        SmsManager smsManager = SmsManager.getDefault();
    //    smsManager.sendTextMessage(ICE, null, distressSignal, null, null);
        Iterator it = callLog.iterator();
        while(it.hasNext())
        {
            Toast.makeText(DistressActivity.this, it.next().toString(), Toast.LENGTH_SHORT);
            smsManager.sendTextMessage("03214147032", null, distressSignal, null, null);
        }
    }

    //checks if a network is available or not
    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    @Override
    public void onBackPressed() {
        //do nothing
    }
    //checks if internet is available or not
    public boolean hasActiveInternetConnection() {
        if (isNetworkAvailable()) {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e("Wifi Check", "Error checking internet connection", e);
            }
        } else {
            Log.d("Wifi Check", "No network available!");
        }
        return false;
    }

    private double[] getLocation() {
        double[] coord = new double[2];
        GPSTracker gps = new GPSTracker(this);
        if(gps.canGetLocation())
        {
            coord[0] = gps.getLatitude(); // returns latitude
            coord[1] = gps.getLongitude(); // returns longitude
            return coord;
        }
        else
        {
            //TODO: Add coarse location
            coord[0] = 0; // returns latitude
            coord[1] = 0;
        }
        coord[0] = 0; // returns latitude
        coord[1] = 0;
        return coord;
    }
}

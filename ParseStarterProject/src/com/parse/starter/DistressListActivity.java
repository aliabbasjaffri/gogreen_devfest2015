package com.parse.starter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DistressListActivity extends Activity {
    ParseUser currentUser = ParseUser.getCurrentUser();
    String role = currentUser.get("Role").toString();
    ArrayList<ParseObject> alertsList = new ArrayList<ParseObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distress_list);
        populateAlertsList();
    }

    public void populateAlertsList()
    {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PushData");


       /* try {
            List<ParseObject> list = query.find();
            for (int i=0; i < list.size(); ++i)
            {
                alertsList.add(list.get(i));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    for (int i=0; i < list.size(); ++i)
                    {
                        alertsList.add(list.get(i));
                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
                populateListView();
            }
        });
    }
    MyListAdapter adapter;
    private void populateListView() {
        adapter = new MyListAdapter();

        ListView list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_distress_list, menu);
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
    //private int globPosition = 0;
    private class MyListAdapter extends ArrayAdapter<ParseObject> {
        public MyListAdapter() {
            super(DistressListActivity.this, R.layout.displayreports_row,alertsList);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.displayreports_row, parent, false);
            }
            ParseObject obj = alertsList.get(position);
            int is_verified = obj.getInt("authorityVerified");
            // Make:
            TextView makeText = (TextView) itemView.findViewById(R.id.list_row_name);
            if(role.equals ("Authority"))
                makeText.setText(obj.get("PusherName").toString() + '\n' + obj.get("PusherCNIC").toString());
            else
                makeText.setText(obj.get("PusherName").toString());

            TextView countView = (TextView) itemView.findViewById(R.id.list_verified_count);
            countView.setText("Verified: " + obj.get("PushCount").toString());

            TextView pushmessagetext = (TextView) itemView.findViewById(R.id.alertText);
            pushmessagetext.setText(obj.get("PushMessage").toString());
            if(is_verified == 1)
            {
                countView.setTextColor(getResources().getColor(R.color.red));
            }
            else
            {
                countView.setTextColor(getResources().getColor(R.color.black));
            }
            Button verifyButton = (Button) itemView.findViewById(R.id.list_verify_button);
            //globPosition = position;
            verifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseObject obj = alertsList.get(position);
                    List<Object> verifiers = obj.getList("Verifiers");
                    boolean isVerifier = false;
                    if(verifiers != null) {
                        for (int i = 0; i < verifiers.size(); i++) {
                            if (verifiers.get(i).equals(currentUser.get("CNIC").toString())) {
                                isVerifier = true;
                                break;
                            }
                        }
                    }
                    if (!isVerifier) {
                        if (role.equals("Authority")) {
                            obj.put("authorityVerified", 1);
                            obj.saveInBackground();
                            ParseQuery pushQuery2 = ParseInstallation.getQuery();
                            pushQuery2.whereEqualTo("channels", "User"); // Set the channel
                            ParsePush push = new ParsePush();
                            push.setQuery(pushQuery2);
                            push.setMessage("AUTHORITY VERIFICATION: \n" + alertsList.get(position).get("PusherName") + " distress call verified by " + currentUser.get("Name") + " Authority. ");
                            push.sendInBackground();
                        }
                        if(verifiers==null) {
                            verifiers = new ArrayList<Object>();
                        }
                        verifiers.add(currentUser.get("CNIC").toString());
                        obj.put("Verifiers", verifiers);
                        int old_count = obj.getInt("PushCount");
                        old_count++;
                        obj.put("PushCount", old_count);
                        try {
                            obj.save();
                            Toast.makeText(getApplicationContext(), "Event verified successfully!", Toast.LENGTH_SHORT).show();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        //view.setEnabled(false);
                        adapter.clear();
                        populateAlertsList();
                        populateListView();
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Alert already verified", Toast.LENGTH_SHORT).show();
                }
            });
            return itemView;
        }
    }
}

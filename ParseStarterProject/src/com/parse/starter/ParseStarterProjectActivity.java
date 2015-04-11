package com.parse.starter;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParseStarterProjectActivity extends Activity {
    private Button submitButton;
    private String cnic;
    private static String userPushChannel = "User";
    private static String authorityPushChannel = "Authority";
    public void subscribeToPush(String channel)
    {
        //register for push notifications
        ParsePush.subscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }

    private Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);
        spinner = (Spinner) findViewById(R.id.roleSpinner);
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("User");
        spinnerArray.add("Authority");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, spinnerArray); //selected item will look like a spinner set from XML
        spinner.setAdapter(spinnerArrayAdapter);
        subscribeToPush(userPushChannel);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null)
        {
            Intent i=new Intent(this, DistressActivity.class);
            startActivity(i);
        }
        submitButton = (Button) findViewById(R.id.registerationSubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText name_et = (EditText) findViewById(R.id.nameEditText);
                EditText cnic_et = (EditText) findViewById(R.id.cnicEditText);
                EditText ice_et = (EditText) findViewById(R.id.emergencyContactEditText);
                EditText email_et  = (EditText) findViewById(R.id.emailEditText);

                String name = name_et.getText().toString();
                cnic = cnic_et.getText().toString();
                String ice = ice_et.getText().toString();
                String email = email_et.getText().toString();
                String role = spinner.getSelectedItem().toString();
                if(!emailValidator( email)|| email.length()==0)
                {
                    Toast.makeText(getApplicationContext(),
                            "Please enter valid email address", Toast.LENGTH_LONG)
                            .show();
                    email="";
                    email_et.setText(email);
                    return;

                }
                if(cnic.length()!=13 || cnic.length() == 0)
                {
                    Toast.makeText(getApplicationContext(),
                            "Invalid CNIC", Toast.LENGTH_LONG).show();
                    cnic="";
                    cnic_et.setText(cnic);
                    return;
                }
                if(ice.length() == 0)
                {
                    Toast.makeText(getApplicationContext(),
                            "Invalid ICE", Toast.LENGTH_LONG).show();
                    ice="";
                    ice_et.setText(ice);
                    return;
                }
                if(name.length()==0 )
                {
                    Toast.makeText(getApplicationContext(),
                            "Invalid Name", Toast.LENGTH_LONG).show();
                    name="";
                    name_et.setText(name);
                    return;
                }

                ParseUser newUser = new ParseUser();
                newUser.setUsername(cnic);
                newUser.setEmail(email);
                newUser.put("CNIC", cnic);
                newUser.put("ICE", ice);
                newUser.setPassword("");
                newUser.put("Name", name);
                if(role == "Authority")
                {
                    subscribeToPush(authorityPushChannel);
                    newUser.put("Role", "Authority");
                }
                else
                    newUser.put("Role", "User");
                newUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null)
                        {
                            ParseUser.logInInBackground(cnic,"", new LogInCallback() {
                                public void done(ParseUser user, ParseException e2) {
                                    if (user != null) {
                                        Toast.makeText(ParseStarterProjectActivity.this,
                                                "User registered successfully!", Toast.LENGTH_LONG)
                                                .show();
                                        Intent i=new Intent(getApplicationContext(),DistressActivity.class);
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(ParseStarterProjectActivity.this,
                                                e2.toString(), Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(ParseStarterProjectActivity.this,
                                    e.toString(), Toast.LENGTH_LONG)
                                    .show();
                        }

                    }
                });
            }
        });
    }
    public boolean emailValidator(String email)
    {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    @Override
    public void onBackPressed() {
        //do nothing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

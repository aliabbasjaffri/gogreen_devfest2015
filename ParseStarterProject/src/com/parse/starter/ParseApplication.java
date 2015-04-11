package com.parse.starter;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

public class ParseApplication extends Application {


  @Override
  public void onCreate() {
    super.onCreate();

    // Initialize Crash Reporting.
    ParseCrashReporting.enable(this);

    // Enable Local Datastore.
    Parse.enableLocalDatastore(this);

    // Add your initialization code here
    Parse.initialize(this, "ZEHv9djqUwtsjJSG9qDqlURP0vVzi4ywOHCes4rE", "eFNBJZzGA8jJne4hwCHsrNsDsp7JD976dPBZITo1");

    // Specify a Activity to handle all pushes by default.
      PushService.setDefaultPushCallback(this, DistressListActivity.class);

      // Save the current installation.


      try {
          ParseInstallation.getCurrentInstallation().save();
      } catch (ParseException e) {
          e.printStackTrace();
      }

      //ParseUser.enableAutomaticUser();
    ParseACL defaultACL = new ParseACL();
    // Optionally enable public read access.
    defaultACL.setPublicReadAccess(true);
    defaultACL.setPublicWriteAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

  }
}

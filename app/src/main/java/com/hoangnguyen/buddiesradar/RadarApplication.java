package com.hoangnguyen.buddiesradar;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import com.hoangnguyen.buddiesradar.models.Follow;
import com.hoangnguyen.buddiesradar.models.Room;
import com.hoangnguyen.buddiesradar.models.User;
import com.hoangnguyen.buddiesradar.models.UserDetail;

/**
 * Created by tl on 05.02.15.
 */
public class RadarApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Room.class);
        ParseObject.registerSubclass(UserDetail.class);
        ParseObject.registerSubclass(Follow.class);

        Parse.initialize(this,
                "cuV0C51aioI2eMuE5eAncgkUgZL5XBaTQCqzigwx",
                "z3dJcBEZZIlt6MofwQXfh5P7cimHLBwxNh10LxKF");
    }
}

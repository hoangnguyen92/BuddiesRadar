package com.hoangnguyen.buddiesradar.services;

import android.app.Service;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.parse.ParseException;

import com.hoangnguyen.buddiesradar.data.LocalDb;
import com.hoangnguyen.buddiesradar.models.User;
import com.hoangnguyen.buddiesradar.models.UserDetail;
import com.hoangnguyen.buddiesradar.widgets.TrackingStatusToggleWidget;

public class LocationTrackingService extends Service implements Handler.Callback {

    public final static String ACTION_START_MONITORING = "com.hoangnguyen.buddiesradar.action.ACTION_START_MONITORING";
    public final static String ACTION_STOP_MONITORING = "com.hoangnguyen.buddiesradar.action.ACTION_STOP_MONITORING";
    public final static String HANDLER_THREAD_NAME = "trackingservicethread";

    private static final String ON_TRACKING_CLICK = "com.hoangnguyen.buddiesradar.action.ON_TRACKING_CLICK";

    private final static long GPS_INTERVAL_TRACKING = 0;
    private final static long NETWORK_INTERVAL_TRACKING = 30000;
    private final static long GPS_MIN_DISTANCE = 0;
    private final static long NETWORK_MIN_DISTANCE = 0;

    private LocationListener mGpsListener;
    private LocationListener mNetworkListener;
    private Looper mLooper;
    private Handler mHandler;
    private LocalDb mLocalDb;
    private User mCurrentUser;

    public LocationTrackingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocalDb = LocalDb.getInstance();
        mCurrentUser = mLocalDb.getCurrentUser();

        HandlerThread thread = new HandlerThread(HANDLER_THREAD_NAME);
        thread.start();

        mLooper = thread.getLooper();

        mHandler = new Handler(mLooper, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mHandler.sendMessage(mHandler.obtainMessage(0, intent));
        }

        return START_STICKY;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Intent intent = (Intent) msg.obj;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_START_MONITORING)) {
            mLocalDb.setTrackingOn(true);
            doStartTracking();
        } else if (action.equalsIgnoreCase(ACTION_STOP_MONITORING)) {
            mLocalDb.setTrackingOn(false);
            doStopTracking();
            stopSelf();
        }

        return true;
    }

    private void doStartTracking() {
        doStopTracking();

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Track with GPS provider
        mGpsListener = new ServiceLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_INTERVAL_TRACKING, GPS_MIN_DISTANCE, mGpsListener, mLooper);

        // Track with Network provider
        mNetworkListener = new ServiceLocationListener();
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_INTERVAL_TRACKING, NETWORK_MIN_DISTANCE, mNetworkListener, mLooper);

        updateWidget();

        setUserActiveState(true);
    }

    private void doStopTracking() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (mGpsListener != null) {
            lm.removeUpdates(mGpsListener);
            mGpsListener = null;
        }

        if (mNetworkListener != null) {
            lm.removeUpdates(mNetworkListener);
            mNetworkListener = null;
        }

        updateWidget();

        setUserActiveState(false);
    }

    private void setUserActiveState(boolean isActive) {
        if (mCurrentUser != null) {
            try {
                UserDetail location = mCurrentUser.getUserDetail();
                location.fetchIfNeeded();
                location.setActive(isActive);
                location.save();
            } catch (ParseException e) {
                Log.d(LocationTrackingService.class.getSimpleName(),
                        e.getMessage());
            }
        }
    }

    private void updateWidget() {
        Intent statusToggleIntent = new Intent(this, TrackingStatusToggleWidget.class);
        statusToggleIntent.setAction(ON_TRACKING_CLICK);

        sendBroadcast(statusToggleIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        doStopTracking();

        if (mLooper != null) {
            mLooper.quit();
        }
    }
}

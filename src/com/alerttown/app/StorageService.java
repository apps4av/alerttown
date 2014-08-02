package com.alerttown.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * @author zkhan
 * Main storage service. It stores all states so when activity dies,
 */
public class StorageService extends Service implements Observer, LocationListener {

    
    private EventInterface mEvent;
    
    /**
     * For performing periodic activities.
     */
    private Timer mTimer;
        
    
    private long mLastGetTime;
    
    private long mLastLocationTime;
    
    private HashMap<Long, Event> mEvents;

    private LocationManager mLocationManager;
    
    private Location mLocation;
    
    private ReportFilter mReports;
    
    /**
     * Local binding as this runs in same thread
     */
    private final IBinder binder = new LocalBinder();
    
    /**
     * Make sure you run the service in foreground to let it run forever
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        Notification notification = new Notification(R.drawable.alert,
                getString(R.string.notifications_enabled), System.currentTimeMillis());
     
        Intent main = new Intent(this, MainActivity.class);
        main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, main,  PendingIntent.FLAG_UPDATE_CURRENT);
     
        notification.setLatestEventInfo(this, getString(R.string.app_name), getString(R.string.notifications_enabled), pendingIntent);
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;
     
        startForeground(2, notification);
     
        return START_STICKY;
    }
    
    /**
     * @author zkhan
     *
     */
    public class LocalBinder extends Binder {
        /**
         * @return
         */
        public StorageService getService() {
            return StorageService.this;
        }
    }
    
    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }
    
    /* (non-Javadoc)
     * @see android.app.Service#onUnbind(android.content.Intent)
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
          
        super.onCreate();

        mTimer = new Timer();
        TimerTask gpsTime = new UpdateTask();
     
        mEvents = new HashMap<Long, Event>();
        
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        /*
         * Only last 24 hours
         */
        mLastGetTime = System.currentTimeMillis() - 24 * 3600 * 1000;
        mLastLocationTime = System.currentTimeMillis() - 24 * 3600 * 1000;

        mReports = new ReportFilter();
        
        /*
         * Every minute, start 5 sec after start
         */
        mTimer.scheduleAtFixedRate(gpsTime, 5000, 5000);
    }
        
    /* (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTimer != null) {
            mTimer.cancel();
        }
    }
    
    /**
     * @author zkhan
     * Timer
     */
    private class UpdateTask extends TimerTask {
        
        /* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
        public void run() {
            mHandler.sendEmptyMessage(0);
        }
    }

    /**
     * 
     */
    @Override
    public void update(Observable observable, Object data) {
        if(data instanceof Report) {
            /*
             * A backup report got submitted
             */
            Report r = (Report)data;
            if(r.submitted) {
                /*
                 * remove a submitted report
                 */
                mReports.removeReport(r);
            }
        }
        else {
            /*
             * Got events. Store
             */
            LinkedList<Event> events = (LinkedList<Event>)data;
            for(Event e : events) {
                Event old = mEvents.get(e.id);
                if(old != null) {
                    // this is required since if notified, this could cause re-notify
                    if(old.shouldNotNotify()) {
                        e.setNotified();
                    }
                }
                mEvents.put(e.id, e);
                e.updateLocation(mLocation);
            }

            // store this for next get time
            mLastGetTime = System.currentTimeMillis();
            
            // if app is showing, update the map
            if(mEvent != null) {
                mEvent.getCallback(mEvents);
            }
        }

    }
    
    /**
     * Activity calls to get notified
     * @param e
     */
    public void setEventListener(EventInterface e) {
        if(null != e) {
            /*
             * Send latest
             */
            e.getCallback(mEvents);
        }
        mEvent = e;
    }

    /**
     * This leak warning is not an issue if we do not post delayed messages, which is true here.
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long time = (System.currentTimeMillis() - mLastGetTime) / 60000;
            if(time > 0) {
                Get g = new Get();
                g.addObserver(StorageService.this);
                g.getIt(time); // in mins
                
                // send out backups if any
                mReports.sendOutBackups(StorageService.this);
                tryGetLocation();
            }

            time = (System.currentTimeMillis() - mLastLocationTime) / 60000;
            if(time >= 5) {
                /*
                 * If 6 minutes passed since last location recvd, ask for location every minute
                 */
                mLocation = null;
            }
        }
    };

    /**
     * Service kept location
     * @return
     */
    public Location getLocation() {
        return mLocation;
    }

    @Override
    public void onLocationChanged(Location arg0) {
        if(arg0 != null) {
            // got location, try later again
            mLocation = arg0;
            mLocationManager.removeUpdates(StorageService.this);
            mLastLocationTime = System.currentTimeMillis();
            
            // update distance from each event
            Object[] list = mEvents.values().toArray();

            for(int i = 0; i < list.length; i++) {
                Event e = (Event)list[i];
                
                if(e.isExpired()) {
                    /*
                     * expired event, throw out
                     */
                    mEvents.remove(e.id);
                    continue;
                }
                
                /*
                 * Update event location
                 */
                e.updateLocation(mLocation);
                
                /*
                 * New crime events nearby. put in notification bar
                 */
                if(e.shouldNotNotify()) {
                    continue;
                }
                NotificationBar n = new NotificationBar(getApplicationContext());
                n.notifyCrime(e);
                // notified, dont do it again
                e.setNotified();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    /**
     * 
     */
    private void tryGetLocation() {
        /*
         * Hopefully all will agree
         */
        long minTime = 60 * 1000;
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, 0, this, null);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, this, null);
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, 0, this, null);
    }

    /**
     * 
     * @param r
     */
    public void backupReport(Report r) {
        mReports.addReport(r);
    }
    
}
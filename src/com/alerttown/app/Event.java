package com.alerttown.app;

import java.util.Date;


import android.location.Location;

/**
 * @author zkhan
 * Basic event class for events received from the internet
 *
 */
public class Event {

    /*
     * Instead of getters just access them 
     */
    public long id;
    public String type;
    public double lon;
    public double lat;
    public String reported;
    public String duration;
    public float radius;
    public int count;
    public boolean valid;
    public String updated;
    public boolean push;
    private long mExpires;
    
    private double mDistance;
    
    private boolean mNotified;
    
    private static final double FAR = 500;
    
    
    /**
     * 
     * @param toParse String from the internet to parse
     */
    public Event(String toParse) {
             
        valid = false;
        mDistance = 1E10;
        mNotified = false;
        
        // its a CSV
        String tokens[] = toParse.split(",");
        
        // make sure its not corrupted
        if(tokens.length != 10) {
            return;
        }
        
        try {
            
            //parse the event from PHP
            
            type = tokens[0];
            lat = Double.parseDouble(tokens[1]);
            lon = Double.parseDouble(tokens[2]);
            reported = tokens[3];
            duration = tokens[4];
            radius = Float.parseFloat(tokens[5]);
            count = Integer.parseInt(tokens[6]);
            id = Long.parseLong(tokens[7]);
            updated = tokens[8];
            push = tokens[9].equals("Y") ? true : false;
            
            // reported time in UTC ms
            Date date = Util.fromISODateString(reported);
            long time = date.getTime();
            
            // duration
            String tokens0[] = duration.split(":");
           
            int hours = Integer.parseInt(tokens0[0]);
            int mins = Integer.parseInt(tokens0[1]);
            int secs = Integer.parseInt(tokens0[2]);
            
            long durtime = (hours * 60 * 60 + mins * 60  + secs) * 1000;
           
            // expiry time
            mExpires = time + durtime;

        }
        catch(Exception e) {
            return;
        }
        
        valid = true;
    }
    
    /**
     * The event could expire here and on internet. If not connected to internet, there
     * must be a way to delete the event locally.
     * @return
     */
    public boolean isExpired() {

        long now = System.currentTimeMillis();
        long diff = now - mExpires;
        if(diff > 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Update its location
     */
    public void updateLocation(Location l) {
        if(l == null) {
            mDistance = 1E10;
            return;
        }
        Projection p = new Projection(lon, lat, l.getLongitude(), l.getLatitude());
        mDistance = p.getDistance();
    }
    
    /**
     * Is this far?
     * @return
     */
    public boolean isFar() {
        return mDistance > FAR;
    }

    /**
     * Is this far?
     * @return
     */
    private boolean isNotDanger() {
        return mDistance > radius;
    }

    /**
     * 
     * @return
     */
    public void setNotified() {
        mNotified = true;
    }
    
    /**
     * 
     * @return
     */
    public boolean shouldNotNotify() {
        if(!push) {
            return true;
        }
        if(mNotified) {
            return true;
        }
        if(isNotDanger()) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @return
     */
    public double getDistance() {
        return mDistance;
    }
}

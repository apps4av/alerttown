package com.alerttown.app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 
 * @author zkhan
 * A report calss
 */
public class Report {

    /**
     * Put a report initial together and allow the public members to be populated
     */
    public Report() {
        
        Date d = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fmt.setTimeZone(TimeZone.getTimeZone("gmt"));
        reported = fmt.format(d);
        submitted = false;
    }
    

    public String type;
    public String lon;
    public String lat;
    public String reported;
    public String username;
    public String notes;
    public String uuid;
    public boolean submitted;
}

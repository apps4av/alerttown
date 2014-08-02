package com.alerttown.app;


import java.util.LinkedList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 
 * @author zkhan
 * A class that filter reports at the app level.
 * It also allows capability to store the reports
 */
public class ReportFilter {

    LinkedList<Report> mBackupReports;
    
    // A delay that limits report frequency.
    private static final long DELAY = 1000 * 30;
    
    /**
     * Init the list where reports are kept
     */
    public ReportFilter() {
        mBackupReports = new LinkedList<Report>();
    }
    
    
    /**
     * Get last time a report was submitted from preferences
     * @param ctx
     * @return
     */
    private static long getLastTime(Context ctx) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        long recent = pref.getLong("LastReported", 0);
        return recent;
    }

    /**
     * Set last time when a report was submitted from preferences
     * @param ctx
     * @param last
     */
    private static void setLastTime(Context ctx, long last) {        
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong("LastReported", last);
        editor.commit();
    }
    
    /**
     * App can check if a report can be submitted now
     * @param ctx
     * @return
     */
    public static boolean canSubmit(Context ctx) {
        long diff = System.currentTimeMillis() - getLastTime(ctx);
        if(diff > DELAY) {
            return true;
        }
        return false;
    }
    
    /**
     * App calls this to set time after report is sbmitted
     * @param ctx
     */
    public static void submit(Context ctx) {
        setLastTime(ctx, System.currentTimeMillis());
    }

    /**
     * App calls this to store a non xmitted report
     * @param r
     */
    public void addReport(Report r) {
        mBackupReports.add(r);
    }

    /**
     * App calls this to remove a xmitted report
     * @param r
     */
    public void removeReport(Report r) {
        mBackupReports.remove(r);
    }
    

    /**
     * Service calls this periodically to send out reports
     */
    public void sendOutBackups(StorageService service) {
        for(Report r : mBackupReports) {
            Put put = new Put();
            put.addObserver(service);
            put.putIt(r);
        }
    }
}

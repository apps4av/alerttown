package com.alerttown.app;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * 
 * @author zkhan
 * Stricly a class to store marker data for android-map-utils
 */
public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private final Bitmap mImage;
    private String mTitle;
    private String mTime;
    private Long mId;
    private int mCount;

    /**
     * 
     * @param lat
     * @param lng
     * @param b
     * @param title
     * @param time
     * @param id
     * @param count
     */
    public MyItem(double lat, double lng, Bitmap b, String title, String time, Long id, int count) {
        mPosition = new LatLng(lat, lng);
        mImage = b;
        mTitle = title;
        mTime = time;
        mId = id;
        mCount = count;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
    
    /**
     * 
     * @return
     */
    public Long getID() {
        return mId;
    }
    
    /**
     * 
     * @return
     */
    public int getCount() {
        return mCount;
    }

    /**
     * 
     * @return
     */
    public Bitmap getBitmap() {
        return mImage;
    }
    
    /**
     * 
     * @return
     */
    public String getTitle() {
        return mTitle;
    }
    

    /**
     * 
     * @return
     */
    public String getTime() {
        return mTime;
    }

}
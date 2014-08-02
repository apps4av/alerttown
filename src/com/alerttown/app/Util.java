package com.alerttown.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * 
 * @author zkhan
 * Misc helper functions
 */
public class Util {

    
    /**
     * A routine to find how long ago the event occured represented by this marker item
     * @param ctx
     * @return
     */
    public static String getTimeAgo(Context ctx, String mtime) {
        
        Date then = null;
        
        try {
            then = Util.fromISODateString(mtime);
        } catch (Exception e) {
        }
        
        String ret = "";
        if(then != null) {
            long diff = System.currentTimeMillis() - then.getTime();
            if(diff >= 0) {
                // only need hours and minutes
                diff /= 60000; // minutes
                long hr = diff / 60;
                if(hr < 0) {
                    hr = 0;
                }
                long mn = diff % 60;
                if(mn < 0) {
                    mn = 0;
                }
                ret = hr + " hours ";
                ret += mn + " minutes ";
                ret += ctx.getString(R.string.ago);
            }
        }
        
        return ret;
    }
    /**
     * 
     * @param isoDateString
     * @return
     * @throws Exception
     */
    public static Date fromISODateString(String isoDateString) throws Exception {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("Zulu"));
            return f.parse(isoDateString);
    }
    
    /**
     * Check if network is available
     * @param ctx
     * @return
     */
    public boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    
    /**
     * Routine that uses web service for geo location to find a list of addresses
     * @param address
     * @return
     */
    public static List<Address> getGeoPoint(String address) {
        
        int LIMIT = 5;

        // Send a address search string like 105 wood
        List<Address> list = new ArrayList<Address>();
        String addr;
        try {
            addr =  URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            return list;
        }
        
        // geolocation service query
        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?address=" + addr + "&ka&sensor=false");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        // it returns in JSON, parse
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            return list;
        }

        // Dont return too many results
        for(int i = 0; i < LIMIT; i++) {
            double lon = 0;
            double lat = 0;
            String formatted = null;
            try {
    
                lon = ((JSONArray)jsonObject.get("results")).getJSONObject(i)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");
    
                lat = ((JSONArray)jsonObject.get("results")).getJSONObject(i)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");
    
                formatted = ((JSONArray)jsonObject.get("results")).getJSONObject(i)
                        .getString("formatted_address");
    
            } catch (JSONException e) {
                return list;
            }
            
            // got a suggestion
            if(formatted != null) {
                // set first line as suggestion, also populate its coordinates
                Address a = new Address(Locale.getDefault());
                a.setLongitude(lon);
                a.setLatitude(lat);
                a.setAddressLine(0, formatted);
                list.add(a);
            }
        }

        return list;
    }

    
    /**
     * 
     * @return
     */
    public static Location getLastLocation(Context ctx) {
        LocationManager lm = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);

        if(null == lm) {
            return null;
        }

        List<String> providers = lm.getProviders(true);

        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) {
                break;
            }
        }
        return l;
    }
    
    /**
     * Device's phone number
     * @param ctx
     * @return
     */
    public static String getPhoneNumber(Context ctx) {
        TelephonyManager tMgr = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }
}

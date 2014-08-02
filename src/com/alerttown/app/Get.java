package com.alerttown.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;

/**
 * 
 * @author zkhan
 * This class starts a task to get event data from the internet
 */
public class Get extends Observable {

    private static GetTask mGetTask;
    
    /**
     * Start the task
     * @param time
     */
    public void getIt(long time) {
        
        if(null != mGetTask) {
            if(mGetTask.getStatus() == AsyncTask.Status.RUNNING) {
                mGetTask.cancel(true);
            }
        }
        mGetTask = new GetTask();
        mGetTask.execute(time);
    }
    
    /**
     * Get a list of events
     * @param time
     * @return
     */
    private LinkedList<Event> getData(long time) {
        
        String result = null;
        StringBuilder sb = null;
        InputStream is = null;
        LinkedList<Event> events = new LinkedList<Event>();
        time += 2; // min 2 minutes margin
        long minutes = time % 60;
        long hours = time / 60;

        /*
         * Get incremental report since we last got it
         */
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("hours", "" + hours));
        nameValuePairs.add(new BasicNameValuePair("minutes", "" + minutes));
        nameValuePairs.add(new BasicNameValuePair("key", "" + Constants.PASSWORD));
        
        //http post
        try{
             HttpClient httpclient = new DefaultHttpClient();

             HttpPost httppost = new HttpPost(Constants.SERVER + "get_new.php");
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
             HttpResponse response = httpclient.execute(httppost);
             HttpEntity entity = response.getEntity();
             is = entity.getContent();
        }
        catch(Exception e) {
        }
        
        //convert response to string
        try{
              BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
               sb = new StringBuilder();
               sb.append(reader.readLine() + "\n");

               String line="0";
               while ((line = reader.readLine()) != null) {
                              sb.append(line + "\n");
               }
               is.close();
               result=sb.toString();
        }
        catch(Exception e){
        }

        try{
            // Now parse events and make a list, PHP sends \n seprated events
            String lines[] = result.split("\n");
            for(int i=0; i < lines.length; i++) {
                
                Event e = new Event(lines[i]);
                if(e.valid) {
                    events.add(e);
                }
            }
        }
        catch(Exception e) {
        } 
        
        return events;
    }
    
    /**
     * @author zkhan
     *
     */
    private class GetTask extends AsyncTask<Object, String, String> {
        
        private LinkedList<Event> mFound = null;
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */     
        @Override
        protected String doInBackground(Object... vals) {
            
            Thread.currentThread().setName("Get");
            mFound = getData((Long)vals[0]);
            
            return null;
        }
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String something) {
            // notify the service that new events are available
            Get.this.setChanged();
            Get.this.notifyObservers(mFound);
        }
    }
}

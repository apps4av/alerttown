package com.alerttown.app;

import java.util.ArrayList;
import java.util.Observable;

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
 * 
 * A class to start a task to send a report
 *
 */
public class Put extends Observable {

    private static PutTask mPutTask;
    
    private static Report mLastReport;
    
    /**
     * Start the task
     * @param c
     */
    public void putIt(Report c) {
        
        if(null != mPutTask) {
            if(mPutTask.getStatus() == AsyncTask.Status.RUNNING) {
                mPutTask.cancel(true);
            }
        }
        mPutTask = new PutTask();
        mPutTask.execute(c);
    }
    
    /**
     * Actual call to put the data on the server
     * @param c
     * @return
     */
    private boolean putData(Report c) {
        
        // Send the report.
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("type", c.type));
        nameValuePairs.add(new BasicNameValuePair("lon", c.lon));
        nameValuePairs.add(new BasicNameValuePair("lat", c.lat));
        nameValuePairs.add(new BasicNameValuePair("reported", c.reported));
        nameValuePairs.add(new BasicNameValuePair("username", c.username));
        nameValuePairs.add(new BasicNameValuePair("notes", c.notes));
        nameValuePairs.add(new BasicNameValuePair("uuid", c.uuid));
        nameValuePairs.add(new BasicNameValuePair("key", "" + Constants.PASSWORD));

        
        //http post
        try{
             HttpClient httpclient = new DefaultHttpClient();

             HttpPost httppost = new HttpPost(Constants.SERVER + "put_new.php");
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
             httpclient.execute(httppost);
        }
        catch(Exception e) {
            // This flag is needed to find if the report needs to be stored for xmission later
            c.submitted = false;
            return false;
        }
        
        c.submitted = true;
        return true;
    }


    /**
     * @author zkhan
     *
     */
    private class PutTask extends AsyncTask<Object, String, Boolean> {
        
        
        private Report report;
        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */     
        @Override
        protected Boolean doInBackground(Object... vals) {
            
            report = (Report)vals[0];
            Thread.currentThread().setName("Put");
            return putData(report);
        }
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Boolean result) {
            // notify the sender so user can be informed
            mLastReport = report;
            Put.this.setChanged();
            Put.this.notifyObservers(report);
        }
    }
    
    /**
     * 
     * @return
     */
    public static Report getLastReport() {
        return mLastReport;
    }
}

package com.alerttown.app;

import java.util.Observable;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

/**
 * 
 * @author zkhan
 *
 */
public class AddressToGps extends Observable {

    
    AddressTask mAddressTask;
    
    /**
     * Start a task to get resolution 
     * @param ctx
     * @param address
     * @return
     */
    public Location get(Context ctx, String address) {  
        if(null != mAddressTask) {
            mAddressTask.cancel(true);
        }
        mAddressTask = new AddressTask();
        mAddressTask.execute(ctx, address);

        return null;
    }
   
    /**
     * @author zkhan
     *
     */
    private class AddressTask extends AsyncTask<Object, String, Object> {
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */     
        @Override
        protected Object doInBackground(Object... vals) {
            
            Thread.currentThread().setName("Address");
            
            return Util.getGeoPoint((String)vals[1]);
        }
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Object arg) {
            /*
             * Set list view as return address list
             */
            AddressToGps.this.setChanged();
            AddressToGps.this.notifyObservers(arg);
        }
    }

    

}


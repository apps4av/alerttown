package com.alerttown.app;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v4.app.NotificationCompat;

/**
 * 
 * @author zkhan
 * A class that puts events on the notification bar
 */
public class NotificationBar {

    private Context mContext;
    
    /**
     * 
     * @param ctx
     */
    public NotificationBar(Context ctx) { 
        mContext = ctx;
        
    }

    /**
     * To report an event, we must be within certain location of the event occurance
     * @param e
     */
    public void notifyCrime(Event e) {

        // prepare intent which is triggered if the
        // notification is selected
        
        NotificationManager notificationManager = 
                (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);

        /*
         * Alert!
         * Make intent to MainActitivty, and send info about this
         */
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("location", e.lon + "," + e.lat);
        intent.putExtra("id", e.id);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                .setSmallIcon(CrimeAdapter.getIconResource(e.type))
                .setContentTitle(e.type)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentText(
                        ((float)Math.round(e.getDistance() * 10.f) / 10.f) + " " + mContext.getString(R.string.miles_away));
            
            
        Notification n = builder.build();
        
        notificationManager.notify((int)e.id, n); 
        
        // also make a tone to alert the user

        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP);
        
    }
        
    
    /**
     * Removes the notification if needed
     * @param id
     */
    public  void cancelNotification(int id) {
        NotificationManager notificationManager = 
                (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    
}

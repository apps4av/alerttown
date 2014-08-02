package com.alerttown.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Start the app at boot
 * @author zkhan
 *
 */
public class StartServiceOnBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, StorageService.class);
            context.startService(serviceIntent);
        }
    }

}

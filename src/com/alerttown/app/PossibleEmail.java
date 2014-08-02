package com.alerttown.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * @author zkhan
 * Find user's google email from account manager
 *
 */
public class PossibleEmail {
    
    /**
     * 
     * @param ctx
     * @return
     */
    public static String get(Context ctx) {
        try{
            Account[] accounts = AccountManager.get(ctx).getAccountsByType("com.google");
              return accounts[0].name;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

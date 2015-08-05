package com.everad.bannerapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class PhoneUnlockedBroadcastReceiver extends BroadcastReceiver {
    private static final long BANNER_SHOW_DELAY = 5000/*TimeUnit.MINUTES.toMillis(1)*/;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            BannerShowBroadcastReceiver.sendPendingBroadcastForBannerShow(context, BANNER_SHOW_DELAY);
        }
    }
}
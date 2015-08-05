package com.everad.bannerapp.receivers;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.everad.bannerapp.ActivityVisibilityHolder;
import com.everad.bannerapp.BannerActivity;
import com.everad.bannerapp.TimeUtils;

import java.util.concurrent.TimeUnit;

/**
 * BroadcastReceiver to make decision to show or not banner after delay
 */
public class BannerShowBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_SHOW_BANNER = "com.everad.bannerapp.ACTION_SHOW_BANNER";

    private static final String PREFS_FILE_NAME = "BannerPrefs";
    private static final String PREFS_KEY_DATE = "PREFS_KEY_DATE";
    private static final String PREFS_KEY_SHOWS_COUNT = "PREFS_KEY_SHOWS_COUNT";

    private static final long BANNER_RETRY_DELAY = TimeUnit.MINUTES.toMillis(5);
    private static final int BANNER_SHOW_LIMIT = 3;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION_SHOW_BANNER.equals(intent.getAction())) {
            if (!ActivityVisibilityHolder.isActivityVisible()) {
                KeyguardManager keyguardManager =
                        (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                boolean screenLocked = keyguardManager.inKeyguardRestrictedInputMode();

                if (!screenLocked && !tryToExceedDaylyLimit(context)) {
                    Log.i("BannerShowBroadcast", "show activity");
                    Intent activityIntent = new Intent(context, BannerActivity.class);
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(activityIntent);
                }
            } else {
                sendPendingBroadcastForBannerShow(context, BANNER_RETRY_DELAY);
            }
        }
    }

    /**
     * Makes 2 actions: checks condition about {@link #BANNER_SHOW_LIMIT}. If it is not exceeded, puts
     * necessary notes to {@link SharedPreferences}
     *
     * @param context context
     * @return true, if current time matches to time of last banner show
     */
    private boolean tryToExceedDaylyLimit(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        long dateTime = preferences.getLong(PREFS_KEY_DATE, 0);
        long currentTime = System.currentTimeMillis();
        boolean limitExceeded = false;
        SharedPreferences.Editor editor = preferences.edit();
        if (TimeUtils.areTimestampsFromSameDay(currentTime, dateTime)) {
            int count = preferences.getInt(PREFS_KEY_SHOWS_COUNT, 0);
            if (count < BANNER_SHOW_LIMIT) {
                editor.putInt(PREFS_KEY_SHOWS_COUNT, ++count);
            } else {
                limitExceeded = true;
            }
        } else {
            editor.putLong(PREFS_KEY_DATE, currentTime);
            editor.putInt(PREFS_KEY_SHOWS_COUNT, 1);
        }
        editor.commit();
        return limitExceeded;
    }

    /**
     * Starts pending intent to launch BroadcastReceiver in moment to show banner.
     *
     * @param context context
     * @param delay time to delay in millis
     */
    /*package*/ static void sendPendingBroadcastForBannerShow(Context context, long delay) {
        Intent bannerIntent = new Intent(BannerShowBroadcastReceiver.ACTION_SHOW_BANNER);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, bannerIntent, 0);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, pendingIntent);
    }
}

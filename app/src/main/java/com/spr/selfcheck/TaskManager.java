package com.spr.selfcheck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

public class TaskManager {
    private final Context context;
    private final Intent intent;
    private AlarmManager alarmManager;
    private PendingIntent myPendingIntent;
    private BroadcastReceiver myBroadcastReceiver;
    private FirebaseAccess access;

    public TaskManager(Context context) {
        this.context = context;
        intent = new Intent("com.spr.selfcheck");
    }

    public void logout_schedule(long timeInMillisecond) {
        myBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Logout employee
                long currentTime = System.currentTimeMillis();
                if (currentTime > 86000000 && currentTime < 86200000) {
                    access = new FirebaseAccess();
                    access.logout_all(context);
                }
            }
        };
        context.registerReceiver(myBroadcastReceiver, new IntentFilter("com.spr.selfcheck"));
        myPendingIntent = PendingIntent.getBroadcast(context, 9, intent, 0);
        alarmManager = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillisecond, AlarmManager.INTERVAL_DAY,myPendingIntent);
    }

    public void unregister_schedule() {
        alarmManager.cancel(myPendingIntent);
        context.unregisterReceiver(myBroadcastReceiver);
    }

}

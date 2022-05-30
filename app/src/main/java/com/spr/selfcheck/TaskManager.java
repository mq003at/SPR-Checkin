package com.spr.selfcheck;

import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TaskManager {
    long timeTrigger;

    Context context;
    Intent intent;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    public TaskManager(Context context) {
        this.context = context; Log.e("Created", "yes");
    }

    public void setLogoutAtNight(){
        Log.e("Generated", "yes");
        intent = new Intent(context, TaskManager.LogoutAtNight.class);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 55);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }
        timeTrigger = calendar.getTimeInMillis();
        pendingIntent = PendingIntent.getBroadcast(context, (int) timeTrigger, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeTrigger, pendingIntent);
    }

    public void setGenerateTodayLogAtNight() {
        intent = new Intent(context, TaskManager.GenerateLogAtNight.class);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }
        timeTrigger = calendar.getTimeInMillis();
        pendingIntent = PendingIntent.getBroadcast(context, (int) timeTrigger, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeTrigger, pendingIntent);

    }

    public void unregisterGenerateTodayLogAtNight(){

    }

    static public class LogoutAtNight extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(4000);
            FirebaseAccess access = new FirebaseAccess();
//            access.logout_all(context);
        }
    }

    static public class GenerateLogAtNight extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Date date = new Date();
            String dateStamp = new SimpleDateFormat("yyyyMMdd").format(date);
            FirebaseAccess access = new FirebaseAccess();
//            access.generate_logs(context, dateStamp);
        }
    }


}
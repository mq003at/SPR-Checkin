package com.spr.selfcheck;

import static android.content.Context.ALARM_SERVICE;

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
    int intTimeTriggered;

    Context context;
    Intent intent;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    public TaskManager(Context context) {
        this.context = context;
    }

    public long calendarGenerator(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }
        Log.e("Time: ", String.valueOf(calendar.getTimeInMillis()));
        return calendar.getTimeInMillis();
    }

    public void setLogoutAtNight(){
        timeTrigger = calendarGenerator(23, 58, 0);
        intent = new Intent(context, TaskManager.LogoutAtNight.class);
        intent.putExtra("time_triggered", String.valueOf(timeTrigger));
        pendingIntent = PendingIntent.getBroadcast(context, (int) timeTrigger, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeTrigger, pendingIntent);
        Log.e("TimeLogoutToSet", String.valueOf(timeTrigger));

    }

    public void setGenerateTodayLogAtNight() {
        timeTrigger = calendarGenerator(23, 58, 0);
        intent = new Intent(context, TaskManager.GenerateLogAtNight.class);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("time_triggered", timeTrigger);
        pendingIntent = PendingIntent.getBroadcast(context, (int) timeTrigger, intent, 0);
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeTrigger, pendingIntent);
    }

    public void unregisterGenerateTodayLogAtNight(Context context){
        timeTrigger = calendarGenerator(23, 58, 0);
        intent = new Intent(context, TaskManager.GenerateLogAtNight.class);
        intent.putExtra("time_triggered", timeTrigger);
        pendingIntent = PendingIntent.getBroadcast(context, (int) timeTrigger, intent, 0);
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    static public class LogoutAtNight extends BroadcastReceiver {
        long longTimeTriggered;
        long currentTime;
        @Override
        public void onReceive(Context context, Intent intent) {
            currentTime = System.currentTimeMillis();
            longTimeTriggered = Long.parseLong(intent.getExtras().getString("time_triggered"));
            if ((new InternetHandler(context).checkForInternet() && ((currentTime - longTimeTriggered) < 300))) {
                // Vibrator not work when device sleep
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(4000);
                FirebaseAccess access = new FirebaseAccess();
                access.logout_all(context);
            }
        }
    }

    static public class GenerateLogAtNight extends BroadcastReceiver {
        long longTimeTriggered;
        long currentTime;
        @Override
        public void onReceive(Context context, Intent intent) {
            currentTime = System.currentTimeMillis();
            longTimeTriggered = Long.parseLong(intent.getExtras().getString("time_triggered"));
            if ((new InternetHandler(context).checkForInternet() && ((currentTime - longTimeTriggered) < 300))) {
                // Vibrator not work when device sleep
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(4000);
                Date date = new Date();
                String dateStamp = new SimpleDateFormat("yyyyMMdd").format(date);
                FirebaseAccess access = new FirebaseAccess();
                access.generate_logs(context, dateStamp);
            }
        }
    }
}
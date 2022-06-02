package com.spr.selfcheck;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class FirebaseAccess {
    final private DatabaseReference shopReference;
    final private DatabaseReference eventReference;

    public String eName;
    public String employeeID;

    public interface String_Callback {
        void onSuccess(String value);
    }

    public interface List_String_Callback {
        void onSuccess(ArrayList<ArrayList<String>> value);
    }

    public FirebaseAccess() {
        FirebaseDatabase dbInstance = FirebaseDatabase.getInstance();
        eventReference = dbInstance.getReference().child("shop_events/b4b8bb4ceeaa2aee/authorised_id/");
        shopReference = dbInstance.getReference().child("shop_data/b4b8bb4ceeaa2aee/employee_data/");
        employeeID = "";
    }


    public void update_state(String id, String direction) {
        eventReference.child(id + "/actual_state").setValue(direction);
    }

    public void add_record(String id, String documentStamp, String timeStamp, int dateStamp, String direction) {
        EventLog record = new EventLog(dateStamp, direction, timeStamp);
        eventReference.child(id + "/log_events/" + documentStamp).setValue(record);
    }

    public void self_check(String id, String documentStamp, String timeStamp, int dateStamp, String realtime, Context context) {
        TextView txtPlaceholderTwo = ((Activity) context).findViewById(R.id.txtPlaceholderTwo);
        eventReference.child(id + "/actual_state").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                txtPlaceholderTwo.setText(context.getString(R.string.get_database_error, task.getException()));
            } else {
                String direction = String.valueOf(task.getResult().getValue());
                String strDirection;
                if (direction.equals("in")) {
                    direction = "out";
                    strDirection = context.getString(R.string.logout);
                } else {
                    direction = "in";
                    strDirection = context.getString(R.string.login);
                }
                update_state(id, direction);
                add_record(id, documentStamp, timeStamp, dateStamp, direction);
                txtPlaceholderTwo.setText(context.getString(R.string.direction_employee, eName, strDirection, realtime));
                new Handler().postDelayed(() -> {
                    // Refresh
                    txtPlaceholderTwo.setText("");
                }, 10000);
            }
        });
    }

    public void output_name(String id, Context context) {
        get_name(id, context, name -> {
            TextView txtPlaceholderOne = ((Activity) context).findViewById(R.id.txtPlaceholderOne);
            txtPlaceholderOne.setText(context.getString(R.string.hello_employee, name));
        });
    }

    public void get_name(String id, Context context, final String_Callback callback) {
        shopReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String snapKey = snap.getKey();
                    Query query = shopReference.child(snapKey + "/employees/").orderByChild("tag_id").equalTo(id);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String[] arrEmp = snapshot.getValue().toString().split("=", 5);
                                String[] employeeName = arrEmp[2].split(",");
                                eName = employeeName[0];
                                employeeID = id;
                                callback.onSuccess(eName);
                            }
//                                    Toast.makeText(context, R.string.no_tag_on_database, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, context.getString(R.string.get_database_error, error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, context.getString(R.string.get_database_error, error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get data of all employee working on a specific day (String today)
    public void get_employee_data(Context context, String today, final String_Callback string_callback) {

        eventReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String snapKey = snap.getKey();
                    Query query = eventReference.child(snapKey + "/log_events").orderByChild("dateStamp").equalTo(Integer.parseInt(today));
                    query.addValueEventListener(new ValueEventListener() {
                        ArrayList<ArrayList<ArrayList<String>>> listData = new ArrayList<>();

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                employeeID = snap.getKey();
                                ArrayList<ArrayList<String>> employeeData = new ArrayList<>();
                                int ArrayPosition = 0;

                                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                                    int finalArrayPosition = ArrayPosition;
                                    String[] arrEvt = eventSnap.getValue().toString().split(",", 5);
                                    String timeStamp = (arrEvt[0].split("="))[1];
                                    if (timeStamp.length() < 13) timeStamp = timeStamp + "00";
                                    String timeStampOutput = timeStamp.substring(8, 10) + ":" + timeStamp.substring(10,12) + ":" + timeStamp.substring(12);
                                    String direction = ((arrEvt[2].split("="))[1]).replace("}", "");

                                    employeeData.add(new ArrayList<>(Arrays.asList("", "")));
                                    employeeData.get(finalArrayPosition).set(0, direction);
                                    employeeData.get(finalArrayPosition).set(1, timeStampOutput);
                                    ArrayPosition++;
                                }

                                get_name(employeeID, context, name -> {
                                    ArrayList n = new ArrayList<>(Arrays.asList(name));
                                    ArrayList id = new ArrayList<>(Arrays.asList(employeeID));
                                    employeeData.add(0, id);
                                    employeeData.add(1, n);
                                    string_callback.onSuccess(employeeData.toString());
                                });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Function to generate logs
    public void generate_logs(Context context, String today) {
        IOStream writeTool = new IOStream(context, today, "spr-backup-");
        get_employee_data(context, today, employeeData -> {
            writeTool.writeToFile(employeeData.replace("[", "").replace("]", "") + "\n\n");
        });
    }

    // Logout all employee
    public void logout_all(Context context) {
        Query query = eventReference.orderByChild("actual_state").equalTo("in");
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    StringBuilder listNotLoggedOut = new StringBuilder();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        update_state(snap.getKey(), "out");
                        listNotLoggedOut.append(snap.getKey()).append(snap.getKey()). append(" ");
                    }
                    NotificationMaker notify = new NotificationMaker();
                    notify.alertInEmployee(context, context.getString(R.string.alert_not_logout, listNotLoggedOut.toString()), new Intent(context, FirebaseAccess.class), 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}

package com.spr.selfcheck;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;

public class FirebaseAccess {
    final private DatabaseReference shopReference;
    final private DatabaseReference eventReference;
    final private DatabaseReference pinReference;
    final private DatabaseReference todoReference;
    final private String shop = "b4b8bb4ceeaa2aee";

    public String eName;
    public String employeeID;
    private String storePasscode;
    private String error;
    private String[] empInfo;

    public interface String_Callback {
        void onSuccess(String value);
    }

    public interface List_String_Callback {
        void onSuccess(ArrayList<ArrayList<String>> value);
    }

    public FirebaseAccess() {
        FirebaseDatabase dbInstance = FirebaseDatabase.getInstance();
        eventReference = dbInstance.getReference().child("shop_events/" + shop + "/authorised_id/");
        shopReference = dbInstance.getReference().child("shop_data/" + shop + "/employee_data/");
        pinReference = dbInstance.getReference().child("shop_data/" + shop + "/pin/");
        todoReference = dbInstance.getReference().child("shop_events/" + shop + "/todo_data/");
        employeeID = "";
    }

    public String storePasscode() {
        return storePasscode;
    }

    public String eName() {
        return eName;
    }

    public String error() {
        return error;
    }

    public void get_store_passcode(Context context) {
        pinReference.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                error = context.getString(R.string.get_database_error, task.getException());
                storePasscode = null;
            }
            else {
                storePasscode = String.valueOf(task.getResult().getValue());
            };
        });
    }

    public void update_state(String id, String direction) {
        eventReference.child(id + "/actual_state").setValue(direction);
    }

    public void add_record(String id, String documentStamp, String timeStamp, int dateStamp, String direction) {
        EventLog record = new EventLog(dateStamp, direction, timeStamp);
        eventReference.child(id + "/log_events/" + documentStamp).setValue(record);
    }

    public void add_todo(String text, boolean check, String recipient, String documentStamp, String dateStamp, String dateS) {
        // Add record
        TodoRecord todoRecord = new TodoRecord(dateS, dateStamp, recipient, check, text);
        todoReference.child(documentStamp + "Todo").setValue(todoRecord);
    }

    public void self_check(String id, String documentStamp, String timeStamp, int dateStamp, String realtime, Context context) {
        TextView txtPlaceholderTwo = ((Activity) context).findViewById(R.id.txtPlaceholderTwo);
        eventReference.child(id + "/actual_state").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                txtPlaceholderTwo.setText(context.getString(R.string.get_database_error, task.getException()));
            } else {
                String newStamp = documentStamp + employeeID;
                String direction = String.valueOf(task.getResult().getValue());
                String strDirection;
                if (direction.equals("in")) {
                    direction = "out";
                    newStamp += "0";
                    strDirection = context.getString(R.string.logout);
                } else {
                    direction = "in";
                    newStamp += "1";
                    strDirection = context.getString(R.string.login);
                }
                update_state(id, direction);
                add_record(id, newStamp, timeStamp, dateStamp, direction);
                txtPlaceholderTwo.setText(context.getString(R.string.direction_employee, eName, strDirection, realtime));
                new Handler().postDelayed(() -> {
                    // Refresh
                    txtPlaceholderTwo.setText(R.string.reminder_logout);
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
        shopReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String snapKey = snap.getKey();
                    Query query = shopReference.child(snapKey + "/employees/").orderByChild("tag_id").equalTo(id);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String[] arrEmp = snapshot.getValue().toString().split("=", 5);
                                String[] employeeName = arrEmp[2].split(",");
                                eName = employeeName[0];
                                employeeID = id;
                                callback.onSuccess(eName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, context.getString(R.string.get_database_error, error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (eName == null) Toast.makeText(context, R.string.no_tag_on_database, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, context.getString(R.string.get_database_error, error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get data of all employee working on a specific day (String today)
    public void get_employee_data(Context context, String today, final String_Callback string_callback) {

        eventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String snapKey = snap.getKey();
                    Query query = eventReference.child(snapKey + "/log_events").orderByChild("dateStamp").equalTo(Integer.parseInt(today));
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
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
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    StringBuilder listNotLoggedOut = new StringBuilder();
                    Date date = new Date();
                    String timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(date);
                    String documentStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
                    String dateStamp = new SimpleDateFormat("yyyyMMdd").format(date);
                    String dateTodo = new SimpleDateFormat("dd-MM-yyyy").format(date);
                    int dateS = Integer.parseInt(dateStamp);

                    for (DataSnapshot snap : snapshot.getChildren()) {
                        update_state(snap.getKey(), "out");
                        add_record(snap.getKey(), documentStamp, timeStamp, dateS,"out");
                        listNotLoggedOut.append(snap.getKey()).append(" ");
                    }

                    NotificationMaker notify = new NotificationMaker();
                    String alert = context.getString(R.string.alert_not_logout, listNotLoggedOut.toString());
                    notify.alertInEmployee(context, context.getString(R.string.logout_report), alert, new Intent(context, FirebaseAccess.class), 1);
                    add_todo(alert, false, "Opastajat", documentStamp, dateStamp, dateTodo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}

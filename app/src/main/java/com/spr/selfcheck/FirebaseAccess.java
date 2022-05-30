package com.spr.selfcheck;

import android.app.Activity;
import android.content.Context;
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

    public interface String_Callback{
        void onSuccess(String value);
    }
    public interface List_String_Callback{
        void onSuccess(ArrayList<ArrayList<String>> value);
    }

    public FirebaseAccess()
    {
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
             }
             else {
                 String direction = String.valueOf(task.getResult().getValue());
                 String strDirection;
                 if (direction.equals("in")) {direction = "out"; strDirection = context.getString(R.string.logout);}
                 else {direction = "in"; strDirection = context.getString(R.string.login);}
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

     public void get_name(String id, Context context, final String_Callback callback) {
         TextView txtPlaceholderOne = ((Activity) context).findViewById(R.id.txtPlaceholderOne);
         txtPlaceholderOne.setText(R.string.scanning_load);
         shopReference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap: snapshot.getChildren()){
                        String snapKey = snap.getKey();
                        Query query = shopReference.child(snapKey + "/employees/").orderByChild("tag_id").equalTo(id);
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        String[] arrEmp = snapshot.getValue().toString().split("=", 5);
                                        String[] employeeName = arrEmp[2].split(",");
                                        eName = employeeName[0];
                                        employeeID = id;
                                        callback.onSuccess(eName);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    txtPlaceholderOne.setText(context.getString(R.string.get_database_error, error));
                                }
                            });
                    }
                 txtPlaceholderOne.setText(R.string.no_tag_on_database);
             }
             @Override
             public void onCancelled(@NonNull DatabaseError error) {
                 txtPlaceholderOne.setText(context.getString(R.string.get_database_error, error));
             }
         });
    }

    // Get data of all employee working on a specific day (String today)
    public void get_employee_data(Context context, String today, final String_Callback string_callback) {

        eventReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap: snapshot.getChildren()) {

                    String snapKey = snap.getKey();
                    Query query = eventReference.child(snapKey + "/log_events").orderByChild("dateStamp").equalTo(Integer.parseInt(today));
                    query.addValueEventListener(new ValueEventListener() {
                        ArrayList<ArrayList<ArrayList<String>>> listData = new ArrayList<>();
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                IOStream writeTool = new IOStream(context, today);
                                String docData = null;
                                String employeeID = snapKey;

                                ArrayList<ArrayList<String>> employeeData = new ArrayList<>();
                                ArrayList employeeName = new ArrayList();
                                int ArrayPosition = 0;

                                for (DataSnapshot eventSnap: snapshot.getChildren()) {
                                    int finalArrayPosition = ArrayPosition;
                                    String documentStamp = eventSnap.getKey();
                                    String[] arrEvt = eventSnap.getValue().toString().split(",", 5);
                                    String timeStamp = (arrEvt[0].split("="))[1];
                                    String dateStamp = (arrEvt[1].split("="))[1];
                                    String direction = ((arrEvt[2].split("="))[1]).replace("}", "");

                                    employeeData.add(new ArrayList<>(Arrays.asList("","","")));
                                    employeeData.get(finalArrayPosition).set(0, employeeID);
                                    employeeData.get(finalArrayPosition).set(1, direction);
                                    employeeData.get(finalArrayPosition).set(2, timeStamp);
                                    ArrayPosition++;
                                }

                                get_name(employeeID, context, name -> {
                                    ArrayList a = new ArrayList();
                                    a.add(name);
                                    employeeData.add(0, a);
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
    public void generate_logs(Context context, String today){
        get_employee_data(context, today, employeeData -> {
                IOStream writeTool = new IOStream(context, today);
                String dataToWrite = employeeData;
    //            writeTool.writeToFile(dataToWrite);
    //            Log.e("data", dataToWrite);
            Log.e("data", dataToWrite);
        });
    }

    // Logout all employee
    public void logout_all(Context context){
        TextView txtPlaceholderTwo = ((Activity) context).findViewById(R.id.txtPlaceholderTwo);
        Query query = eventReference.orderByChild("actual_state").equalTo("in");
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    StringBuilder listNotLoggedOut = new StringBuilder();
                    for (DataSnapshot snap: snapshot.getChildren()) {
                        update_state(snap.getKey(), "out");
                        listNotLoggedOut.append(snap.getKey()).append(" ");
                    }
                    txtPlaceholderTwo.setText(context.getString(R.string.alert_not_logout, listNotLoggedOut.toString()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.spr.selfcheck;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.DTDHandler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class FirebaseAccess {
    final private DatabaseReference shopReference;
    final private DatabaseReference eventReference;
    final private FirebaseDatabase dbInstance;

    public String eName;
    public String employeeID;

    public FirebaseAccess()
    {
        dbInstance = FirebaseDatabase.getInstance();
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
         eventReference.child(id + "/actual_state").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
             @Override
             public void onComplete(@NonNull Task<DataSnapshot> task) {
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
                 }
             }
         });
     }

     public void get_name(String id, Context context) {
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
                                        txtPlaceholderOne.setText(context.getString(R.string.hello_employee) + eName + ".");
                                        return;
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

    // Generating log each day, at 23.59
    public void generate_logs(Context context, String today) {

        eventReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap: snapshot.getChildren()) {

                    String snapKey = snap.getKey();
                    Query query = eventReference.child(snapKey + "/log_events").orderByChild("dateStamp").equalTo(Integer.parseInt(today));
                    query.addValueEventListener(new ValueEventListener() {
                        String dataBackup = "";
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {
                                IOStream writeTool = new IOStream(context, today);
                                String docData = null;
                                String employeeID = snapKey;

                                for (DataSnapshot eventSnap: snapshot.getChildren()) {
                                    String documentStamp = eventSnap.getKey();
                                    String[] arrEvt = eventSnap.getValue().toString().split(",", 5);
                                    String timeStamp = (arrEvt[0].split("="))[1];
                                    String dateStamp = (arrEvt[1].split("="))[1];
                                    String direction = (arrEvt[2].split("="))[1];
                                    direction = direction.replace("}", "");

                                    docData = employeeID + "-" + documentStamp + "-" + timeStamp + "-" + dateStamp + "-" + direction + ".\n";
                                    writeTool.writeToFile(docData);
                                }
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

    // Deleting records that more than 50 days old
    public void delete_old_records(String cutoff) {

    }



}

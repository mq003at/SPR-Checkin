package com.spr.selfcheck;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;


import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;

import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    NfcAdapter nfcAdapter;
    TextView txtPlaceholderOne;
    TextView txtPlaceholderTwo;
    Button btnLogIn;
    Button btnWrite;
    ImageButton btnLocale;
    ImageButton btnLocale2;
    ImageButton btnLocale3;
    CheckBox checkBoxLog;
    FirebaseAccess access;
    TaskManager sprManager;

    SharedPreferences sharedPreferences;
    String currentLocale;


    String documentStamp, dateStamp, timeStamp, realtime;
    int dateS;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Generating layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        Objects.requireNonNull(getSupportActionBar()).hide();


        // localStorage init
        sharedPreferences = getSharedPreferences("SPR", MODE_PRIVATE);
        if (sharedPreferences.contains("locale")) currentLocale = sharedPreferences.getString("locale", "");
        else {
            currentLocale = "fi";
            sharedPreferences.edit().putString("locale", "fi").apply();
        }
        LocaleHelper.setLocale(this, currentLocale);

        // UI Init
        txtPlaceholderOne = findViewById(R.id.txtPlaceholderOne);
        txtPlaceholderTwo = findViewById(R.id.txtPlaceholderTwo);
        txtPlaceholderOne.setText(R.string.card_request);
        txtPlaceholderTwo.setText(R.string.reminder_logout);

        btnLogIn = findViewById(R.id.btnLogIn);
        btnLocale = findViewById(R.id.btnLocale);
        btnLocale2 = findViewById(R.id.btnLocale2);
        btnLocale3 = findViewById(R.id.btnLocale3);
        btnLogIn.setText(R.string.in_out);
        btnWrite = findViewById(R.id.btnWrite);
        btnWrite.setText(R.string.write_nfc_tags);
        checkBoxLog = findViewById(R.id.checkBoxLog);

        // NFC Init
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            txtPlaceholderOne.setText(R.string.no_nfc_support);
        }
        if (!nfcAdapter.isEnabled()) {
            txtPlaceholderOne.setText(R.string.nfc_disable_alert);
        }

        access = new FirebaseAccess();
        sprManager = new TaskManager(this);

        // Button
        btnWrite.setOnClickListener(v -> {
            Intent goWriteNfc = new Intent(v.getContext(), NfcWriteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("locale", currentLocale);
            goWriteNfc.putExtras(bundle);
            startActivity(goWriteNfc);
        });

        btnLogIn.setOnClickListener(view -> {
            if (access.employeeID.equals("")) txtPlaceholderOne.setText(R.string.card_request);
            else {

                // Time init, scheduledTime equivalent to 23:55.
                Date date = new Date();
                documentStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
                timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(date);
                dateStamp = new SimpleDateFormat("yyyyMMdd").format(date);
                dateS = Integer.parseInt(dateStamp);
                realtime = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(date);

                // Database info update
                access.self_check(access.employeeID, documentStamp, timeStamp, dateS, realtime, this);

                // Logging out people (if not set yet)
                sprManager.setLogoutAtNight();

                // Making log files in case the website breaks (only activate when button checked)
                if (checkBoxLog.isChecked()) sprManager.setGenerateTodayLogAtNight();

                // More codes
                access.logout_all(this);
            }
        });

        // Log generating for that day (uncheck will cancel log generating)
        checkBoxLog.setOnClickListener(v -> {
            if (!checkBoxLog.isChecked()) sprManager.unregisterGenerateTodayLogAtNight(this);
        });


        // Change locale (fi-en-swed)
        btnLocale.setOnClickListener(v -> {
            if (!currentLocale.equals("fi"))
            {
                currentLocale = "fi";
                sharedPreferences.edit().putString("locale", currentLocale).apply();
                LocaleHelper.setLocale(this, currentLocale);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        btnLocale2.setOnClickListener(v -> {
            if (!currentLocale.equals("en"))
            {
                currentLocale = "en";
                sharedPreferences.edit().putString("locale", currentLocale).apply();
                LocaleHelper.setLocale(this, currentLocale);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }

        });
        btnLocale3.setOnClickListener(v -> {
            if (!currentLocale.equals("sv"))
            {
                currentLocale = "sv";
                sharedPreferences.edit().putString("locale", currentLocale).apply();
                LocaleHelper.setLocale(this, currentLocale);
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    // Setup NFC to be on foreground
    @Override
    protected void onResume(){
        super.onResume();
        setupForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onPause() {
        stopForegroundDispatch(this, nfcAdapter);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);

    }

    private void handleIntent(Intent intent) {
        Parcelable[] rawMessages =
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage ndfMsg = (NdefMessage) rawMessages[0];
        String message = new String(ndfMsg.getRecords()[0].getPayload());

        if (message.isEmpty() == false) {
                String empID = message.substring(3);
                // Database get name
                access = new FirebaseAccess();
                access.output_name(empID, this);
                new Handler().postDelayed(() -> {

                    // Refresh
                    txtPlaceholderOne.setText(R.string.card_request);
                    access = new FirebaseAccess();

                }, 10000);
        } else {
            txtPlaceholderOne.setText("Discover an empty tag!");
            txtPlaceholderTwo.setText("Remember to add the employee after writing ID to the tag.");
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        IntentFilter discovery=new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] tagFilters=new IntentFilter[] { discovery };
        Intent i=new Intent(activity.getApplicationContext(), activity.getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi=PendingIntent.getActivity(activity.getApplicationContext(), 0, i, 0);

        adapter.enableForegroundDispatch(activity, pi, tagFilters, null);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

}


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
import android.os.Bundle;

import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    TextView txtPlaceholderOne;
    TextView txtPlaceholderTwo;
    Button btnLogIn;
//    Button btnRefresh;
    ImageButton btnLocale;
    ImageButton btnLocale2;
    ImageButton btnLocale3;
    FirebaseAccess access;

    SharedPreferences sharedPreferences;
    String currentLocale;

    String documentStamp, dateStamp, timeStamp, realtime;
    long scheduledTime;
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
        scheduledTime = 86100000;
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
        txtPlaceholderTwo.setText("");

        btnLogIn = findViewById(R.id.btnLogIn);
//        btnRefresh = findViewById(R.id.btnRefresh);
        btnLocale = findViewById(R.id.btnLocale);
        btnLocale2 = findViewById(R.id.btnLocale2);
        btnLocale3 = findViewById(R.id.btnLocale3);
        btnLogIn.setText(R.string.in_out);
//        btnRefresh.setText(R.string.refresh);

        // NFC Init
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, R.string.no_nfc_support, Toast.LENGTH_SHORT).show();
        }
        if (!nfcAdapter.isEnabled()) {
            txtPlaceholderOne.setText(R.string.nfc_disable_alert);
        }
        handleIntent(getIntent());
        access = new FirebaseAccess();


        // Logging out people
        TaskManager logoutTask = new TaskManager(this);
        logoutTask.logout_schedule(scheduledTime);

        // Generate log files
        // access.generate_logs(this, dateStamp);

        // Button
        btnLogIn.setOnClickListener(view -> {
            if (access.employeeID.equals("")) txtPlaceholderOne.setText(R.string.card_request);
            else {

                // Time init, scheduledTime equivalent to 23:55.
                Date date = new Date();
                documentStamp = new SimpleDateFormat("yyyyMMddHHmmssS").format(date);
                timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(date);
                dateStamp = new SimpleDateFormat("yyyyMMdd").format(date);
                dateS = Integer.parseInt(dateStamp);
                realtime = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(date);

                // Database info update
                access.self_check(access.employeeID, documentStamp, timeStamp, dateS, realtime, this);
            }
        });

        // Refresh state
//        btnRefresh.setOnClickListener(v -> {
//            txtPlaceholderOne.setText(R.string.card_request);
//            txtPlaceholderTwo.setText("");
//            access = new FirebaseAccess();
//        });

        // Change locale
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
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage ndfMsg = (NdefMessage) rawMessages[0];
                String message = new String(ndfMsg.getRecords()[0].getPayload());
                String empID = message.substring(3);

                // Database get name
                access = new FirebaseAccess();
                access.get_name(empID, this);
                new Handler().postDelayed(() -> {

                    // Refresh
                    txtPlaceholderTwo.setText("");
                    access = new FirebaseAccess();

                }, 10000);

            } else {txtPlaceholderOne.setText(R.string.card_read_problem_warning);}
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

}


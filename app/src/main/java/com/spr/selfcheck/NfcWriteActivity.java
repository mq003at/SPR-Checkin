package com.spr.selfcheck;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class NfcWriteActivity extends Activity {
    NfcAdapter nfcAdapter;
    Button btnBack;
    Button btnSbm;
    TextView txtInputGuide;
    EditText editInput;
    ImageView imageView;
    Tag tag;

    FirebaseAccess access;
    String currentLocale;
    Boolean isAuthorized;
    String empID;
    String tagRecord;

    @SuppressLint("SetTextI18n")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_nfc);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        Bundle bundle = getIntent().getExtras();
        LocaleHelper.setLocale(this, bundle.getString("locale"));
        tagRecord = null;

        // UI Init
        btnBack = findViewById(R.id.btnBack);
        btnBack.setText(R.string.go_back);
        btnSbm = findViewById(R.id.btnSbm);
        btnSbm.setText(R.string.sbm);
        txtInputGuide = findViewById(R.id.txtInputGuide);
        editInput = findViewById(R.id.editInput);
        imageView = findViewById(R.id.imageView);
        editInput.requestFocus();
        imageView.setImageResource(R.drawable.logo);

        // Init
        isAuthorized = false;
        access = new FirebaseAccess();
        access.get_store_passcode(this);
        txtInputGuide.setText("Input password, then press Next, then input the new tagID and press next again, then insert the card to write the ID to the tag.");
        currentLocale = getIntent().getExtras().getString("locale");
        editInput.setHint("Manger's password");
        Intent backToMain = new Intent(this, MainActivity.class);

        // NFC Init
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            txtInputGuide.setText(R.string.no_nfc_support);
        }
        if (!nfcAdapter.isEnabled()) {
            txtInputGuide.setText(R.string.nfc_disable_alert);
        }
        handleIntent(getIntent());

        // Btn Init
        btnBack.setOnClickListener(v -> startActivity(backToMain));
        btnSbm.setOnClickListener(v -> {
            if (editInput.getText().toString().equals(""))
                txtInputGuide.setText("Do not leave this field blank");
            else {
                if (!isAuthorized) {
                    String password = access.storePasscode();
                    if (password == null) txtInputGuide.setText(access.error());
                    else {
                        if (!editInput.getText().toString().equals(password)) {
                            txtInputGuide.setText(R.string.wrong_password);
                        } else {
                            txtInputGuide.setText("Input the tagID you want to write.");
                            editInput.setText("");
                            editInput.setHint("Tag ID.");
                            isAuthorized = true;
                        }
                    }
                } else {
                    tagRecord = editInput.getText().toString();
                    if (empID == null) {
                        txtInputGuide.setText("Please insert card.");
                    } else {
                        txtInputGuide.setText("Insert the card behind the phone to start writing.");
                        Log.e("Write", editInput.getText().toString());
                    }
                }
            }
        });
    }

    // Setup NFC to be on foreground
    @Override
    protected void onResume() {
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

    @SuppressLint("SetTextI18n")
    private void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Log.e("Good", isAuthorized + " " + tagRecord);
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (isAuthorized && tagRecord != null) {
                if (rawMessages != null) {
                    // Replace tags
                    NdefMessage ndfMsg = (NdefMessage) rawMessages[0];
                    String message = new String(ndfMsg.getRecords()[0].getPayload());
                    empID = message.substring(3);
                    Toast.makeText(this, "Old ID: " + empID + ". New ID: " + tagRecord + ".", Toast.LENGTH_LONG).show();
                } else {
                    // Write new tag
                    Toast.makeText(this, "New NFC tag: " + tagRecord + ".", Toast.LENGTH_LONG).show();
                }
                writeTag(tag, createTextMessage(tagRecord));
                refresh();
            } else txtInputGuide.setText("You have to type the password or the tagID and press Next before inserting the card!");

        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.putExtra("requestCode", 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        @SuppressLint("UnspecifiedImmutableFlag") final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public NdefMessage createTextMessage (String id) {
        try {
            byte[] lang = Locale.getDefault().getLanguage().getBytes(StandardCharsets.UTF_8);
            byte[] text = id.getBytes(StandardCharsets.UTF_8); // Content in UTF-8

            int langSize = lang.length;
            int textLength = text.length;

            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
            payload.write((byte) (langSize & 0x1F));
            payload.write(lang, 0, langSize);
            payload.write(text, 0, textLength);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
            return new NdefMessage(new NdefRecord[]{record});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeTag(Tag tag, NdefMessage id) {
        if (tag != null) {
            try {
                Ndef ndefTag = Ndef.get(tag);
                if (ndefTag == null) {
                    NdefFormatable nForm = NdefFormatable.get(tag);
                    if (nForm != null) {
                        nForm.connect();
                        nForm.format(id);
                        nForm.close();
                    }
                } else {
                    ndefTag.connect();
                    ndefTag.writeNdefMessage(id);
                    ndefTag.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void refresh(){
        txtInputGuide.setText("You can go back to the login screen, or press the new tag ID in the input field, press next, then insert the new card to continue writing.");
        editInput.setText("");
        editInput.setHint("123456");
        editInput.clearFocus();
        empID = null;
        tagRecord = null;
    }
}

package words.com.locationsharing;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MessageListener {

    private static final String SENT = "send";
    private static final String DELIVERED = "deliverd";
    private static final int ALL_PERMISSIONS_RESULT = 4453453;
    Button sendBtn,retrieveBtn,popUpBtn;
    EditText number;
    TextView title2;
    ImageView close;
    LinearLayout llroot;
    Spinner spinner;
    private ArrayList<String> listP;
    private ArrayList<String> listR;
    private ArrayList<String> listR2;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendBtn = findViewById(R.id.sendBtn);
        retrieveBtn = findViewById(R.id.retrieveBtn);
        popUpBtn = findViewById(R.id.btn_share);
        number = findViewById(R.id.et_number);
        close = findViewById(R.id.close);
        llroot = findViewById(R.id.llroot);
        spinner = findViewById(R.id.spinner);
        title2 = findViewById(R.id.title2);
        String[] items = new String[]{"SIM1", "SIM2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());



        listP= new ArrayList<>();
        listR= new ArrayList<>();
        listR2= new ArrayList<>();
        listP.add(Manifest.permission.ACCESS_FINE_LOCATION);
        listP.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        listP.add(Manifest.permission.INTERNET);
        listP.add(Manifest.permission.SEND_SMS);
        listP.add(Manifest.permission.READ_PHONE_STATE);
        listP.add(Manifest.permission.READ_SMS);
        listP.add(Manifest.permission.RECEIVE_SMS);

        listR2 = permissionsToRequest(listP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (listR2.size() > 0) {
                requestPermissions(listR2.
                        toArray(new String[listR2.size()]), ALL_PERMISSIONS_RESULT);
            }
        }



        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llroot.setVisibility(View.INVISIBLE);

            }
        });
        popUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String simType = spinner.getSelectedItem().toString();
                String num = number.getText().toString();
                if (num.isEmpty() || num.length() < 11){
                    Toast.makeText(MainActivity.this, "Please enter valid number with country code.", Toast.LENGTH_SHORT).show();
                }else if (popUpBtn.getText().equals("Share")){
                    shareLocation(simType,num);
                }else if(popUpBtn.getText().equals("Retrieve")){
                    retrieveLocation(simType,num);
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpBtn.setText("Share");
                llroot.setVisibility(View.VISIBLE);
            }
        });
        retrieveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpBtn.setText("Retrieve");
                llroot.setVisibility(View.VISIBLE);
            }
        });
        startService(new Intent(MainActivity.this, LocationRetrieve.class));

        MessageReciever.bindListener(MainActivity.this);

        new GPSettings(this).turnGPSOn(new GPSettings.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                if (isGPSEnable){
                    Toast.makeText(MainActivity.this, "GPS is turning On", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "GPS is enabled", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void retrieveLocation(String simType, String number) {
        int sim =0;
        if (simType.equals("SIM2")){
            sim = 1;
        }
        number = "+"+number;
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        // SEND BroadcastReceiver
        BroadcastReceiver sendSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        // DELIVERY BroadcastReceiver
        BroadcastReceiver deliverSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(sendSMS, new IntentFilter(SENT));
        registerReceiver(deliverSMS, new IntentFilter(DELIVERED));
        String smsText = "1qaz2wsx3edc";
        //+ System.lineSeparator() +" Shared by: "+ preferencesHelper.getName(); // getSmsText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                }
                // for Activity#requestPermissions for more details.
                return;
            }if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);

                //SendSMS From SIM One
                if (sim == 0){
                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(number, null, smsText, sentPI, deliveredPI);
                }else if (sim == 1){
                    //SendSMS From SIM Two
                    SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage(number, null, smsText, sentPI, deliveredPI);

                }

            }


        } else {
            SmsManager.getDefault().sendTextMessage(number, null, smsText, sentPI, deliveredPI);
            Toast.makeText(getBaseContext(), smsText, Toast.LENGTH_SHORT).show();
        }
        llroot.setVisibility(View.INVISIBLE);
    }

    private void shareLocation(String simType, String number) {
        int sim =0;
        if (simType.equals("SIM2")){
            sim = 1;
        }
        number = "+"+number;
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
                SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        // SEND BroadcastReceiver
        BroadcastReceiver sendSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                      Toast.makeText(MainActivity.this, "SMS sent", Toast.LENGTH_SHORT).show();
                      break;
                }
            }
        };

        // DELIVERY BroadcastReceiver
        BroadcastReceiver deliverSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(sendSMS, new IntentFilter(SENT));
        registerReceiver(deliverSMS, new IntentFilter(DELIVERED));
        String lng = preferences.getString("lng","0");
        String lat = preferences.getString("lat","0");
        String  api = "https://www.google.com/maps/search/?api=1&query="+lat+","+lng;
        String smsText = "Location: "+ preferences.getString("location","Location is not available") +System.lineSeparator()
                + api;
        //+ System.lineSeparator() +" Shared by: "+ preferencesHelper.getName(); // getSmsText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                }
                // for Activity#requestPermissions for more details.
                return;
            }if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
                SubscriptionInfo simInfo2 = (SubscriptionInfo) localList.get(1);

                //SendSMS From SIM One
                if (sim == 0){
                    SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage(number, null, smsText, sentPI, deliveredPI);
                }else if (sim == 1){
                    //SendSMS From SIM Two
                    SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage(number, null, smsText, sentPI, deliveredPI);

                }

            }


        } else {
            SmsManager.getDefault().sendTextMessage(number, null, smsText, sentPI, deliveredPI);
            Toast.makeText(getBaseContext(), smsText, Toast.LENGTH_SHORT).show();
        }
        llroot.setVisibility(View.INVISIBLE);
    }
    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }
    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : listP) {
                    if (!hasPermission(perm)) {
                        listR.add(perm);
                    }
                }

                if (listR.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(listR.get(0))) {
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(listR.
                                                        toArray(new String[listR.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }

                }
                break;
        }
    }

    @Override
    public void messageReceived(String message,String number) {


        String num = number.substring(1,13);

        shareLocation(spinner.getSelectedItem().toString(),num);

    }
}

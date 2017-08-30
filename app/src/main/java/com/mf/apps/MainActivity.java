package com.mf.apps;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private static final String END_POINT = "http://112.78.144.146/mf/insertreport.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (expired()) {
//            setResult(RESULT_CANCELED);
//            finish();
//            return;
//        }
        setContentView(R.layout.activity_main);
        _txvw_00 = (TextView) findViewById(R.id._txvw_00);
        _txvw_00.setOnClickListener(this);
        _txvw_01 = (TextView) findViewById(R.id._txvw_01);
        _txvw_02 = (TextView) findViewById(R.id._txvw_02);
        _txvw_03 = (TextView) findViewById(R.id._txvw_03);
        fl = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT >= 23) {
            int pcFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int pcCoar = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (pcFine != PackageManager.PERMISSION_GRANTED && pcCoar != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 1 );
            }
            else {
                scan();
            }
        }
        else {
            scan();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            ir = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (ir == null || ir.getContents() == null) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            if (lc != null) {
                String url = ir.getContents() // http://112.78.144.146/mf/insertreport.php?id=xxxx
                           + "&lat="  + lc.getLatitude()
                           + "&long=" + lc.getLongitude();
                post(url.toLowerCase());
            }
            else {
                _txvw_00.setText("Retry");
                _txvw_01.setText(Html.fromHtml("App can't find your location"));
                _txvw_03.setText(Html.fromHtml("<b>URL Result:</b> ") + ir.getContents());
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            _txvw_02.setText(Html.fromHtml("<b>Err Message:</b> " + Log.getStackTraceString(e)));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
        case 1:
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                scan();
            }
            break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(_txvw_00)) {
            String text = _txvw_00.getText().toString().toLowerCase();
            if (text.equals("retry")) {
                scan();
            }
            if (text.equals("thank you")) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    /*
     *
     *
     *
     */

    private ProgressDialog _prdl_00 = null;
    private TextView _txvw_00 = null;
    private TextView _txvw_01 = null;
    private TextView _txvw_02 = null;
    private TextView _txvw_03 = null;

    private boolean expired() {
        boolean b = false;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int mont = calendar.get(Calendar.MONTH);
        if (year >= 2017 && mont >= 8) {
            b = true;
        }
        return b;
    }

    private void scan() {
        _txvw_01.setText("");
        IntentIntegrator ii = new com.google.zxing.integration.android.IntentIntegrator(this);
        ii.setPrompt(".");
        ii.setOrientationLocked(true);
        ii.setBeepEnabled(true);
        ii.setCaptureActivity(PortraitCaptureActivity.class);
        ii.initiateScan();
        try {
            fl.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lc = location;
                    }
                }
            });
        } catch (SecurityException se) {
            Log.e(TAG, Log.getStackTraceString(se));
            _txvw_02.setText(Html.fromHtml("<b>Err Message:</b> " + Log.getStackTraceString(se)));
        }
    }

    void post(final String url) {
        new AsyncTask<Void, Void, String[]>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                _prdl_00 = ProgressDialog.show(MainActivity.this, "", "Uploading...", false, false, null);
            }
            @Override
            protected String[] doInBackground(Void... params) {
                String[] response = null;
                try { Thread.sleep(1000); } catch (Exception e) { }
                try {
                    response = HttpFactory.doGet(url);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                return response;
            }
            @Override
            protected void onPostExecute(String[] response) {
                super.onPostExecute(response);
                try {
                    _prdl_00.dismiss();
                    if (response == null || response[1].length() > 0) {
                        _txvw_00.setText("Retry");
                        _txvw_01.setText("App can't send data to server");
                        _txvw_02.setText(Html.fromHtml("<b>Err Message:</b> " + response[1]));
                        _txvw_03.setText(Html.fromHtml("<b>Scan Result:</b> ") + url);
                    }
                    else {
                        _txvw_00.setText("Thank You");
                        _txvw_02.setText(Html.fromHtml("<b>Err Message:</b> " + response[0]));
                        _txvw_03.setText(Html.fromHtml("<b>Scan Result:</b> ") + url);
                    }
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    _txvw_02.setText(Html.fromHtml("<b>Err Message:</b> " + Log.getStackTraceString(e)));
                }
            }
        }.execute();
    }

    private FusedLocationProviderClient fl = null;
    private IntentResult ir = null;
    private Location lc = null;

}

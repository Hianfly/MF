package com.hiandev.mf;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private static final String END_POINT = "http://112.78.144.146/mf/insertreport.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (expired()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        _txvw_00 = (TextView) findViewById(R.id._txvw_00);
        _txvw_00.setOnClickListener(this);
        _txvw_01 = (TextView) findViewById(R.id._txvw_01);
        _txvw_02 = (TextView) findViewById(R.id._txvw_02);
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck  = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1 );
            }
            else {
                scan();
            }
        }
        else {
            scan();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lm != null) {
            lm.removeUpdates(ll);
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ir = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (ir == null || ir.getContents() == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        if (lm != null) {
            lm.removeUpdates(ll);
        }
        if (lc != null) {
            new AsyncTask<Void, Void, String[]>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    _prdl_00 = ProgressDialog.show(MainActivity.this, "", "Uploading...", false, false, null);
                }
                @Override
                protected String[] doInBackground(Void... params) {
                    String[] response = null;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    try {
                        String url  = END_POINT
                                    + "?id="   + ir.getContents()
                                    + "&long=" + lc.getLongitude()
                                    + "&lat="  + lc.getLatitude();
                        response = HttpFactory.doGet(url);
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                    return response;
                }
                @Override
                protected void onPostExecute(String[] response) {
                    super.onPostExecute(response);
                    _prdl_00.dismiss();
                    if (response == null || response[1].length() > 0) {
                        _txvw_00.setText("Retry");
                        _txvw_01.setText("App can't send data to server");
                        _txvw_02.setText(response[1]);
                    }
                    else {
                        _txvw_00.setText("Thank You");
                        _txvw_02.setText(response[0]);
                    }
                }
            }.execute();
        }
        else {
            _txvw_00.setText("Retry");
            _txvw_01.setText("App can't find your location");
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
            if (lm == null) {
                lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            lm.removeUpdates(ll);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, ll);
        } catch (SecurityException se) {
            Log.e(TAG, Log.getStackTraceString(se));
        }
    }

    private IntentResult     ir = null;
    private Location         lc = null;
    private LocationManager  lm = null;
    private LocationListener ll = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lc = location;
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

}

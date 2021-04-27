package com.sunson.sunsonhris;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.auron.library.vcard.VCard;
import it.auron.library.vcard.VCardParser;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS = 100;
    private static final int ZXING_CAMERA_PERMISSION = 1;
    private static final int INTERNET_PERMISSION = 2;
    private static final int PHONE_PERMISSION = 3;
    private String urlu = "http://192.168.2.42/api/tlog/store?";
    private static final String TAG = "Network";
    private Class<?> mClss;
    String tag_json_obj = "json_obj_req";
    int success;
    private static final String TAG_SUCCESS = "isSuccess";
    private static final String TAG_MESSAGE = "message";
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected CustomAdapter mAdapter;
    protected ArrayList<String> mDataset =  new ArrayList<>();
    private static final int DATASET_COUNT = 10;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initDataset();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomAdapter(mDataset);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // END_INCLUDE(initializeRecyclerView)

    }

    private void initDataset() {
        mDataset = new ArrayList<>();
        for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset.add("This is element #" + i) ;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        if (!imei.contains("869266020005648") ) {
            Toast.makeText(this, "APLIKASI HANYA BISA DIPAKAI DI DEVICE YANG TERDAFTAR DI DEPT. IT", Toast.LENGTH_SHORT).show();
            finish();
        };
        Log.v("Eror", "rawResult.getText()");

    }

    public void launchSimpleActivity(View v) {

        launchActivity(ScannerActivity.class);
    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            String[] permisions = null;
            permisions[0] = Manifest.permission.CAMERA;
            permisions[1] = Manifest.permission.INTERNET;
            permisions[2] = Manifest.permission.READ_PHONE_STATE;//
            ActivityCompat.requestPermissions(this,
                    permisions, PERMISSIONS);
        } else {
            Intent captureIntent = new Intent(this, clss);
            startActivityForResult(captureIntent, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        boolean isGranted = false;
        switch (requestCode) {
            case PERMISSIONS:
                if (grantResults.length > 0 ){
                    for (int i = 0 ; i < grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            isGranted = false;
                            Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                            break;
                        } else {
                            isGranted = true;
                        }
                        if (i == (grantResults.length -1) && isGranted){
                            if(mClss != null) {
                                Intent intent = new Intent(this, mClss);
                                startActivity(intent);
                            }
                        }
                    }

                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent result) {
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK && result != null) {
                VCard vCard = VCardParser.parse(result.getStringExtra("DATA"));
                try {
                    if (vCard.getTitle() != null && vCard.getName() != null){
                        Toast.makeText(this, vCard.getName() + " " + vCard.getTitle()  , Toast.LENGTH_SHORT).show();
                        login(vCard.getTitle(), vCard.getName());
                    } else {

                        Toast.makeText(this, "Gagal, silahkan coba kembali"  , Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e){
                    Toast.makeText(this, e.toString()  , Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.v("Eror","err");
            }
        } else {
            Log.v("Eror", "rawResult.getText()");
        }
        super.onActivityResult(requestCode, resultCode, result);
    }

    private void login (final String pin, final String nik) {
        String url = "pin=" + pin + "&nik=" + nik;
//        String query = null;
//        try {
//            query = URLEncoder.encode(url, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        StringRequest strReq = new StringRequest(Request.Method.POST, urlu + url  , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Update Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);
                    // Check for error node in json
                    if (success == 1) {
                        counter++;
                        updateView(counter + ". NAMA : " + jObj.getString("nama") + " NIK : " + jObj.getString("nik") +" TANGGAL: " + jObj.getString("tgl")+ " JAM: " + jObj.getString("jam"));
                        Toast.makeText(getApplicationContext(), "SELAMAT, CheckIn Berhasil!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Maaf, terjadi kesalahan, silahkan dicoba lagi", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        } );
//        strReq.setRetryPolicy(new RetryPolicy() {
//            @Override
//            public int getCurrentTimeout() {
//                return 30000;
//            }
//
//            @Override
//            public int getCurrentRetryCount() {
//                return 30000;
//            }
//
//            @Override
//            public void retry(VolleyError error) throws VolleyError {
//                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    public void updateView(String data) {
        mDataset.add(data);
        mAdapter.notifyDataSetChanged();
    }


}
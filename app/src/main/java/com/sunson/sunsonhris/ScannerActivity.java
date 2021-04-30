package com.sunson.sunsonhris;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private static final String TAG = "Result";
    private ZXingScannerView mScannerView;
    private String data = "";
    private int isFrontCamera = 0;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_simple_scanner);


        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        SharedPreferences mypref = PreferenceManager.getDefaultSharedPreferences(this);
        isFrontCamera = mypref.getInt("camera", 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera(isFrontCamera);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();

    }

    @Override
    public void handleResult(Result rawResult) {
//        Toast.makeText(this, "Contents = " + rawResult.getText() +
//                ", Format = " + rawResult.getBarcodeFormat().toString(), Toast.LENGTH_SHORT).show();
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        // If you would like to resume scanning, call this method below:
//        mScannerView.resumeCameraPreview(this);
        this.data = rawResult.getText();



//
//        String data = "Contents = " + rawResult.getText() +
//                ", Format = " + rawResult.getBarcodeFormat().toString();
//        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
//
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                mScannerView.resumeCameraPreview(ScannerActivity.this);

                Intent intent=new Intent();
                intent.putExtra("DATA",rawResult.getText());
                setResult(-1,intent);
                finish();//finishing activity
            }
        }, 1000);
    }

}
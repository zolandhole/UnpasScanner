package com.example.unpasscanner.utils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.Objects;
import java.util.Timer;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQRActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private Timer timer;
    final int waktu = 10 * 1000;
    final int waktuBerjalan = 30 * (60 * 1000);
    private String dataNim;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(ScanQRActivity.this);
        Handler handler = new Handler();
        setContentView(mScannerView);
        mScannerView.setKeepScreenOn(true);
        mScannerView.setResultHandler(ScanQRActivity.this);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            int requestCameraPermissionID = 1001;
            ActivityCompat.requestPermissions(ScanQRActivity.this, new String[]{Manifest.permission.CAMERA}, requestCameraPermissionID);
            finish();
        }
//        mScannerView.startCamera(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,waktuBerjalan);
        dataNim = Objects.requireNonNull(getIntent().getExtras()).getString("LISTNIM");
    }

    @Override
    protected void onResume() {
        super.onResume();
        cekInternet();
        mScannerView.startCamera(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        matikanPengecekanInternet();
        mScannerView.stopCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(final com.google.zxing.Result result) {
        mScannerView.stopCameraPreview();
        String hasilScanEncripted = result.getText();
        Intent intent = new Intent(ScanQRActivity.this, ResultScanActivity.class);
        intent.putExtra("HASILSCAN", hasilScanEncripted);
        intent.putExtra("DATANIM", dataNim);
        startActivity(intent);
        mScannerView.resumeCameraPreview(ScanQRActivity.this);
    }

    private void cekInternet(){
        timer = new Timer();
        timer.schedule(new NetworkStatus(this, "scan"),0,waktu);
    }

    private void matikanPengecekanInternet(){
        timer.cancel();
        timer.purge();
        timer = new Timer();
    }
}

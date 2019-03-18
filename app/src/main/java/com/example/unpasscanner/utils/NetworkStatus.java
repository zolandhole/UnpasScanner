package com.example.unpasscanner.utils;

import android.content.Context;
import android.util.Log;

import com.example.unpasscanner.MainActivity;

import java.util.TimerTask;

public class NetworkStatus extends TimerTask {
    private Context context;
    private String halaman;
    public NetworkStatus(Context context, String halaman){
        this.context = context;
        this.halaman = halaman;
    }
    @Override
    public void run() {
        if (CheckConnection.apakahTerkoneksiKeInternet(context)){
            switch (halaman){
                case "main":
                    Log.w("YARUD", "ADA KONEKSI INTERNET");
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.displaySuccess();
                    break;
                case "scan":
                    Log.w("SCAN", "ADA KONEKSI INTERNET");
                    break;
            }
        } else {
            switch (halaman){
                case "main":
                    Log.e("YARUD", "TIDAK ADA KONEKSI INTERNET");
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.displayFailed();
                    break;
                case "scan":
                    Log.e("SCAN", "TIDAK ADA KONEKSI INTERNET");
//                    ScanQRActivity scanQRActivity = (ScanQRActivity) context;
//                    scanQRActivity.finish();
                    break;
            }
        }
    }
}

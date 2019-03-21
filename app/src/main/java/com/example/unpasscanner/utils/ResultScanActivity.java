package com.example.unpasscanner.utils;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.unpasscanner.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ResultScanActivity extends AppCompatActivity {
    private String decripted, dateformat;
    private MediaPlayer mp, nmp;
    private ImageView imageViewSuccess;
    private TextView textViewResultNama, textViewDoa;

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_scan);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.63),(int)(height*.35));

        imageViewSuccess = findViewById(R.id.imageViewSuccess);
        textViewResultNama = findViewById(R.id.textViewResultNama);
        textViewDoa = findViewById(R.id.textViewDoa);

        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar datetimeKalender = Calendar.getInstance();
        Date date= datetimeKalender.getTime();
        dateformat = dtf.format(date);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,3*1000);

        mp = MediaPlayer.create(this, R.raw.success);
        nmp = MediaPlayer.create(this, R.raw.unsuccess);


        String hasilScan = Objects.requireNonNull(getIntent().getExtras()).getString("HASILSCAN");
        assert hasilScan != null;
        try {
            decripted = AESUtils.decrypt(hasilScan).trim();
            Log.e("DECRYPT QR",decripted);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int jumlahKata = countWords(decripted);

        if (!(jumlahKata == 4)){
            nmp.start();
            imageViewSuccess.setImageResource(R.drawable.icon_question);
            textViewDoa.setText("Hmm !");
            textViewDoa.setTextColor(R.color.colorPrimary);
            textViewResultNama.setVisibility(View.GONE);
        } else {
            textViewResultNama.setVisibility(View.VISIBLE);
            prosesHasil();
        }
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    private void prosesHasil() {
        String parseHasil[] = decripted.split(" ",3);
        final String namaHasil = parseHasil[1];
        final String nimHasil = parseHasil[0];
        final String waktuHasil = parseHasil[2];

        final String dataNim = Objects.requireNonNull(getIntent().getExtras()).getString("DATANIM");
        assert dataNim != null;
        if (dataNim.toLowerCase().contains(nimHasil.toLowerCase())){
            if (waktuHasil.equals(dateformat)){
                textViewResultNama.setText(namaHasil);
                mp.start();
            } else {
                imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
                textViewDoa.setText("Ups !");
                textViewDoa.setTextColor(R.color.colorPrimary);
                textViewResultNama.setText("QR Code kadaluarsa, Silahkan Generate kembali");
                nmp.start();
            }

        } else {
            imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
            textViewDoa.setText("Ups !");
            textViewDoa.setTextColor(R.color.colorPrimary);
            textViewResultNama.setText("Kita tidak menemukan data Anda di Matakuliah ini");
            nmp.start();
        }
    }

    private static int countWords(String input){
        if (input == null || input.isEmpty()){
            return 0;
        }
        String[] words = input.split("\\s+");
        return words.length;
    }
}

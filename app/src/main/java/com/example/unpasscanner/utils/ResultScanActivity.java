package com.example.unpasscanner.utils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.unpasscanner.MainActivity;
import com.example.unpasscanner.R;
import com.example.unpasscanner.models.ListMahasiswa;
import com.example.unpasscanner.models.SerializableMahasiswa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ResultScanActivity extends AppCompatActivity {
    private String decripted, dateformat;
    private MediaPlayer mp, nmp;
    private ImageView imageViewSuccess;
    private TextView textViewResultNama, textViewDoa;
    ArrayList<SerializableMahasiswa> arrayListMahasiswa;
    ArrayList<String> arrayListMac;
    Intent intentList;
    private String nimMahasiswa,idMk;


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

        if (!(jumlahKata == 1)){
            nmp.start();
            imageViewSuccess.setImageResource(R.drawable.icon_question);
            textViewDoa.setText("Hmm !");
            textViewDoa.setTextColor(R.color.colorPrimary);
            textViewResultNama.setVisibility(View.GONE);
        } else {
            textViewResultNama.setVisibility(View.VISIBLE);
            intentList = getIntent();
            arrayListMahasiswa = (ArrayList<SerializableMahasiswa>) intentList.getSerializableExtra("LISTNIM");
            arrayListMac = intentList.getStringArrayListExtra("LISTMACADDRESS");
            idMk = intentList.getStringExtra("IDMK");
            Log.e("SCANNER RESULT NIM", arrayListMahasiswa.get(0).getNim());
            //Log.e("SCANNER RESULT BT",arrayListMac.get(0));
            Log.e("NIM SCANNER", decripted);
            prosesHasil();
        }
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    private void prosesHasil() {
        String parseHasil[] = decripted.split(" ",3);
        //final String namaHasil = parseHasil[1];
        final String nimHasil = parseHasil[0];
        //final String waktuHasil = parseHasil[2];

        final String dataNim = arrayListMahasiswa.get(0).getNim();
        assert dataNim != null;

        String dataakhir="";
        String namaMahasiswa="";
        for(int index1=0;index1<arrayListMahasiswa.size();index1++){
            Log.e("MAC LIST MAHASISWA",arrayListMahasiswa.get(index1).getMac_user());
            Log.e("NIM LIST MAHASISWA",arrayListMahasiswa.get(index1).getNim());
            if(arrayListMahasiswa.get(index1).getNim().equals(decripted)){
                for(int index2=0;index2<arrayListMac.size();index2++){
                    Log.e("MAC LIST DISCOVER",arrayListMac.get(index2));
                    if(arrayListMac.get(index2).equals(arrayListMahasiswa.get(index1).getMac_user())){
                        dataakhir="absen";
                        nimMahasiswa=arrayListMahasiswa.get(index1).getNim();
                    }
                }
            }
        }
        Log.e("HASIL",dataakhir);
        if(dataakhir.equals("absen")){
            //waktu belum
            textViewResultNama.setText(namaMahasiswa);
            absensiMahasiswa();
            mp.start();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
            imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
            textViewDoa.setText("Ups !");
            textViewDoa.setTextColor(R.color.colorPrimary);
            textViewResultNama.setText("QR Code kadaluarsa, Silahkan Generate kembali");
            nmp.start();

        }
        if (dataNim.toLowerCase().contains(nimHasil.toLowerCase())){
//            if (waktuHasil.equals(dateformat)){
//                textViewResultNama.setText(namaHasil);
//                mp.start();
//            } else {
//                imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
//                textViewDoa.setText("Ups !");
//                textViewDoa.setTextColor(R.color.colorPrimary);
//                textViewResultNama.setText("QR Code kadaluarsa, Silahkan Generate kembali");
//                nmp.start();
//            }

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

    private void absensiMahasiswa() {
        final String jam = ambil_jam();
        Date calender = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String waktuAbsen = df.format(calender);
        Log.e("JAM ",jam);
        Log.e("WAKTU ABSEN ",jam);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerSide.URL_ABSEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("RESPONSE ",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.optString("error").equals("true")){
                                String tidakadadata = jsonObject.getString("message");
                                Log.e("YARUD", tidakadadata);
                                imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
                                textViewDoa.setText("Ups !");
                                textViewResultNama.setText("Terjadi Kesalahan Ketika Absen");
                                nmp.start();
                            }else if(jsonObject.optString("error").equals("false")){
                                String tidakadadata = jsonObject.getString("message");
                                Log.e("YARUD", tidakadadata);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR", error.toString());
                    }
                }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("user", nimMahasiswa);
                params.put("id_mk", idMk);
                params.put("jam", jam);
                params.put("waktu_absen", waktuAbsen);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private String ambil_jam() {
        Calendar calendar= Calendar.getInstance();
        int jam = calendar.get(Calendar.HOUR_OF_DAY);
        int menit = calendar.get(Calendar.MINUTE);
        return Integer.toString(jam)+":"+Integer.toString(menit);
    }
}

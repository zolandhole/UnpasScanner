package com.example.unpasscanner.utils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ResultScanActivity extends AppCompatActivity {
    private static final String TAG = "ResultScanActivity";
    private String decripted, dateformat, hasilScan;
    private MediaPlayer mp, nmp;
    private ImageView imageViewSuccess;
    private TextView textViewResultNama, textViewDoa;
    ArrayList<SerializableMahasiswa> arrayListMahasiswa;
    ArrayList<String> arrayListMac;
    Intent intentList;
    private String nimMahasiswa,idMk;
    private String resultQR;
    private CountDownTimer countDownTimer;
    private Date jamQR;
    private long perbedaanMenit;
    private boolean status = false;


    @SuppressLint({"SetTextI18n", "ResourceAsColor", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_scan);
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
//        getWindow().setLayout((int)(width*.63),(int)(height*.35));

        initView();
        initListener();
        initRunning();
        hasilDariScanner();

    }

    private void initListener() {
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar datetimeKalender = Calendar.getInstance();
        Date date= datetimeKalender.getTime();
        dateformat = dtf.format(date);
        hasilScan = Objects.requireNonNull(getIntent().getExtras()).getString("HASILSCAN");
        mp = MediaPlayer.create(this, R.raw.success);
        nmp = MediaPlayer.create(this, R.raw.unsuccess);
    }

    private void initView() {
        imageViewSuccess = findViewById(R.id.imageViewSuccess);
        textViewResultNama = findViewById(R.id.textViewResultNama);
        textViewDoa = findViewById(R.id.textViewDoa);
    }

    private void kirimUserKeMain() {
        Intent intentMain = new Intent(ResultScanActivity.this, MainActivity.class);
        intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentMain);
        finish();
    }

    private void hasilDariScanner() {
        int jumlahKata = countWords(decripted);
        if (!(jumlahKata == 2)){
            resultQR = "gagal";
            nmp.start();
            imageViewSuccess.setImageResource(R.drawable.icon_question);
            textViewDoa.setText("Hmm !");
            textViewResultNama.setText("QRCODE tidak dikenal");
            textViewResultNama.setVisibility(View.VISIBLE);
            countDownTimer.start();
        } else {
            intentList = getIntent();
            arrayListMahasiswa = (ArrayList<SerializableMahasiswa>) intentList.getSerializableExtra("LISTNIM");
            arrayListMac = intentList.getStringArrayListExtra("LISTMACADDRESS");
            idMk = intentList.getStringExtra("IDMK");
            prosesHasil();
        }
    }

    private void initRunning() {
        assert hasilScan != null;
        try {
            decripted = AESUtils.decrypt(hasilScan).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }

        countDownTimer = new CountDownTimer(3000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {}
            @Override
            public void onFinish() {
                switch (resultQR){
                    case "gagal":
                        kirimUserKeMain();
                        break;
                    case "berhasil":

                        break;
                    case "NI tidak sesuai":
                        finish();
                        break;
                    case "Mac BT tidak ditemukan":
                        finish();
                        break;

                    case "data sesuai":
                        kirimUserKeMain();
                        break;
                }
            }
        };
    }

    private void prosesHasil() {

        String parseHasil[] = decripted.split(" ",2);
        String nimHasil = parseHasil[0];
        String waktuUser = parseHasil[1];

        Date scannerTime = Calendar.getInstance().getTime();
        SimpleDateFormat formatBaru = new SimpleDateFormat("yyy-MM-dd/HH:mm:ss");
        String waktuScanner = formatBaru.format(scannerTime);

        Date dateUser = null;
        Date dateScanner = null;

        try {
            dateUser = formatBaru.parse(waktuUser);
            dateScanner = formatBaru.parse(waktuScanner);

            long diff = dateScanner.getTime() - dateUser.getTime();

//            perbedaanMenit = diff / 1000 % 60;
            perbedaanMenit = diff / (60 * 1000) % 60;
//            long diffHours = diff / (60 * 60 * 1000) % 24;
//            long diffDays = diff / (24 * 60 * 60 * 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (perbedaanMenit >=0 && perbedaanMenit < 2){
            pengecekanNomorInduk(nimHasil);
        } else {
            resultQR = "gagal";
            nmp.start();
            imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
            textViewDoa.setText("Maaf !");
            textViewResultNama.setText("QRCODE Kadaluarsa");
            textViewResultNama.setVisibility(View.VISIBLE);
            countDownTimer.start();
        }
    }

    private void pengecekanNomorInduk(String nimHasil) {
        for (int ni=0; ni < arrayListMahasiswa.size(); ni++){
            if (arrayListMahasiswa.get(ni).getNim().equals(nimHasil)){
                status=true;
                break;
            }else{
                status=false;
            }
        }

        if(!status){
            resultQR="NI tidak sesuai";
            nmp.start();
            imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
            textViewDoa.setText("Maaf !");
            textViewResultNama.setText("Anda tidak terdaftar di mata kuliah ini");
            textViewResultNama.setVisibility(View.VISIBLE);
            countDownTimer.start();
        }else{
            cekMacAddress();
        }
    }

    private void cekMacAddress() {
        status=false;
        String namaMahasiswa="";
        for (int ni=0; ni < arrayListMahasiswa.size(); ni++){
            for (int mac=0; mac < arrayListMac.size(); mac++) {
                if (arrayListMac.get(mac).equals(arrayListMahasiswa.get(ni).getMac_user())) {
                    status = true;
                    namaMahasiswa = arrayListMahasiswa.get(ni).getNama();
                    break;
                }
            }
        }
        if(!status){
            resultQR="Mac BT tidak ditemukan";
            imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
            textViewDoa.setText("Maaf !");
            textViewResultNama.setText("Bluetooth anda tidak terdeteksi \natau tidak sesuai dengan data server");
            textViewResultNama.setVisibility(View.VISIBLE);
            nmp.start();
            countDownTimer.start();
        }else{
            resultQR="data sesuai";
            textViewResultNama.setText(namaMahasiswa);
            absensiMahasiswa();
            mp.start();
            countDownTimer.start();

        }

    }

    private void pengecekanBluetooth() {
        String dataakhir="";
        String namaMahasiswa="";
        for(int index1=0;index1<arrayListMahasiswa.size();index1++){
//            Log.e("MAC LIST MAHASISWA",arrayListMahasiswa.get(index1).getMac_user());
//            Log.e("NIM LIST MAHASISWA",arrayListMahasiswa.get(index1).getNim());
            if(arrayListMahasiswa.get(index1).getNim().equals(decripted)){
                for(int index2=0;index2<arrayListMac.size();index2++){
//                    Log.e("MAC LIST DISCOVER",arrayListMac.get(index2));
                    if(arrayListMac.get(index2).equals(arrayListMahasiswa.get(index1).getMac_user())){
                        dataakhir="absen";
                        nimMahasiswa=arrayListMahasiswa.get(index1).getNim();
                    }
                }
            }
        }
        Log.e("HASIL",dataakhir);
        if(dataakhir.equals("absen")){
            textViewResultNama.setText(namaMahasiswa);
            absensiMahasiswa();
            mp.start();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else{
            imageViewSuccess.setImageResource(R.drawable.icon_sad_red);
            textViewDoa.setText("Ups !");
            textViewDoa.setTextColor(R.color.colorPrimary);
            textViewResultNama.setText("Mac Address atau Nim Anda Tidak Terdeteksi");
            nmp.start();

        }
    }

//    private SimpleDateFormat getWaktuScanner() {
//        Date calender = Calendar.getInstance().getTime();
//
//        return dfJam;
//    }

    private static int countWords(String input){
        if (input == null || input.isEmpty()){
            return 0;
        }
        String[] words = input.split("\\s+");
        return words.length;
    }

    private void absensiMahasiswa() {
        Date calender = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dfJam = new SimpleDateFormat("HH:mm:ss");
        final String waktuAbsen = df.format(calender);
        final String jamAbsen = dfJam.format(calender);
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
                params.put("jam", jamAbsen);
                params.put("waktu_absen", waktuAbsen);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}

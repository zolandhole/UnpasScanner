package com.example.unpasscanner;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.unpasscanner.models.ListMahasiswa;
import com.example.unpasscanner.models.Ruangan;
import com.example.unpasscanner.models.SerializableMahasiswa;
import com.example.unpasscanner.utils.DBHandler;
import com.example.unpasscanner.utils.ScanActivity;
import com.example.unpasscanner.utils.ScanQRActivity;
import com.example.unpasscanner.utils.ServerSide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private String TAG = "MainActivity";
    private ConstraintLayout clSuccess, clLoading, clFailed;
    private CardView cvRetry;
    private CardView mainCardViewSetting, mainCardViewMhsAbsen;
    private DBHandler dBHandler;
    private String koderuanganDB, namaRuanganDB;
    private TextView namaRuangan, tvMataKuliah, tvSks, tvKelas, tvJadwal;
    private String id,tanggal,jam_mulai,jam_selesai,nama_fakultas,nama_jurusan,nama_matakuliah,sks,nama_dosen;
    private String idMk;
    private String ketersediaanJadwal="";
    ArrayList<SerializableMahasiswa> arrayListMahasiswa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initView();
        initObject();
        initRunning();
        cvRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRunning();
            }
        });
        mainCardViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keamananDialog();
            }
        });
        mainCardViewMhsAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Next", Toast.LENGTH_SHORT).show();
                cekJadwalRuangan();
                displayLoading();
                if(!ketersediaanJadwal.equals("")) {
                    keScanActivity(arrayListMahasiswa, idMk);
                }
            }
        });
    }

    private void initView() {
        clSuccess = findViewById(R.id.clSuccess);
        clLoading = findViewById(R.id.clLoading);
        clFailed = findViewById(R.id.clFailed);
        cvRetry = findViewById(R.id.cvRetry);
        mainCardViewMhsAbsen = findViewById(R.id.MainCardViewMhsAbsen);
        mainCardViewSetting = findViewById(R.id.MainCardViewSetting);
        namaRuangan = findViewById(R.id.namaRuangan);
        tvMataKuliah = findViewById(R.id.tvMataKuliah);
        tvSks = findViewById(R.id.tvSks);
        tvKelas = findViewById(R.id.tvKelas);
        tvJadwal = findViewById(R.id.tvJadwal);
    }

    private void initObject() {
        dBHandler = new DBHandler(MainActivity.this);
    }

    private void initRunning() {
        displayLoading();
        checkInternet();
        cekJadwalRuangan();
    }

    private void checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            checkDatabase();
        } else {
            displayFailed();
            Toast.makeText(this, "Tidak ada Koneksi Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkDatabase(){
        getDatabase();
        if (koderuanganDB == null){
            isiFormRuangan();
        } else if (koderuanganDB.equals("")){
            isiFormRuangan();
        } else {
            namaRuangan.setText(namaRuanganDB);
//            cekJadwalRuangan();
            displaySuccess();
        }
    }

    private void cekJadwalRuangan() {
        Date calender = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        final String waktuSekarang = df.format(calender);
        Log.e("JAM",waktuSekarang);
        //Log.e("KODE RUANGAN ",koderuanganDB);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ServerSide.URL_JADWAL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("RESPONSE ",response);
                        try {
                            ketersediaanJadwal="";
                            JSONObject jsonObject = new JSONObject(response);
                                    if(jsonObject.optString("error").equals("true")){
                                        String tidakadadata = jsonObject.getString("message");
                                        Log.e("YARUD", tidakadadata);
                                        displaySuccess();
                                        tvMataKuliah.setText("Tidak Ada Jadwal");
                                        tvSks.setText("Tidak Ada Jadwal");
                                        tvJadwal.setText("Tidak Ada Jadwal");
                                    }else if(jsonObject.optString("error").equals("false")){
                                        arrayListMahasiswa = new ArrayList<>();;
                                        JSONArray jsonArray = jsonObject.getJSONArray("message");
                                        for (int i=0; i < jsonArray.length(); i++){
                                            Log.e("ARRAY JSON",jsonArray.toString());
                                            JSONObject jsonObjectHasil = jsonArray.getJSONObject(i);
                                            idMk = jsonObjectHasil.getString("id");
                                            id = jsonObjectHasil.getString("id");
                                            tanggal = jsonObjectHasil.getString("tanggal");
                                            jam_mulai = jsonObjectHasil.getString("jam_mulai");
                                            jam_selesai = jsonObjectHasil.getString("jam_selesai");
                                            nama_matakuliah = jsonObjectHasil.getString("nama_matakuliah");
                                            sks = jsonObjectHasil.getString("sks");
                                            nama_dosen = jsonObjectHasil.getString("nama_dosen");
                                            JSONArray jsonArray1 = jsonObjectHasil.getJSONArray("list_user");
                                            for(int j=0; j<jsonArray1.length(); j++){
                                                JSONObject jsonObjectListUser = jsonArray1.getJSONObject(j);
                                                Log.e("LIST JSON LIST", jsonArray1.getJSONObject(j).toString());
                                                ListMahasiswa listMahasiswa = new ListMahasiswa();
                                                listMahasiswa.setNim(jsonObjectListUser.getString("nim"));
                                                listMahasiswa.setNama(jsonObjectListUser.getString("nama"));
                                                listMahasiswa.setMac_user(jsonObjectListUser.getString("mac_user"));
                                                arrayListMahasiswa.add(new SerializableMahasiswa(listMahasiswa.getNim(),listMahasiswa.getNama(),listMahasiswa.getMac_user()));
                                            }
                                        }
                                        Log.e("List Mahasiswa",arrayListMahasiswa.toString());
                                        for(int i=0;i<arrayListMahasiswa.size();i++){
                                            Log.e("MAHASISWA GET",arrayListMahasiswa.get(i).getNim());
                                        }
                                        tvMataKuliah.setText(nama_matakuliah);
                                        tvSks.setText("SKS "+sks);
                                        tvJadwal.setText("Jam Mata Kuliah: "+jam_mulai+" - "+jam_selesai);
                                        displaySuccess();
                                        ketersediaanJadwal="tersedia";
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
                        ketersediaanJadwal="";
                    }
                }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("jam", waktuSekarang);
                params.put("ruangan", koderuanganDB);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getDatabase() {
        try {
            List<Ruangan> ruanganList = dBHandler.getAllRuangan();
            for (Ruangan ruangan: ruanganList){
                koderuanganDB = ruangan.getKodeRuangan();
                namaRuanganDB = ruangan.getRuangan();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        dBHandler.close();
    }

    private void isiFormRuangan(){
        dBHandler.deleteRuangan();
        Intent intent = new Intent(MainActivity.this, FormRuanganActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void displayLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clLoading.setVisibility(View.VISIBLE);
                clSuccess.setVisibility(View.GONE);
                clFailed.setVisibility(View.GONE);
            }
        });
    }

    public void displayFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clFailed.setVisibility(View.VISIBLE);
                clSuccess.setVisibility(View.GONE);
                clLoading.setVisibility(View.GONE);
            }
        });
    }

    public void displaySuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clFailed.setVisibility(View.GONE);
                clSuccess.setVisibility(View.VISIBLE);
                clLoading.setVisibility(View.GONE);
            }
        });
    }

    private void keamananDialog() {
        final String usernameEdit = "admin", kataSandi = "admin123";
        View subview = getLayoutInflater().inflate(R.layout.dialog_layout,null);
        final EditText subUserAdmin = subview.findViewById(R.id.userAdmin);
        final EditText subPasswordAdmin = subview.findViewById(R.id.passwordAdmin);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.todoDialogLight);
        builder.setIcon(R.drawable.ic_info)
                .setTitle("Keamanan")
                .setView(subview)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (subUserAdmin.getText().toString().equals("")){
                            Toast.makeText(MainActivity.this, "Masukan Username", Toast.LENGTH_SHORT).show();
                        } else if (subPasswordAdmin.getText().toString().equals("")){
                            Toast.makeText(MainActivity.this, "Masukan Password", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!subUserAdmin.getText().toString().equals(usernameEdit) || !subPasswordAdmin.getText().toString().equals(kataSandi)){
                                Toast.makeText(MainActivity.this, "Username dan password salah", Toast.LENGTH_SHORT).show();
                            } else {
                                isiFormRuangan();
                            }
                        }
                    }
                })
                .setNeutralButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button yes = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        yes.setTextColor(Color.rgb(29,145,36));
    }

    private void keScanActivity(ArrayList <SerializableMahasiswa> listMahasiswas, String idMk) {
        Toast.makeText(this, "Menuju Scanner", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(MainActivity.this, ScanQRActivity.class);
//        intent.putExtra("LISTNIM",listMahasiswas);
//        intent.putExtra("IDMK",idMk);
//        startActivity(intent);

        Intent intentscanner = new Intent (this, ScanActivity.class);
        intentscanner.putExtra("LISTNIM",listMahasiswas);
        intentscanner.putExtra("IDMK",idMk);
        startActivity(intentscanner);
        //finish();
    }




}

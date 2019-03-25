package com.example.unpasscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.unpasscanner.models.Ruangan;
import com.example.unpasscanner.utils.CheckConnection;
import com.example.unpasscanner.utils.DBHandler;
import com.example.unpasscanner.utils.NetworkStatus;
import com.example.unpasscanner.utils.ServerSide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

public class FormRuanganActivity extends AppCompatActivity implements Spinner.OnItemSelectedListener, View.OnClickListener {


    private ConstraintLayout constraintTitle,constrainFak,constrainRuangan;
    private ProgressBar progressBarForm;
    private Button btn_selesai;
    private TextView textViewTitle, textViewFak;
    private JSONArray dataFak, dataRuangan;
    private ArrayList<String> ruangans, fakultas;
    private Spinner spinnerFak, spinnerRuangan;
    private Timer timer;
    final int waktu = 10 * 1000;
    private String kodeFak;
    private DBHandler dbHandler;
    private String idruangan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_ruangan);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            int requestCameraPermissionID = 1003;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestCameraPermissionID);
        }else{
            
        }
        initView();
        initListener();
        initRunning();
    }

    private void pageLoading(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarForm.setVisibility(View.VISIBLE);
                constraintTitle.setVisibility(View.GONE);
                constrainFak.setVisibility(View.GONE);
                constrainRuangan.setVisibility(View.GONE);
                btn_selesai.setVisibility(View.GONE);
            }
        });
    }

    private void pageFailed(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarForm.setVisibility(View.GONE);
                constraintTitle.setVisibility(View.VISIBLE);
                textViewTitle.setText(R.string.nointernet);
                constrainFak.setVisibility(View.GONE);
                constrainRuangan.setVisibility(View.GONE);
                btn_selesai.setVisibility(View.GONE);
            }
        });
    }

    private void pageSuccess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarForm.setVisibility(View.GONE);
                constraintTitle.setVisibility(View.VISIBLE);
                constrainFak.setVisibility(View.VISIBLE);
                constrainRuangan.setVisibility(View.GONE);
                btn_selesai.setVisibility(View.GONE);
            }
        });
    }

    private void initView() {
        constraintTitle = findViewById(R.id.constraintTitle);
        constrainFak = findViewById(R.id.constrainFak);
        constrainRuangan = findViewById(R.id.constrainRuangan);
        progressBarForm = findViewById(R.id.progressBarForm);
        btn_selesai = findViewById(R.id.btn_selesai);
        spinnerFak = findViewById(R.id.spinnerFak);
        textViewFak = findViewById(R.id.textViewFak);
        spinnerRuangan = findViewById(R.id.spinnerRuangan);
        textViewTitle = findViewById(R.id.textViewTitle);
    }

    private void initListener() {
        dbHandler = new DBHandler(FormRuanganActivity.this);
        ruangans = new ArrayList<>();
        fakultas = new ArrayList<>();
        spinnerFak.setOnItemSelectedListener(FormRuanganActivity.this);
        btn_selesai.setOnClickListener(FormRuanganActivity.this);
        spinnerRuangan.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String ruangan = spinnerRuangan.getSelectedItem().toString();
                if (ruangan.equals("Pilih Ruangan ...")){
                    Toast.makeText(FormRuanganActivity.this, "Silahkan Pilih Ruangan", Toast.LENGTH_SHORT).show();
                    btn_selesai.setVisibility(View.GONE);
                } else {
                    btn_selesai.setVisibility(View.VISIBLE);
                    Toast.makeText(FormRuanganActivity.this, "Klik Selesai untuk menyimpan data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ruangans.clear();
            }
        });
    }

    private void initRunning(){
        pageLoading();
        if (!CheckConnection.apakahTerkoneksiKeInternet(this)){
            Toast.makeText(getApplicationContext(),"Tidak ada koneksi Internet",Toast.LENGTH_SHORT).show();
            pageFailed();
        } else {
            ambilDataFak();
        }
    }

    private void ambilDataFak() {
        RequestQueue requestQueue = Volley.newRequestQueue(FormRuanganActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ServerSide.URL_FAKULTAS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.optString("error").equals("false")){
                            try {
                                dataFak = response.getJSONArray("message");
                                getFakultas(dataFak);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (response.optString("error").equals("true")){
                            String tidakadadata;
                            try {
                                tidakadadata = response.getString("message");
                                Toast.makeText(FormRuanganActivity.this, tidakadadata, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("YARUD", "onErrorResponse: "+ error);
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void getFakultas(JSONArray jsonArray) {
        fakultas.add("Pilih Fakultas ...");
        for (int i=0; i<jsonArray.length(); i++){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                fakultas.add(jsonObject.getString("nama_fakultas"));
                pageSuccess();
            } catch (JSONException e) {
                pageFailed();
                e.printStackTrace();
            }
        }
        spinnerFak.setAdapter(new ArrayAdapter<>(FormRuanganActivity.this, android.R.layout.simple_spinner_dropdown_item, fakultas));
    }

    private String getKodeFak(int position){
        kodeFak = "";
        try {
            JSONObject jsonObject = dataFak.getJSONObject(position-1);
            kodeFak = jsonObject.getString("id");
            pageSuccess();
        } catch (JSONException e) {
            pageFailed();
            e.printStackTrace();
        }
        return kodeFak;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String fakultas = spinnerFak.getSelectedItem().toString();
        if (fakultas.equals("Pilih Fakultas ...")){
            Toast.makeText(this, "Silahkan Pilih Fakultas", Toast.LENGTH_SHORT).show();
            constrainRuangan.setVisibility(View.GONE);
            textViewFak.setText("");
        } else {
            ruangans.clear();
            textViewFak.setText(getKodeFak(position));
            kodeFak = textViewFak.getText().toString().trim();
            constrainRuangan.setVisibility(View.VISIBLE);
            progressBarForm.setVisibility(View.VISIBLE);
            ambilDataRuangan(kodeFak);
        }
    }

    private void ambilDataRuangan(String kodeFak) {
        progressBarForm.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(FormRuanganActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ServerSide.URL_RUANGAN + kodeFak, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.optString("error").equals("false")){
                            try {
                                dataRuangan = response.getJSONArray("message");
                                getRuangan(dataRuangan);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (response.optString("error").equals("true")){
                            String tidakadadata;
                            try {
                                tidakadadata = response.getString("message");
                                Toast.makeText(FormRuanganActivity.this, tidakadadata, Toast.LENGTH_SHORT).show();
                                progressBarForm.setVisibility(View.GONE);
                                constrainRuangan.setVisibility(View.GONE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void getRuangan(JSONArray jsonArray) {
        ruangans.add("Pilih Ruangan ...");
        for (int i=0; i<jsonArray.length(); i++){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ruangans.add(jsonObject.getString("nama_ruangan"));
                idruangan = jsonObject.getString("id");
                ruangans.removeAll(Collections.singletonList(""));
                ruangans.removeAll(Collections.singletonList("null"));
                progressBarForm.setVisibility(View.GONE);
            } catch (JSONException e) {
                pageFailed();
                e.printStackTrace();
            }
        }
        spinnerRuangan.setAdapter(new ArrayAdapter<>(FormRuanganActivity.this, android.R.layout.simple_spinner_dropdown_item, ruangans));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_selesai:
                simpanKeDatabase();
                break;
        }
    }

    private void simpanKeDatabase() {
        Log.e("ID RUANGAN ","test");
        idruangan="6";
        Log.e("ID RUANGAN ",idruangan);
            if (!CheckConnection.apakahTerkoneksiKeInternet(this)){
                Toast.makeText(getApplicationContext(),"Tidak ada koneksi Internet",Toast.LENGTH_SHORT).show();
            } else {
                String kodeRuangan = "";
                List<com.example.unpasscanner.models.Ruangan> listRuangan = dbHandler.getAllRuangan();
                for (com.example.unpasscanner.models.Ruangan ruangan: listRuangan){
                    kodeRuangan = ruangan.getKodeRuangan();
                }
                if (kodeRuangan == null){
                    dbHandler.addRuangan(new Ruangan(1,idruangan,spinnerRuangan.getSelectedItem().toString()));
                } else if (kodeRuangan.equals("")) {
                    dbHandler.addRuangan(new Ruangan(1,idruangan,spinnerRuangan.getSelectedItem().toString()));
                } else {
                    dbHandler.updateRuangan(new Ruangan(1,idruangan,spinnerRuangan.getSelectedItem().toString()));
                }
                keMainActivity();
            }
    }

    private void keMainActivity() {
        Intent intent = new Intent(FormRuanganActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        textViewFak.setText("");
        constrainRuangan.setVisibility(View.GONE);
        ruangans.clear();
    }

    private void cekInternet(){
        timer = new Timer();
        timer.schedule(new NetworkStatus(this, "form"),0,waktu);
    }

    private void matikanPengecekanInternet(){
        timer.cancel();
        timer.purge();
        timer = new Timer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        matikanPengecekanInternet();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cekInternet();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                //finish();
            }else{
                //finish();
            }
        }
    }
}
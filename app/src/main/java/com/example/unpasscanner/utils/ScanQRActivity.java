package com.example.unpasscanner.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.unpasscanner.R;
import com.example.unpasscanner.models.ListMahasiswa;
import com.example.unpasscanner.models.SerializableMahasiswa;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQRActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private Timer timer;
    final int waktu = 10 * 1000;
    final int waktuBerjalan = 30 * (60 * 1000);
    private static final String TAG = "GetAddressActivity";
    Intent intentList;
    ArrayList<SerializableMahasiswa> arrayListMahasiswa;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> listMacAddress;
    private String idMk;
    private CountDownTimer countd;

    public void enableDisableBT(){
        if(!bluetoothAdapter.enable()){
            Log.e(TAG, "enableDisableBT: BLuetooth DISABLE");
        }else{
            Toast.makeText(this, "BLUETOOTH SUDAN AKTIF", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "enableDisableBT: BLUETOOTH ACTIVATED");
        }
    }

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "Receiver: OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                listMacAddress.add(device.getAddress());
                Log.d(TAG, "onReceive: "+ device.getAddress());
            }
        }
    };

    public void btnEnableDisable_Discoverable(){
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!bluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            Log.d(TAG, "btnDiscover: Discovering.");
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1002); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listMacAddress = new ArrayList<>();
        enableDisableBT();
        btnDiscover();
        //btnEnableDisable_Discoverable();
        mScannerView = new ZXingScannerView(ScanQRActivity.this);
        Handler handler = new Handler();
        setContentView(mScannerView);
        mScannerView.setKeepScreenOn(true);
        mScannerView.setResultHandler(ScanQRActivity.this);

//        mScannerView.startCamera(1);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,waktuBerjalan);
        intentList= getIntent();
        arrayListMahasiswa = (ArrayList<SerializableMahasiswa>) intentList.getSerializableExtra("LISTNIM");
        idMk = intentList.getStringExtra("IDMK");

        countd = new CountDownTimer(10000,1000){
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                AsyncTask<Void,Void,String> execute = new AmbilData();
                execute.execute();
            }
        };
        countd.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cekInternet();
//        mScannerView.stopCamera();
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
        Log.e("HASIL SCAN", hasilScanEncripted);
        Intent intent = new Intent(ScanQRActivity.this, ResultScanActivity.class);
        intent.putExtra("HASILSCAN", hasilScanEncripted);
        intent.putExtra("LISTNIM",arrayListMahasiswa);
        intent.putExtra("LISTMACADDRESS",listMacAddress);
        intent.putExtra("IDMK",idMk);
//        Log.e("LIST MAHASISWA",listMacAddress.get(0));
        startActivity(intent);
        mScannerView.resumeCameraPreview(this);
        //finish();
        //mScannerView.resumeCameraPreview(ScanQRActivity.this);
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

    public class AmbilData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected String doInBackground(Void... voids) {
            try {
                unregisterReceiver(mBroadcastReceiver3);
                btnDiscover();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            countd.start();
        }
    }

}

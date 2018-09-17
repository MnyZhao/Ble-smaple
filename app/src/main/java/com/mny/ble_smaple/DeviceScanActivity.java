package com.mny.ble_smaple;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.mny.ble_smaple.ble_scan.BleManager;

import java.util.HashMap;

public class DeviceScanActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * StartScan
     */
    private Button mBtnStart;
    /**
     * StopScan
     */
    private Button mBtnStop;

    BleManager mBleManager;
    private ListView mLvBle;
    LeDeviceListAdapter mLeDeviceListAdapter;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static final int REQUEST_ENABLE_BT = 1;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);
        mBleManager = new BleManager(10000);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        initView();
        //判断蓝牙是否开启
        if (!mBleManager.getBluetoothAdapter().isEnabled()) {
            if (!mBleManager.getBluetoothAdapter().isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void initView() {
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(this);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnStop.setOnClickListener(this);
        mLvBle = (ListView) findViewById(R.id.lv_ble);
        mLvBle.setAdapter(mLeDeviceListAdapter);
        mLvBle.setOnItemClickListener(itemListener);
    }

    AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mBleManager.stopLeDevice(leScanCallback);
            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            if (device == null) return;
            final Intent intent = new Intent(DeviceScanActivity.this, DeviceControlActivity.class);
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            startActivity(intent);

        }
    };

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_start:
                mLeDeviceListAdapter.clear();
                mBleManager.startLeDevice(leScanCallback);
                break;
            case R.id.btn_stop:
                mBleManager.stopLeDevice(leScanCallback);
                break;
        }
    }

    HashMap<String, String> hashMap = new HashMap<>();
    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    hashMap.put(device.getAddress(), "mac:" + device.getAddress());
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    Log.e("BleScanActivity", "run: scanRecord.toString()");
                    System.out.println("BleScanActivity.onLeScan" + scanRecord.toString());
                }
            });

        }
    };
}

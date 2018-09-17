package com.mny.ble_smaple.ble_scan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;


/**
 * Crate by E470PD on 2018/9/10
 * 低功耗蓝牙扫描类 5.0之前的扫描方式 已经被废除
 * 开始扫描 {@link #startLeDevice(BluetoothAdapter.LeScanCallback)}
 * 停止扫描 {@link #stopLeDevice(BluetoothAdapter.LeScanCallback)}
 */
public class BleManager {
    private String TAG = "BleManager";
    private BluetoothAdapter bluetoothAdapter;
    //设置扫描时间后终止扫描
    private long scanPeriod = 10000;
    private Handler handler;

    /**
     * 扫描时间  到时间后自动停止
     *
     * @param scanPeriod
     */
    public BleManager(long scanPeriod) {
        this.scanPeriod = scanPeriod;
        handler = new Handler();
    }

    public void setScanPeriod(long scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return bluetoothAdapter;
    }

    /**
     * 开始扫描低功耗设备 leScanCallBack 没有终止标记 所以我们在外面设置扫描时间
     *
     * @param leScanCallback
     */

    @SuppressLint("NewApi")
    public void startLeDevice(final BluetoothAdapter.LeScanCallback leScanCallback) {
        if (null != getBluetoothAdapter()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopLeDevice(leScanCallback);
                }
            }, scanPeriod);
            getBluetoothAdapter().startLeScan(leScanCallback);
        } else {
            Log.e(TAG, "startScanLe: 设备不支持蓝牙");
        }
    }

    /**
     * 停止扫描
     *
     * @param leScanCallback
     */
    @SuppressLint("NewApi")
    public void stopLeDevice(BluetoothAdapter.LeScanCallback leScanCallback) {
        getBluetoothAdapter().stopLeScan(leScanCallback);
    }
}

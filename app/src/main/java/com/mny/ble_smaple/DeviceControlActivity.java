package com.mny.ble_smaple;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaDescrambler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.mny.ble_smaple.ble_scan.BluetoothLeService;
import com.mny.ble_smaple.ble_scan.Connt;

import java.util.ArrayList;

public class DeviceControlActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mConnectionState;
    private TextView mDataField;

    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    //连接成功与否
    private boolean mConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // 设置UI
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    //用于管理服务生命周期的代码。
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(DeviceScanActivity.class.getName(), "Unable to initialize Bluetooth");
                finish();
            }
            // 成功启动初始化后自动连接到设备。
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        register();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
        unRegister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void register() {
        registerReceiver(broadcastReceiver, makeGattUpdateIntentFilter());
    }

    private void unRegister() {
        unregisterReceiver(broadcastReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Connt.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Connt.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Connt.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Connt.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Connt.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                upDateUi(true);//更新titile
                updateConnectionState(R.string.connected);
            } else if (Connt.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                upDateUi(false);;//更新titile
                updateConnectionState(R.string.disconnected);
                clearUI();
            } else if (Connt.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                /*displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (Connt.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));*/
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    MenuItem connect;
    MenuItem disconnect;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        connect = menu.findItem(R.id.menu_connect);
        disconnect = menu.findItem(R.id.menu_disconnect);
        return true;
    }

    private void upDateUi(boolean mConnected) {
        if (mConnected) {
            connect.setVisible(false);
            disconnect.setVisible(true);
        } else {
            connect.setVisible(true);
            disconnect.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.disconnect();
                mBluetoothLeService.connect(mDeviceAddress);
                updateConnectionState(R.string.connecting);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

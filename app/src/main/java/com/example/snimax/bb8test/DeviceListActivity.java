package com.example.snimax.bb8test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

public class DeviceListActivity extends Activity {

    private BluetoothAdapter BtAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private HashMap<String, BluetoothDevice> btMap;
    public static final String EXTRA_BT_DEVICE = "bt_device";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "Bluetooth Module";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        setResult(Activity.RESULT_CANCELED);

        if(BtAdapter == null){
            Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        else {
            if (!BtAdapter.isEnabled()){
                // Request the user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        //wait for bluetooth to start before checking paired devices
        while (!BtAdapter.isEnabled()){}
        showBluetoothDevices();
    }

    public void showBluetoothDevices() {

        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        Set<BluetoothDevice> pairedDevices = BtAdapter.getBondedDevices();
        btMap = new HashMap<String, BluetoothDevice>();

        if(pairedDevices.size() > 0){
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for(BluetoothDevice device : pairedDevices){
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                btMap.put(device.getAddress(), device);
            }
        }
        else{
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }

    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BtAdapter.cancelDiscovery();

            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            BluetoothDevice device = btMap.get(address);
            intent.putExtra(EXTRA_BT_DEVICE, device);

            setResult(Activity.RESULT_OK, intent);

            finish();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    showBluetoothDevices();
                } else {
                    Toast.makeText(MainActivity.getContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

package com.example.snimax.bb8test;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener{

    private static final String TAG = "Main Activity";
    private  TextView Coordinates;
    private static Context context;
    private static Menu menu;
    private static final int GET_BT_DEVICE = 3;
    private BluetoothService btService;
    private static BluetoothService.ThreadConnected btThread = null;
    private static BluetoothService.ConnectThread connectThread = null;
    private String dataToSend = null;
    private static boolean btConnected = false;
    private long lastTimeSent = 0;
    float yOld = 0;
    float xOld = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);
        Coordinates = (TextView) findViewById(R.id.TextView);
         //In case of need of a floating button


        // Debug button, used to send test data over bluetooth
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btThread != null){
                    String msg = "Bluetooth might work now \n";
                    btThread.write(msg.getBytes());
                    Log.d(TAG, msg);
                }
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);*/
        switch (item.getItemId()){
            case R.id.bluetooth_button:
                if(btConnected == false) {
                    Intent intent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(intent, GET_BT_DEVICE);
                }
                else{
                    menu.getItem(0).setIcon(R.drawable.ic_bluetooth_black_24dp);

                    btThread.cancel();
                    btThread = null;

                    connectThread = btService.getConnectThread();
                    connectThread.cancel();
                    connectThread = null;
                    Toast.makeText(getApplicationContext(), R.string.bt_off, Toast.LENGTH_SHORT).show();
                    btConnected = false;
                }
                return true;
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "Create a settings screen with developer options", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case GET_BT_DEVICE:
                if (resultCode == RESULT_OK) {
                    BluetoothDevice btDevice = data.getExtras().getParcelable(DeviceListActivity.EXTRA_BT_DEVICE);
                    if(btDevice != null) {
                        Log.d(TAG, btDevice.getName() + " " + btDevice.getAddress());
                        btService = new BluetoothService(btDevice);
                        menu.getItem(0).setIcon(R.drawable.ic_bluetooth_connected_black_24dp);
                        Toast.makeText(getApplicationContext(), "Systems initialized" + btDevice.getName() + ".", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this, R.string.bt_device_not_found, Toast.LENGTH_SHORT).show();
                }
                else if (resultCode == RESULT_CANCELED){
                    break;
                }

                else {
                    Toast.makeText(MainActivity.getContext(), R.string.bt_went_wrong, Toast.LENGTH_SHORT).show();
                }
        }

    }
    /* Här körs joysticken och data skickas i form av X och Y koordinater.*/
    @Override
    public void onJoystickMoved(float xPercent, float yPercent, float buttonInp, int id){
        float xCoor = ((float)Math.round(xPercent * 100) / 100);
        float yCoor = ((float)Math.round(yPercent * 100) / 100);

        Log.d(TAG, "Speed: " + yCoor + " Angular velocity: " + xCoor + "H" + buttonInp);
        //Coordinates.setText("X percent: " + xCoor + " Y percent: " + yCoor);
        Coordinates.setText("Speed: " + yCoor + " Angular velocity: " + xCoor + "H" + buttonInp);
        //Coordinates.setText("Left Wheel: " + xBlah + " Right Wheel: " + yBlah);

        if(btThread != null ){//&& xOld != xCoor){
            dataToSend = "X" + (int) (xCoor * 100) + "Y" + (int)(yCoor * 100) + "H" + (int) buttonInp + "\n";
            btThread.write(dataToSend.getBytes());
        }
        /*if(btThread != null ){//&& xOld != xCoor){
            dataToSend = "X" + xBlah + "Y" + yBlah + "\n";
            btThread.write(dataToSend.getBytes());
        }*/
    }

    public static void createConnectionThread(BluetoothSocket socket){
        btThread = new BluetoothService.ThreadConnected(socket);
        btThread.start();
        btConnected = true;
    }

    public static Context getContext(){
        return context;
    }

}

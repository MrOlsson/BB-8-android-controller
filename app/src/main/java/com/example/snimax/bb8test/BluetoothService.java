package com.example.snimax.bb8test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Henrik on 31-Mar-17.
 */

public class BluetoothService {

    private BluetoothAdapter BtAdapter = BluetoothAdapter.getDefaultAdapter();
    private ConnectThread connectThread;
    private UUID MY_UUID;
    private static final String TAG = "BluetoothService";

    public BluetoothService(BluetoothDevice btDevice) {
        MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        //Bluetooth adapter exists since DeviceListActivity returned a device
        BtAdapter = BluetoothAdapter.getDefaultAdapter();

        connectThread = new BluetoothService.ConnectThread(btDevice);
        connectThread.start();
        Toast.makeText(MainActivity.getContext(), "Attempting to connect to " + btDevice.getName() + ".", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "connectThread have started.");

    }

    public ConnectThread getConnectThread(){
        return connectThread;
    }

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final String TAG = "ConnectThread";

        ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                //Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BtAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    Log.d(TAG, "connect failed - " + connectException.getMessage());
                    mmSocket.close();
                } catch (IOException closeException) {
                    //Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            Log.d(TAG, mmSocket.isConnected() ? "y" : "n");
            //manageMyConnectedSocket(mmSocket);
            MainActivity.createConnectionThread(mmSocket);

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public static class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            String strRx = "";

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);
                    final String strByteCnt = String.valueOf(bytes) + " bytes received.\n";

                    Log.d("ThreadConnected", "Bt data received:" + strReceived + " " + strByteCnt);

                    /*runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.append(strReceived);
                            textByteCnt.append(strByteCnt);
                        }});*/

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    /*runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }});*/
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}

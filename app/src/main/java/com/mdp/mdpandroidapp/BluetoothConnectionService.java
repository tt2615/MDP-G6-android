package com.mdp.mdpandroidapp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionG6";

    private static volatile BluetoothConnectionService INSTANCE = null;

    private static final String appName = "MDPG6";

    private static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;

    private AcceptThread mInsecureAcceptThread;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private Handler mHandler;
    static final int MESSAGE_READ = 0;
    static final int MESSAGE_WRITE = 1;


    private BluetoothConnectionService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        });
    }

    /**
     * Get instance of BluetoothService to be used
     * @return An instance of BluetoothService
     */
    public static BluetoothConnectionService getInstance()  {
        if (INSTANCE == null) {
            synchronized (BluetoothConnectionService.class) {
                 if (INSTANCE == null) {
                     INSTANCE = new BluetoothConnectionService();
                 }
            }
        }
        return INSTANCE;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    synchronized void listen() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            Log.d(TAG, "connection thread cancelled");
//            mConnectThread = null;
//        }
//        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
//            Log.d(TAG, "connected thread released");
//            mConnectThread = null;
//        }
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Stop all threads
     */
    synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        listen();
    }

    public void registerNewHandlerCallback(Handler.Callback callback) {
        mHandler = new Handler(callback);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(boolean secure) {
            Log.d(TAG,"create new AcceptThread");
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            if(secure){
                try {
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, MY_UUID_SECURE);
                    Log.d(TAG, "AcceptThread: Setting up secure Server using: " + MY_UUID_SECURE);
                } catch (IOException e1) {
                    Log.e(TAG, "AcceptThread Secure: IOException: " + e1.getMessage());
                }
            }
            else {
                try {
                    tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                } catch (IOException e1) {
                    Log.e(TAG, "AcceptThread Insecure: IOException: " + e1.getMessage());
                }
                Log.d(TAG, "AcceptThread: Setting up insecure Server using: " + MY_UUID_INSECURE);
            }

            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try {
                // This is a blocking call and will only return on a successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            // A connection was accepted. Perform work associated with
            // the connection in a separate thread
            if (socket != null) {
                synchronized (BluetoothConnectionService.this) {
                    manageMyConnectedSocket(socket);
                    cancel();
                }
                Log.i(TAG, "END mAcceptThread ");
            }
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            Log.d(TAG, "ConnectThread: started."+mmDevice.getName()+mmDevice.getAddress());

            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            if(secure){
                Log.d(TAG, "ConnectThread: Trying to create SecureRfcommSocket using UUID: "
                        +MY_UUID_SECURE );
                try {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                } catch (Exception e1) {
                    Log.e(TAG, "ConnectThread: Could not create SecureRfcommSocket " + e1.getMessage());
                }
            }
            else {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                try {
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                } catch (IOException e1) {
                    Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e1.getMessage());
                }
            }
            mmSocket = tmp;
        }

        public void run(){
            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
                manageMyConnectedSocket(mmSocket);
            } catch (IOException e1) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e2) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e2.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE + e1.getMessage());
            }

            synchronized (BluetoothConnectionService.this) {
                mConnectThread = null;
            }
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    /**
     Responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, incomingMessage).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }

            class IndicateAlive extends TimerTask {
                public void run() {
                    mConnectedThread.write("RP Z".getBytes());
                }
            }

            Timer timer = new Timer();
            timer.schedule(new IndicateAlive(), 0, 4000);
        }
        

        //Call this from the main activity to send data to the remote device
        void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
                mmOutStream.flush();
                mHandler.obtainMessage(MESSAGE_WRITE, bytes.length, -1, bytes).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {

            }
        }
    }


    /**
     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other device's AcceptThread.
     **/

    synchronized void startClient(BluetoothDevice device, boolean secure){
        Log.d(TAG, "startClient: Started." + device.getName() + device.getAddress());
        stop();

        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
    }


    synchronized void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }


    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public synchronized void write(byte[] out) {
        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        if (mConnectedThread != null) {
            mConnectedThread.write(out);
        }
        else{
            Log.d(TAG, "device not connected");
        }
    }

}


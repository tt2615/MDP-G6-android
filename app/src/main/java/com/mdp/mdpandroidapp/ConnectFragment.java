package com.mdp.mdpandroidapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;

public class ConnectFragment extends Fragment implements AdapterView.OnItemClickListener{

    public ConnectFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "ConnectFragment";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnectionService mBluetoothConnectionService;

    private ListView lvNewDevices;
    private Button btnEnable_DisableBT, btnEnableDisable_Discoverable, btnDiscover, btnSend, pi_button;
    private EditText msgToSend;

    public BluetoothDevice mBTDevice;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;

    private ListView mDeviceMessages;
    private ArrayAdapter<String> mDeviceMessagesListAdapter;

    public ArrayList<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>(); //for unregister receivers when quit

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set bluetooth adapter
        mBluetoothAdapter = ((MainActivity)getActivity()).getBluetoothAdapter();
        mBluetoothConnectionService = ((MainActivity)getActivity()).getBluetoothConnectionService();
        //  Register handler callback to handle BluetoothService messages
        mBluetoothConnectionService.registerNewHandlerCallback(bluetoothServiceMessageHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_connect, container, false);

        //define buttons and views
        btnEnable_DisableBT = (Button) rootView.findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = (Button) rootView.findViewById(R.id.btnDiscoverable_on_off);
        btnDiscover = (Button) rootView.findViewById(R.id.btnFindUnpairedDevices);
        btnSend = (Button) rootView.findViewById(R.id.SendMsg);
        pi_button = rootView.findViewById(R.id.pi_button);
        msgToSend = (EditText) rootView.findViewById(R.id.MsgToBeSent);
        lvNewDevices = (ListView) rootView.findViewById(R.id.lvNewDevices);
        mDeviceMessages = (ListView) rootView.findViewById(R.id.MsgReceived);
        mDeviceMessagesListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mDeviceMessages.setAdapter(mDeviceMessagesListAdapter);

        //for displaying available device lists
        mBTDevices = new ArrayList<>();
        lvNewDevices.setOnItemClickListener(ConnectFragment.this);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getActivity().registerReceiver(mBroadcastReceiver4, filter);

        //initiate on/off BT button
        if(!mBluetoothAdapter.isEnabled()){
            btnEnable_DisableBT.setText("ON Bluetooth");
        }
        else {
            btnEnable_DisableBT.setText("OFF Bluetooth");
        }

        btnEnable_DisableBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        //initiate on/off discoverable button
        if(mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            btnEnableDisable_Discoverable.setText("Discoverable ON");
        }
        else {
            btnEnableDisable_Discoverable.setText("Discoverable OFF");
        }

        btnEnableDisable_Discoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Log.d(TAG, "onClick: enabling/disabling discoverable.");
                enableDisable_Discoverable();
            }
        });

        //listener for scan available device button
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Log.d(TAG, "onClick: find unpaired devices");
                mBTDevices.clear();
                discover();
            }
        });

        //listener for sending button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = msgToSend.getText().toString().trim();
                if (message.length() != 0) {
                    msgToSend.setText("");
                    mBluetoothConnectionService.send(message);
                }
            }
        });

        pi_button.setOnClickListener(new View.OnClickListener() { //B8:27:EB:3A:91:84
            @Override
            public void onClick(View view) {

                if(!mBluetoothAdapter.isEnabled()){
                    Toast toast = Toast.makeText(getActivity(), "Bluetooth not connected", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                mBluetoothAdapter.cancelDiscovery();
                if(!mBluetoothConnectionService.isConnected()) {
                    mBTDevice = mBluetoothAdapter.getRemoteDevice("B8:27:EB:3A:91:84");
                    mBluetoothConnectionService.startClient(mBTDevice, true);
                }
                else{
                    Toast toast = Toast.makeText(getContext(),"Already Connected",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        mBluetoothAdapter.cancelDiscovery();
        if(mBluetoothConnectionService == null){
            mBluetoothConnectionService.stop();
        }
    }

    /**
     * Unregister all the receivers
     */
    public void unregisterReceiver(BroadcastReceiver receiver){
        if (receivers.contains(receiver)){
            receivers.remove(receiver);
            getActivity().unregisterReceiver(receiver);
            Log.d(TAG, getClass().getSimpleName() + "unregistered receiver: "+receiver);
        }
    }

    /**
     * Broadcast Receiver that detects bond state changes
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            receivers.add(mBroadcastReceiver4);

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: already bonded
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                }
                //case2: creating a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    /**
     * Turn on/off BT of the device
     * -Called when btnEnable_DisableBT is clicked
     */
    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(mBroadcastReceiver1, BTIntent);
        }

    }

    /**
     * Broadcast Receiver for changes made to bluetooth states: Discoverability mode on/off or expire.
     * -Executed by enableDisableBT() Method
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            receivers.add(mBroadcastReceiver1);

            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        btnEnable_DisableBT.setText("ON Bluetooth");
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        btnEnable_DisableBT.setText("Turning Off...");
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        btnEnable_DisableBT.setText("OFF Bluetooth");
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        btnEnable_DisableBT.setText("Turning On...");
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Turn on/off discoverable mode for BT
     * -Called when btnEnableDisable_Discoverable is clicked
     */
    public void enableDisable_Discoverable() {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 600 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //extend time from 120s to 600s
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        getActivity().registerReceiver(mBroadcastReceiver2,intentFilter);
    }

    /**
     * Broadcast Receiver for changes made to bluetooth states: Discoverability mode on/off or expire.
     * -Executed by btnEnableDisable_Discoverable() Method
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            receivers.add(mBroadcastReceiver2);

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

    /**
     * List devices that are not yet paired
     * -Called when btnDiscover is clicked
     */
    public void discover(){
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        //disable discovering first if it is on
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");
        }

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
    }

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * Populate a list of devices on ListView
     * -Executed by discover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            receivers.add(mBroadcastReceiver3);
            Log.d(TAG, "onReceive: ACTION FOUND.");

            //populate a list of devices connectible if discover mode turns on
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                Log.d("jaydenchua", device.getAddress());
                if (device.toString().equals("B8:27:EB:3A:91:84") || device.toString().equals("C8:21:58:8F:02:8B") || device.toString().equals("48:2C:A0:38:7B:EE") || device.toString().equals("E0:9D:31:DC:1F:3A")){ // pi,jay,brandon
                    mBTDevices.add(device);
                }
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    /**
     * Create bond on selected device
     * Override method of implemented Interface: AdapterView.OnItemClickListener
     */

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        if(!mBluetoothAdapter.isEnabled()){
            Toast toast = Toast.makeText(getActivity(), "Bluetooth not connected", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        mBTDevice = mBTDevices.get(i);

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevice.getName();
        String deviceAddress = mBTDevice.getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        Log.d(TAG, "Trying to pair with " + deviceName);

        //create bond
        mBTDevice.createBond();

        //create connection
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        if(!mBluetoothConnectionService.isConnected()) {
            mBluetoothConnectionService = BluetoothConnectionService.getInstance();
        }
        mBluetoothConnectionService.startClient(mBTDevice, true);
    }


    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     *
     * -Executed by discover() method
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = ActivityCompat.checkSelfPermission(getContext(),"Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += ActivityCompat.checkSelfPermission(getContext(),("Manifest.permission.ACCESS_COARSE_LOCATION"));
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                switch (message.what) {
                    case BluetoothConnectionService.MESSAGE_READ:
                        //  Reading message from remote device
                        String receivedMessage = message.obj.toString();
                        mDeviceMessagesListAdapter.add(receivedMessage);
                        break;
                    case BluetoothConnectionService.MESSAGE_TOAST:
                        String deviceName = message.obj.toString();
                        Toast toast = Toast.makeText(getContext(),
                                "Device Connected: "+deviceName,
                                Toast.LENGTH_SHORT);
                        toast.show();

                    return false;

                }
            }catch (Throwable t) {
                Log.e(TAG,null, t);
            }
            return false;
        }
    };
}

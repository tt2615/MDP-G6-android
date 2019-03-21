package com.mdp.mdpandroidapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mdp.mdpandroidapp.BluetoothConnectionService;

public class ControlFragment extends Fragment implements SensorEventListener {

    private final String TAG = "ControlFragment";

    private BluetoothConnectionService mBluetoothConnectionService;

    private TextView mArduinoState;
    private Button mBtnFw, mBtnTr, mBtnTl, mBtnTb, tilt_button;

    private SensorManager sensorManager;
    private Sensor sensor;

    private boolean tiltMode = false;

    public ControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mBluetoothConnectionService = ((MainActivity)getActivity()).getBluetoothConnectionService();
        mBluetoothConnectionService.registerNewHandlerCallback(bluetoothServiceMessageHandler);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_control, container, false);

        mArduinoState=(TextView)rootView.findViewById(R.id.statusBar);
        mBtnFw = (Button)rootView.findViewById(R.id.btnForward);
        mBtnTr = (Button)rootView.findViewById(R.id.btnTurnRight);
        mBtnTl = (Button)rootView.findViewById(R.id.btnTurnLeft);
        mBtnTb = (Button)rootView.findViewById(R.id.btnBackward);
        tilt_button = rootView.findViewById(R.id.tilt_button);

        mBtnFw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = "AD 0,0,10;";
                mBluetoothConnectionService.send(message);
            }
        });

        mBtnTr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = "AD 0,1,10;";
                mBluetoothConnectionService.send(message);
            }
        });

        mBtnTl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = "AD 0,3,10;";
                mBluetoothConnectionService.send(message);
            }
        });

        mBtnTb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = "AD 0,2,10;";
                mBluetoothConnectionService.send(message);
            }
        });

        tilt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tiltMode = !tiltMode;
                if(tiltMode) tilt_button.setText("press to off tilt mode");
                else tilt_button.setText("press to on tilt mode");
            }
        });

        return rootView;
    }

    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                switch (message.what) {
                    case BluetoothConnectionService.MESSAGE_READ:
                        //  Reading message from remote device
                        String receivedMessagetmp = message.obj.toString();
                        String receivedMessage = receivedMessagetmp.substring(2, receivedMessagetmp.length() - 1);
                        switch (receivedMessage){
                            case "rl":
                                mArduinoState.setText("rotating left");
                                break;
                            case "rr":
                                mArduinoState.setText("rotating right");
                                break;
                            case "mf":
                                mArduinoState.setText("moving forward");
                                break;
                            case "mb":
                                mArduinoState.setText("moving backward");
                                break;
                            case "nm":
                                mArduinoState.setText("not moving");
                                break;
                        }
                        return false;
                }
            }catch (Throwable t) {
                Log.e(TAG,null, t);
            }

            return false;
        }
    };


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(tiltMode) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            if (Math.abs(x) > Math.abs(y)) {
                if (x < -6) {
                    String message = "AD 0,1,0;";
                    mBluetoothConnectionService.send(message); //rr
                }
                if (x > 6) {
                    String message = "AD 0,3,0;";
                    mBluetoothConnectionService.send(message); //rl
                }
            } else {
                if (y < -5) {
                    String message = "AD 0,0,10;";
                    mBluetoothConnectionService.send(message); //mf
                }
                if (y > 5) {
                    String message = "AD 0,2,10;";
                    mBluetoothConnectionService.send(message); //mb
                }
            }
            if (x > (-2) && x < (2) && y > (-2) && y < (2)) {
                // neutral
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


}

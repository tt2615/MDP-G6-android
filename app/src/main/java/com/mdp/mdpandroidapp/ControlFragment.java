package com.mdp.mdpandroidapp;

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

public class ControlFragment extends Fragment {

    private final String TAG = "ControlFragment";

    private BluetoothConnectionService mBluetoothConnectionService;

    private TextView mArduinoState;
    private Button mBtnFw, mBtnTr, mBtnTl, mBtnTb;


    public ControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mBluetoothConnectionService = ((MainActivity)getActivity()).getBluetoothConnectionService();
        mBluetoothConnectionService.registerNewHandlerCallback(bluetoothServiceMessageHandler);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_control, container, false);

        mArduinoState=(TextView)rootView.findViewById(R.id.statusBar);
        mBtnFw = (Button)rootView.findViewById(R.id.btnForward);
        mBtnTr = (Button)rootView.findViewById(R.id.btnTurnRight);
        mBtnTl = (Button)rootView.findViewById(R.id.btnTurnLeft);
        mBtnTb = (Button)rootView.findViewById(R.id.btnBackward);

        mBtnFw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = "AD 0,0,10;";
                mBluetoothConnectionService.write(message.getBytes());
            }
        });

        mBtnTr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = "AD 0,1,10;";
                 mBluetoothConnectionService.write(message.getBytes());
            }
        });

        mBtnTl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = "AD 0,-1,10;";
                mBluetoothConnectionService.write(message.getBytes());
            }
        });

        mBtnTb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String message = "AD 0,2,10;";
                mBluetoothConnectionService.write(message.getBytes());
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
                        Log.d("jaydenchua", receivedMessage);
                        switch (receivedMessage){
                            case "ml": // todo checklist #4 robot status
                                mArduinoState.setText("moving left");
                                break;
                            case "mr":
                                mArduinoState.setText("moving right");
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


}

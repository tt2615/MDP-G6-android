package com.mdp.mdpandroidapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    private static final String TAG = "ExploreFragment";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothConnectionService mBluetoothConnectionService;

    private ArrayAdapter<String> mDeviceMessagesListAdapter;
    private ListView mDeviceMessages;

    // algo buttons
    private Button fastpath_button;
    private Button explore_button;
    private Button auto_button;
    private Button manual_button;
    private Button update_button;
    private Button cancel_button;
    private Button sendsp_button;
    private Button sendwp_button;
    private Button calibrate_button;

    private boolean manual_display_mode = false;
    private Integer positionId;
    private Integer oldPositionId;
    private ArrayList<Integer[]> arrowId = new ArrayList<Integer[]>();
    private String mapDescriptor1;
    private String mapDescriptor2;
    private String arrowString = "";
    private char arduinoDir = 'u';

    // wp sp portion
    private Button waypoint_button;
    private Button startpoint_button;
    private Button reset_button;
//    private TextView button_status;
    int mode = 0;
    static final int ModeWayPoint = 1;
    static final int ModeStartPoint = 2;
    static final int ModeIdle =0;

    // parsing mode protion
    private int parsingMode = 0;
    static final int ParsingModeIdle = 0;
    static final int ParsingModeExplore = 1;
    static final int ParsingModeFPath = 2;
    static final int ParsingModeCal = 3;

    // arena portion
    private Arena mArena;

    // class variable for waypoint and startpoint
    private TextView waypoint_coord;
    private TextView startpoint_coord;
    int wayPointId;
    int startPointId;
    String wp_str = "-";
    String sp_str = "-";
    SharedPreferences wp_sp;
    SharedPreferences sp_sp;
    SharedPreferences mdf1_sp;
    SharedPreferences mdf2_sp;
    SharedPreferences image_sp;
    public static final String DEFAULTCOORD = "-";
    private int start_direction = 0;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mBluetoothAdapter = ((MainActivity)getActivity()).getBluetoothAdapter();
        mBluetoothConnectionService = ((MainActivity)getActivity()).getBluetoothConnectionService();
        mBluetoothConnectionService.registerNewHandlerCallback(bluetoothServiceMessageHandler);
    }

    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                switch (message.what) {
                    case BluetoothConnectionService.MESSAGE_READ:
                        String receivedMessage = message.obj.toString();
                        String msg = receivedMessage.substring(2, receivedMessage.length() - 1); //b'x'
                        Log.d(TAG,"message received: " + msg);
                        String[] msgList = msg.split(";");
                        String[] arduinoPosition;
                        //  Reading message from remote device based on different parsing mode
                        switch (parsingMode) {
                            case ParsingModeExplore:
                                arduinoPosition = msgList[0].split(",");
                                if(msgList.length==4) {
                                    Log.d(TAG, "MsgLength: " + msgList[2].length());
                                    Log.d(TAG,"not empty update");
                                    Log.d(TAG, "des1: "+msgList[2]);
                                    Log.d(TAG, "des2: "+msgList[3]);
                                    mapDescriptor1 = msgList[2];
                                    mapDescriptor2 = msgList[3];
                                }
                                switch (arduinoPosition[0]) {
                                    case "po":
                                        int po_x = Integer.parseInt(arduinoPosition[1]);
                                        int po_y = Integer.parseInt(arduinoPosition[2]);
                                        arduinoDir = msgList[1].charAt(0);
                                        positionId = corToId(po_x, po_y);
                                        mDeviceMessagesListAdapter.add("Android Position at: " + po_x + ", " + po_y +
                                                " || Direction: " + arduinoDir +
                                                "\nMDF String 1: " + mapDescriptor1 +
                                                "\nMDF String 2: " + mapDescriptor2 +
                                                "\n" + printArrows());
                                        Log.d(TAG, "android position at: " + po_x + "," + po_y);
                                        if (manual_display_mode) {
                                            break;
                                        }
                                        updateArena();
                                        updatePosition();
                                        break;
                                    case "ar":
                                        int ar_x = Integer.parseInt(arduinoPosition[1]);
                                        int ar_y = Integer.parseInt(arduinoPosition[2]);
                                        char arrowDir = msgList[1].charAt(0);
                                        Log.d(TAG, "discovered arrow: " + ar_x + "," + ar_y +
                                                "\ndirection: " + arrowDir);
                                        discoverArrow(ar_x,ar_y,arrowDir);
                                        updateArena();
                                        updatePosition();
                                        mDeviceMessagesListAdapter.add("discovered arrow: " + ar_x + "," + ar_y + "," + arrowDir);
                                        break;
                                    case "ExpEnd":
                                        Toast toast = Toast.makeText(getContext(),
                                                "Exploration finished!",
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                        mDeviceMessagesListAdapter.add("Exploration finished!");
                                        deactivateButton(explore_button);
                                        set_parse_mode(ParsingModeCal);
                                        updateArena();
                                        updatePosition();

                                        SharedPreferences.Editor edit_mdf1_sp = mdf1_sp.edit();
                                        edit_mdf1_sp.putString("sp_mdf1", mapDescriptor1);
                                        edit_mdf1_sp.commit();

                                        SharedPreferences.Editor edit_mdf2_sp = mdf2_sp.edit();
                                        edit_mdf2_sp.putString("sp_mdf2", mapDescriptor2);
                                        edit_mdf2_sp.commit();

                                        SharedPreferences.Editor edit_image_sp = image_sp.edit();
                                        edit_image_sp.putString("sp_arrow", printArrows());
                                        edit_image_sp.commit();
                                        break;
                                }
                                break;

                            case ParsingModeCal:
                                switch (msgList[0]){
                                    case "CalEnd":
                                        Toast toast = Toast.makeText(getContext(),
                                                "Calibration finished!",
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                        mDeviceMessagesListAdapter.add("Calibration finished!");
                                        fastpath_button.setEnabled(true);
                                        break;
                                }
                                break;

                            case ParsingModeFPath:
                                arduinoPosition = msgList[0].split(",");

                                switch (arduinoPosition[0]) {
                                    case "po":
                                        int po_x = Integer.parseInt(arduinoPosition[1]);
                                        int po_y = Integer.parseInt(arduinoPosition[2]);
                                        arduinoDir = msgList[1].charAt(0);
                                        positionId = corToId(po_x, po_y);
                                        mDeviceMessagesListAdapter.add("Android Position at: " + po_x + ", " + po_y +
                                                " || Direction: " + arduinoDir);
                                        Log.d(TAG, "android position at: " + po_x + "," + po_y);
                                        if (manual_display_mode) {
                                            break;
                                        }
                                        updateArena();
                                        updatePosition();
                                        break;
                                    case "FPathEnd":
                                        set_parse_mode(ParsingModeIdle);
                                        deactivateButton(fastpath_button);
                                        break;
                                }
                                break;
                        }
                        break;

                }
                return false;
            }catch (Throwable t) {
                Log.e(TAG,null, t);
            }
            return false;
        }
    };

    private String printArrows() {
        String arrowString = "Arrows found: ";
        for (Integer[] i: arrowId) {
            arrowString += "[" + getCol(i[0]) + "," + getRow(i[0]) + "," + Character.forDigit(i[1], 36) + "] ";
            Log.d(TAG, "arrow dir: " + i[1]);

        }
        return arrowString;
    }

    private void set_parse_mode(int pMode) {
        parsingMode = pMode;
        switch (parsingMode){
            case ParsingModeIdle:
                explore_button.setEnabled(true);
                startpoint_button.setEnabled(true);
                waypoint_button.setEnabled(true);

                fastpath_button.setEnabled(false);
                update_button.setEnabled(false);
                auto_button.setEnabled(false);
                manual_button.setEnabled(false);
                cancel_button.setEnabled(false);
                break;

            case ParsingModeExplore:
                manual_button.setEnabled(true);
                cancel_button.setEnabled(true);

                mode = ModeIdle;
                auto_button.setEnabled(false);
                activateButton(auto_button);
                fastpath_button.setEnabled(false);
                explore_button.setEnabled(false);
                update_button.setEnabled(false);
                startpoint_button.setEnabled(false);
                waypoint_button.setEnabled(false);
                set_mode(ModeIdle);
                break;


            case ParsingModeCal:

                fastpath_button.setEnabled(false);
                explore_button.setEnabled(false);
                auto_button.setEnabled(false);
                manual_button.setEnabled(false);
                update_button.setEnabled(false);
                cancel_button.setEnabled(false);
                startpoint_button.setEnabled(false);
                waypoint_button.setEnabled(false);
                break;

            case ParsingModeFPath:
                manual_button.setEnabled(true);
                cancel_button.setEnabled(true);

                mode = ModeIdle;
                auto_button.setEnabled(false);
                activateButton(auto_button);
                fastpath_button.setEnabled(false);
                explore_button.setEnabled(false);
                update_button.setEnabled(false);
                startpoint_button.setEnabled(false);
                waypoint_button.setEnabled(false);
                waypoint_button.setText("Waypoint");
                updateArena();
                updatePosition();
                break;
        }
    }

    private void updatePosition(){
        oldPositionId = positionId;
        mArena.showArduinoPosition();
    }

//    po,1,1;d;FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F;00000100001C80000000001C0000080000060001C00000080000
//    po,1,1;d;FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF;000000000400000001C800000000000700000001000000003F00000E00000000040000000000
    private void updateArena(){
        Log.d("mapdesc2", mapDescriptor2);
        BigInteger bi1 = new BigInteger(mapDescriptor1, 16);
        String mapDescriptor1Bin = bi1.toString(2);
        String mapDescriptor2Bin = "";

        Log.d("mapdesc2", mapDescriptor2);
        String MDS2length = "%" + mapDescriptor2.length()*4 + "s";
        BigInteger bi2 = new BigInteger(mapDescriptor2, 16);
        mapDescriptor2Bin = bi2.toString(2);
        mapDescriptor2Bin = String.format(MDS2length, mapDescriptor2Bin).replace(" ", "0");

        int descriptor2Ptr = 0;
        for (int i=2;i<302;i++){
            int id = posToId(i);
            if(mapDescriptor1Bin.charAt(i)=='0'){
                mArena.setColor(id,"#CCCCCC","single","");
            }
            else if(mapDescriptor1Bin.charAt(i)=='1'){
                if(mapDescriptor2Bin.charAt(descriptor2Ptr)=='0'){
                    mArena.setColor(id,"#FFEBCD","single","");
                }
                else if (mapDescriptor2Bin.charAt(descriptor2Ptr)=='1'){
                    mArena.setColor(id,"#000000","single","");
                }
                descriptor2Ptr++;
            }
        }

        mArena.showArrows();
    }

    private int posToId(int i) {
        i-=2;
        return (19-i/15)*16+(i%15)+1;
    }

    private void discoverArrow(int ar_x, int ar_y, char ar_dir) {
        Integer[] tmp = {corToId(ar_x,ar_y), Character.getNumericValue(ar_dir)};
        Log.d(TAG, "discover arrow: " + tmp[0] +" " + tmp[1]);
        arrowId.add(tmp);

    }

    private Integer corToId(int po_x, int po_y) {
        Integer cor=(po_x + 1) + (19 - po_y) * 16;
        return cor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View exploreView = inflater.inflate(R.layout.fragment_explore, container, false);

        startpoint_button = exploreView.findViewById(R.id.startpoint_button);
        sendsp_button = exploreView.findViewById(R.id.sendsp_button);
        waypoint_button = exploreView.findViewById(R.id.waypoint_button);
        sendwp_button = exploreView.findViewById(R.id.sendwp_button);
        reset_button = exploreView.findViewById(R.id.reset_button);
//        button_status = exploreView.findViewById(R.id.button_status);
        fastpath_button = exploreView.findViewById(R.id.fastpath_button);
        explore_button = exploreView.findViewById(R.id.explore_button);
        auto_button = exploreView.findViewById(R.id.auto_button);
        manual_button = exploreView.findViewById(R.id.manual_button);
        update_button = exploreView.findViewById(R.id.update_button);
        cancel_button = exploreView.findViewById(R.id.cancel_button);
        sendwp_button = exploreView.findViewById(R.id.sendwp_button);
        calibrate_button = exploreView.findViewById(R.id.calibrate_button);

        mDeviceMessages = (ListView) exploreView.findViewById(R.id.MsgReceived);
        mDeviceMessagesListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mDeviceMessages.setAdapter(mDeviceMessagesListAdapter);

        waypoint_coord = exploreView.findViewById(R.id.waypoint_coord);
        wp_sp = ((MainActivity)getActivity()).getSharedPreference(MainActivity.WP_SP);
        wayPointId = wp_sp.getInt("sp_waypoint_id",1);
        wp_str = "(" + getCol(wayPointId).toString() + ", " +  getRow(wayPointId).toString() + ")";
        waypoint_coord.setText(wp_str);

        startpoint_coord = exploreView.findViewById(R.id.startpoint_coord);
        sp_sp = ((MainActivity)getActivity()).getSharedPreference(MainActivity.SP_SP);
        startPointId = sp_sp.getInt("sp_startpoint_id",1);
        sp_str = "(" + getCol(startPointId).toString() + ", " +  getRow(startPointId).toString() + ")";
        startpoint_coord.setText(sp_str);
        positionId = startPointId;

        mdf1_sp = ((MainActivity)getActivity()).getSharedPreference(MainActivity.MDF1);
        mapDescriptor1 = mdf1_sp.getString("sp_mdf1","N/A");
        mdf2_sp = ((MainActivity)getActivity()).getSharedPreference(MainActivity.MDF2);
        mapDescriptor2 = mdf2_sp.getString("sp_mdf2","N/A");
        image_sp = ((MainActivity)getActivity()).getSharedPreference(MainActivity.ARROW);
        arrowString = image_sp.getString("sp_arrow","N/A");

        fastpath_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String start_message = "AL fp_start";
                mBluetoothConnectionService.write(start_message.getBytes());
                set_parse_mode(ParsingModeFPath);
                activateButton(fastpath_button);
            }
        });

        explore_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set_parse_mode(ParsingModeExplore);

                sleep(2000);

                String start_message = "AL exp_start";
                mBluetoothConnectionService.write(start_message.getBytes());

                activateButton(explore_button);
            }
        });

        auto_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // auto update arduino position on gridlayout
                auto_button.setEnabled(false);
                manual_button.setEnabled(true);
                update_button.setEnabled(false);

                manual_display_mode = false;
                deactivateButton(manual_button);
                activateButton(auto_button);
            }
        });

        manual_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop auto updating (if it is auto updating)
                auto_button.setEnabled(true);
                update_button.setEnabled(true);
                manual_button.setEnabled(false);

                manual_display_mode = true;
                deactivateButton(auto_button);
                activateButton(manual_button);
            }
        });

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // updates gridlayout whenever this button is pressed, IN ORDER OF PRIORITY
                updateArena();
                updatePosition();
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // end current algorithm
                set_parse_mode(ParsingModeIdle);
                deactivateButton(explore_button);
                deactivateButton(fastpath_button);
                deactivateButton(auto_button);
                deactivateButton(manual_button);
            }
        });

        startpoint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode!=ModeStartPoint) {
                    set_mode(ModeStartPoint);
                    startpoint_button.setText("Back To Idle");
                    activateButton(startpoint_button);
                    waypoint_button.setText("set way point");
                    deactivateButton(waypoint_button);
                }
                else {
                    set_mode(ModeIdle);
                    startpoint_button.setText("set start point");
                    deactivateButton(startpoint_button);
                }
            }
        });

        sendsp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startPointId == 0) {
                    Toast toast = Toast.makeText(getContext(),
                            "Please pick a startpoint!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    String start_point_message = "AL sp[" + getCol(startPointId).toString() + "," +  getRow(startPointId).toString() + "," + start_direction + "]";
                    mBluetoothConnectionService.write(start_point_message.getBytes());
                }
            }
        });

        waypoint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode!=ModeWayPoint) {
                    set_mode(ModeWayPoint);
                    waypoint_button.setText("Back To Idle");
                    activateButton(waypoint_button);
                    startpoint_button.setText("set start point");
                    deactivateButton(startpoint_button);
                }
                else {
                    set_mode(ModeIdle);
                    waypoint_button.setText("set way point");
                    deactivateButton(waypoint_button);
                }
            }
        });

        sendwp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wayPointId == 0) {
                    Toast toast = Toast.makeText(getContext(),
                            "Please pick a waypoint!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    String send_wp_message = "AL wp[" + getCol(wayPointId).toString() + "," +  getRow(wayPointId).toString() + "]";
                    mBluetoothConnectionService.write(send_wp_message.getBytes());
                }
            }
        });

        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int row=0; row < 20; row++){
                    for (int col = 1; col <16; col++){
                        mArena.setColor(row*16+col,"#CCCCCC","single","");
                    }
                }

                if (startPointId > 0 && startPointId <= 320) {
                    mArena.setColor(startPointId, "#00FF00", "bordered", "#CCFFCC");
                }

                startpoint_button.setText("set start point");
                deactivateButton(startpoint_button);
                waypoint_button.setText("set way point");
                deactivateButton(waypoint_button);
            }
        });

        calibrate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String calibrate_message = "AD 1,0,0";
                mBluetoothConnectionService.write(calibrate_message.getBytes());
            }
        });

        return exploreView;
    }

    private void activateButton(Button button) {
        button.getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
    }

    private void deactivateButton(Button button) {
        button.getBackground().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorButtonNormal), PorterDuff.Mode.MULTIPLY);
    }

    private Integer getCol(int id) {
        return id%16-1;
    }
    private Integer getRow(int id) {
        return 20-id/16-1;
    }

    public void set_mode(int choice) {
        mode = choice;
        switch (choice){
            case ModeIdle:
//                button_status.setText("None");
                startpoint_button.setText("set start point");
                deactivateButton(startpoint_button);
                waypoint_button.setText("set way point");
                deactivateButton(waypoint_button);
                break;
            case ModeWayPoint:
//                button_status.setText("Select WayPoint");
                break;
            case ModeStartPoint:
//                button_status.setText("Select StartPoint");
                break;
        }
    }

    public void sleep (int millisecond){
        try
        {
            Thread.sleep(millisecond);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GridLayout arenaLayout = (GridLayout)getActivity().findViewById(R.id.arena);
        mArena = new Arena(getActivity(),arenaLayout);

        if (startPointId > 0 && startPointId <= 320) {
            mArena.setColor(startPointId, "#00FF00", "bordered", "#CCFFCC");
        }
    }

    private class Arena {
        Context mContext;
        GridLayout mmArena;
        Integer mCount=0;
        Integer mCol=0;
        Integer mRow=0;
        TextView grid;

        public Arena(Context context, GridLayout arena){
            mContext= context;
            mmArena = arena;
            createArena();
        }

        private void createArena() {
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#CCCCCC"));
            background.setStroke(1,Color.parseColor("#000000"));
            for (int row=0;row<21; row++){
                for(int col=0; col<16; col++){
                    grid = new TextView(mContext);
                    grid.setId(mCount);
                    grid.setWidth(40);
                    grid.setHeight(40);
                    grid.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

                    final int gridId = grid.getId();
                    mCol = getCol(mCount);
                    mRow = getRow(mCount);
                    if(mCol==0&&mRow==0) {}
                    else if(mCol==0){
                        Integer tmp=mRow-1;
                        grid.setText(tmp.toString());
                    }
                    else if(mRow==0){
                        Integer tmp=mCol-1;
                        grid.setText(tmp.toString());
                    }
                    else {
                        grid.setBackground(background);
                    }

                    final Integer finalmCount = mCount;
                    if (!(mCol == 0 || mRow == 0)) {
                        grid.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (mode) {
                                    case ModeIdle:
                                        break;
                                    case ModeWayPoint:
                                        if (gridId == startPointId) {
                                            removeStartPoint(startPointId);
                                        }
                                        removeWayPoint(wayPointId);
                                        setWayPoint(gridId);
                                        break;
                                    case ModeStartPoint:
                                        if (gridId == wayPointId) {
                                            removeWayPoint(wayPointId);
                                        }
                                        removeStartPoint(startPointId);
                                        setStartPoint(gridId);
                                        break;
                                }
                                if (startPointId != 0 && parsingMode == ParsingModeIdle) {
                                    explore_button.setEnabled(true);
                                } else {
                                    explore_button.setEnabled(false);
                                }
                                Log.d("gridid", ((Integer)gridId).toString());
                            }
                        });
                    }
                    mmArena.addView(grid);
                    mCount++;
                }
            }
        }

        private void removeWayPoint(int id) {
            setColor(wayPointId, "#CCCCCC", "single", "");

            wayPointId = 0;
            wp_str = "-";
            waypoint_coord.setText("-");

//            wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_wp_sp = wp_sp.edit();
            edit_wp_sp.putInt("sp_waypoint_id", 1);
            edit_wp_sp.commit();
        }

        private void setWayPoint(Integer id) {
            wayPointId = id;
            if (mapDescriptor1 == null){
                setColor(wayPointId, "#FF0000", "single", "");
            }
            else {
                updateArena();
                updatePosition();
            }

            wp_str = "(" + (getCol(id) - 1) + ", " + (getRow(id) - 1) + ")";
            waypoint_coord.setText(wp_str);

//            wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_wp_sp = wp_sp.edit();
            edit_wp_sp.putInt("sp_waypoint_id", wayPointId);
            edit_wp_sp.commit();
        }

        private void removeStartPoint(int id) {
            setColor(startPointId, "#CCCCCC", "bordered", "#CCCCCC");

            startPointId = 0;
            sp_str = "-";
            startpoint_coord.setText("-");

            SharedPreferences.Editor edit_sp_sp = sp_sp.edit();
            edit_sp_sp.putInt("sp_startpoint_id", 290);
            edit_sp_sp.commit();
        }

        private void setStartPoint(Integer id) {
            startPointId = id;
            setColor(startPointId, "#00FF00", "bordered", "#CCFFCC");

            sp_str = "(" + (getCol(id) - 1) + ", " + (getRow(id) - 1) + ")";
            startpoint_coord.setText(sp_str);

            SharedPreferences.Editor edit_sp_sp = sp_sp.edit();
            edit_sp_sp.putInt("sp_startpoint_id", startPointId);
            edit_sp_sp.commit();
        }

        private void showArduinoPosition() {
            if (oldPositionId == null) {
            }
            else {
                setColor(oldPositionId, "#CCCCCC", "bordered", "#CCCCCC");
            }
            setColor(startPointId, "#00FF00", "bordered", "#CCFFCC");
            setColor(positionId, "#7EC0EE", "robot", "#7EC0EE");
            if(parsingMode==ParsingModeFPath||parsingMode==ParsingModeCal){
                    setColor(wayPointId, "#FF0000", "single", "");
            }
        }

        public void showArrows() {
            for(Integer[] i: arrowId){
                GradientDrawable background = new GradientDrawable();
                background.setStroke(1, Color.parseColor("#000000"));
                Integer cor = i[0];
                Integer dir = i[1];
                Drawable arrow_u = ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_u, null);
                mmArena.getChildAt(cor).setBackground(arrow_u);
            }
        }

        public void setColor(int id, String bgColour, String type, String bbgColour) {
            switch (type){
                case ("single"): {
                    GradientDrawable background = new GradientDrawable();
                    background.setColor(Color.parseColor(bgColour));
                    background.setStroke(1, Color.parseColor("#000000"));
                    mmArena.getChildAt(id).setBackground(background);
                    break;
                }

                case ("bordered"): {
                    GradientDrawable background = new GradientDrawable();
                    background.setColor(Color.parseColor(bgColour));
                    if (id%16 == 0 || id >= 320){
                        background.setStroke(1,Color.parseColor("#FFFFFF"));
                    }
                    else {
                        background.setStroke(1,Color.parseColor("#000000"));
                    }
                    mmArena.getChildAt(id).setBackground(background);

                    int[] surrounding_list = new int[8];
                    surrounding_list[0] = id - 17;
                    surrounding_list[1] = id - 16;
                    surrounding_list[2] = id - 15;
                    surrounding_list[3] = id - 1;
                    surrounding_list[4] = id + 1;
                    surrounding_list[5] = id + 15;
                    surrounding_list[6] = id + 16;
                    surrounding_list[7] = id + 17;

                    for (int i = 0; i < surrounding_list.length; i++) {
                        if (surrounding_list[i] % 16 <= 0 || surrounding_list[i] >= 320) {
                        } else {
                            GradientDrawable border_background = new GradientDrawable();
                            border_background.setColor(Color.parseColor(bbgColour));
                            border_background.setStroke(1, Color.parseColor("#000000"));
                            mmArena.getChildAt(surrounding_list[i]).setBackground(border_background);
                        }
                    }
                    break;
                }

                case ("robot"): {
                    GradientDrawable background = new GradientDrawable();
                    if (id%16 == 0 || id >= 320){
                        background.setStroke(1,Color.parseColor("#FFFFFF"));
                    }
                    else {
                        background.setStroke(1,Color.parseColor("#000000"));
                    }
                    switch (arduinoDir){
                        //u
                        case 'u':
                            Drawable arrow_u = ContextCompat.getDrawable(getActivity(), R.drawable.robot_u);
                            mmArena.getChildAt(id).setBackground(arrow_u);
                            break;
                        //d
                        case 'd':
                            Drawable arrow_d = ContextCompat.getDrawable(getActivity(), R.drawable.robot_d);
                            mmArena.getChildAt(id).setBackground(arrow_d);
                            break;
                        //l
                        case 'l':
                            Drawable arrow_l = ContextCompat.getDrawable(getActivity(), R.drawable.robot_l);
                            mmArena.getChildAt(id).setBackground(arrow_l);
                            break;
                        //r
                        case 'r':
                            Drawable arrow_r = ContextCompat.getDrawable(getActivity(), R.drawable.robot_r);
                            mmArena.getChildAt(id).setBackground(arrow_r);
                            break;
                    }

                    int[] surrounding_list = new int[8];
                    surrounding_list[0] = id - 17;
                    surrounding_list[1] = id - 16;
                    surrounding_list[2] = id - 15;
                    surrounding_list[3] = id - 1;
                    surrounding_list[4] = id + 1;
                    surrounding_list[5] = id + 15;
                    surrounding_list[6] = id + 16;
                    surrounding_list[7] = id + 17;

                    for (int i = 0; i < surrounding_list.length; i++) {
                        if (surrounding_list[i] % 16 <= 0 || surrounding_list[i] >= 320) {
                        } else {
                            GradientDrawable border_background = new GradientDrawable();
                            border_background.setColor(Color.parseColor(bbgColour));
                            border_background.setStroke(1, Color.parseColor("#000000"));
                            mmArena.getChildAt(surrounding_list[i]).setBackground(border_background);
                        }
                    }
                    break;
                }
            }
        }

        private int getRow(Integer mCount) {
            return 20-mCount/16;
        }
        private int getCol(Integer mCount) {
            return mCount%16;
        }
    }
}
// todo deal with null mapdesc strings
// todo flow of algo -> explore to fastpath
// todo json for amdtool (send and receive MDS)

package com.mdp.mdpandroidapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;

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
    private TextView algo_mode;

    private boolean manual_display_mode = false;
    private Integer positionId;
    private Integer oldPositionId;
    private ArrayList<Integer[]> arrowId;
    private String mapDescriptor1;
    private String mapDescriptor2;
    private char arduinoDir = 'u';

    // wp sp portion
    private Button waypoint_button;
    private Button startpoint_button;
//    private Button reset_button;
    private TextView button_status;
    int mode = 0;
    static final int ModeWayPoint = 1;
    static final int ModeStartPoint = 2;
    static final int ModeIdle =0;

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
                        //  Reading message from remote device
                        String receivedMessage = message.obj.toString();
                        String msg1 = receivedMessage.substring(2, receivedMessage.length() - 2);
                        //b'x'
                        Log.d("msg1", msg1);
                        String[] msg = msg1.split(";");
                        String[] arduinoPosition = msg[0].split(",");
                        arduinoDir = msg[1].charAt(0);
                        mapDescriptor1 = msg[2];
                        mapDescriptor2 = msg[3];
                        switch (arduinoPosition[0]){
                            case "po":
                                if(mapDescriptor1==null){break;}
                                int po_x = Integer.parseInt(arduinoPosition[1]);
                                int po_y = Integer.parseInt(arduinoPosition[2]);
                                positionId = corToId(po_x,po_y);
                                mDeviceMessagesListAdapter.add("android position at: " + po_x + "," + po_y +
                                        "\ndirection:" + arduinoDir +
                                        "\n1st descriptor: "+mapDescriptor1 +
                                        "\n2nd descriptor: "+mapDescriptor2);
                                Log.d(TAG,"android position at: " + po_x + "," + po_y);
                                if(manual_display_mode){break;}
                                updateArena();
                                updatePosition(positionId,arduinoDir);
                                break;
                            case "ar":
                                int ar_x = Integer.parseInt(arduinoPosition[1]);
                                int ar_y = Integer.parseInt(arduinoPosition[2]);
                                Log.d(TAG,"discovered arrow: "+ar_x+","+ar_y+","+
                                        "\ndirection: " + arduinoDir);
                                discoverArrow(ar_x,ar_y,arduinoDir);
                                mDeviceMessagesListAdapter.add("discovered arrow: " + ar_x + "," + ar_y + "," + arduinoDir);
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

    private void updatePosition(int positionId,char dir){
        oldPositionId = positionId;
        if(!manual_display_mode){
            mArena.showArduinoPosition(dir);
        }
    }

//    po,1,1;d;FFC07F80FF01FE03FFFFFFF3FFE7FFCFFF9C7F38FE71FCE3F87FF0FFE1FFC3FF87FF0E0E1C1F;00000100001C80000000001C0000080000060001C00000080000
//    po,1,1;d;FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF;000000000400000001C800000000000700000001000000003F00000E00000000040000000000
    private void updateArena(){
        BigInteger bi1 = new BigInteger(mapDescriptor1, 16);
        String mapDescriptor1Bin = bi1.toString(2);
        Log.d("mapdesc1", mapDescriptor1Bin);

        String mapDescriptor2Bin = "";
        if (mapDescriptor2.charAt(0) == '0'){
            mapDescriptor2 = '1' + mapDescriptor2;
            BigInteger bi2 = new BigInteger(mapDescriptor2, 16);
            String mapDescriptor2BinTMP = bi2.toString(2);
            mapDescriptor2Bin = mapDescriptor2BinTMP.substring(1);
        }
        else {
            BigInteger bi2 = new BigInteger(mapDescriptor2, 16);
            mapDescriptor2Bin = bi2.toString(2);
        }

        int descriptor2Ptr = 0;
        for (int i=2;i<302;i++){
            Log.d("mapdesc", ((Integer)i).toString());
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
                Log.d("descriptor2Ptr", ((Integer)descriptor2Ptr).toString());
            }
            Log.d("mapdesclength2", ((Integer)mapDescriptor2Bin.length()).toString());
        }
    }

    private int posToId(int i) {
        i-=2;
        return (19-i/15)*16+(i%15)+1;
    }

    private void discoverArrow(int ar_x, int ar_y, char ar_dir) {
        Integer[] tmp = {corToId(ar_x,ar_y), Character.getNumericValue(ar_dir)};
        arrowId.add(tmp);
        if(!manual_display_mode) {
            mArena.showArrows();
        }
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

        waypoint_button = exploreView.findViewById(R.id.waypoint_button);
        startpoint_button = exploreView.findViewById(R.id.startpoint_button);
//        reset_button = exploreView.findViewById(R.id.reset_button);
        button_status = exploreView.findViewById(R.id.button_status);
        fastpath_button = exploreView.findViewById(R.id.fastpath_button);
        explore_button = exploreView.findViewById(R.id.explore_button);
        auto_button = exploreView.findViewById(R.id.auto_button);
        manual_button = exploreView.findViewById(R.id.manual_button);
        update_button = exploreView.findViewById(R.id.update_button);
        cancel_button = exploreView.findViewById(R.id.cancel_button);
        algo_mode = exploreView.findViewById(R.id.algo_mode);

        mDeviceMessages = (ListView) exploreView.findViewById(R.id.MsgReceived);
        mDeviceMessagesListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mDeviceMessages.setAdapter(mDeviceMessagesListAdapter);

        waypoint_coord = exploreView.findViewById(R.id.waypoint_coord);
        wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
        wayPointId = wp_sp.getInt("wp_sp",1);
        wp_str = "(" + getCol(wayPointId).toString() + ", " +  getRow(wayPointId).toString() + ")";
        waypoint_coord.setText(wp_str);

        startpoint_coord = exploreView.findViewById(R.id.startpoint_coord);
        sp_sp = getActivity().getSharedPreferences("sp_sp", Context.MODE_PRIVATE);
        startPointId = sp_sp.getInt("sp_sp", 0);
        positionId = startPointId;
        String sp_msg = "StartPoint Coordinates: " + getCol(startPointId) + "," + getRow(startPointId);
        mBluetoothConnectionService.write(sp_msg.getBytes());
        sp_str = "(" + getCol(startPointId).toString() + ", " +  getRow(startPointId).toString() + ")";
        startpoint_coord.setText(sp_str);

        fastpath_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // execute fastest path algo
                fastpath_button.setEnabled(false);
                explore_button.setEnabled(false);
                cancel_button.setEnabled(true);
                algo_mode.setText("Fastest Path");

                String start_point_message = "sp[" + getCol(startPointId).toString() + ", " +  getRow(startPointId).toString() + "]";
                mBluetoothConnectionService.write(start_point_message.getBytes());
                String way_point_message = "wp[" + getCol(wayPointId).toString() + ", " +  getRow(wayPointId).toString() +"]";
                mBluetoothConnectionService.write(way_point_message.getBytes());
                String start_message = "AL fp_start";
                mBluetoothConnectionService.write(start_message.getBytes());
            }
        });

        explore_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // execute exploration algo
                explore_button.setEnabled(false);
                fastpath_button.setEnabled(false);
                auto_button.setEnabled(true);
                manual_button.setEnabled(true);
                cancel_button.setEnabled(true);
                algo_mode.setText("Explore\n\nAuto");

                String start_point_message = "AL sp[" + getCol(startPointId).toString() + "," +  getRow(startPointId).toString() + "," + start_direction + "]";
                mBluetoothConnectionService.write(start_point_message.getBytes());
                String start_message = "AL exp_start";
                mBluetoothConnectionService.write(start_message.getBytes());

            }
        });

        auto_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // auto update arduino position on gridlayout

                update_button.setEnabled(false);
                algo_mode.setText("Explore\n\nAuto");

                manual_display_mode = false;
            }
        });

        manual_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop auto updating (if it is auto updating)

                update_button.setEnabled(true);
                algo_mode.setText("Explore\n\nManual");

                manual_display_mode = true;
            }
        });

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // updates gridlayout whenever this button is pressed, IN ORDER OF PRIORITY

                updateArena();
                updatePosition(positionId,arduinoDir);
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // end current algorithm

                fastpath_button.setEnabled(true);
                explore_button.setEnabled(true);
                auto_button.setEnabled(false);
                manual_button.setEnabled(false);
                update_button.setEnabled(false);
                cancel_button.setEnabled(false);
                algo_mode.setText("Stationary");

            }
        });

        waypoint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode!=ModeWayPoint) {
                    set_mode(ModeWayPoint);
                    waypoint_button.setText("Back To Idle");
                    startpoint_button.setText("StartPoint");
                }
                else {
                    set_mode(ModeIdle);
                    waypoint_button.setText("WayPoint");
                }
            }
        });

        startpoint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode!=ModeStartPoint) {
                    set_mode(ModeStartPoint);
                    startpoint_button.setText("Back To Idle");
                    waypoint_button.setText("WayPoint");
                }
                else {
                    set_mode(ModeIdle);
                    startpoint_button.setText("StartPoint");
                }
            }
        });

//        reset_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mArena.removeStartPoint(startPointId);
//                mArena.removeWayPoint(wayPointId);
//                set_mode(ModeIdle);
//                button_status.setText("None");
//                startpoint_button.setText("StartPoint");
//                waypoint_button.setText("WayPoint");
//
//                wayPointId = 1;
//                startPointId = 290;
//
//                explore_button.setEnabled(false);
//                fastpath_button.setEnabled(false);
//                auto_button.setEnabled(false);
//                manual_button.setEnabled(false);
//                update_button.setEnabled(false);
//                cancel_button.setEnabled(false);
//                algo_mode.setText("Stationary");
//
//                wp_str = "-";
//                sp_str = "-";
//
//                wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
//                SharedPreferences.Editor edit_wp_sp = wp_sp.edit();
//                edit_wp_sp.putInt("wp_sp", 1);
//                edit_wp_sp.commit();
//                waypoint_coord.setText(wp_str);
//                sp_sp = getActivity().getSharedPreferences("sp_sp", Context.MODE_PRIVATE);
//                SharedPreferences.Editor edit_sp_sp = sp_sp.edit();
//                edit_sp_sp.putInt("sp_sp", 290);
//                edit_sp_sp.commit();
//                startpoint_coord.setText(sp_str);
//            }
//        });

        return exploreView;
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
                button_status.setText("None");
                break;
            case ModeWayPoint:
                button_status.setText("Select WayPoint");
                break;
            case ModeStartPoint:
                button_status.setText("Select StartPoint");
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GridLayout arenaLayout = (GridLayout)getActivity().findViewById(R.id.arena);
        Log.d("EFragment","checkpoint:"+Boolean.toString(arenaLayout==null));
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
            background.setColor(Color.parseColor("#FFFFFF"));
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
                                if (startPointId != 0) { //todo change condition
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
            setColor(wayPointId, "#FFFFFF", "single", "");

            wayPointId = 0;
            wp_str = "-";
            waypoint_coord.setText("-");

            wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_wp_sp = wp_sp.edit();
            edit_wp_sp.putInt("wp_sp", 1);
            edit_wp_sp.commit();
        }

        private void setWayPoint(Integer id) {
            wayPointId = id;
            setColor(wayPointId, "#FF0000", "single", "");

            wp_str = "(" + (getCol(id) - 1) + ", " + (getRow(id) - 1) + ")";
            waypoint_coord.setText(wp_str);

            wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_wp_sp = wp_sp.edit();
            edit_wp_sp.putInt("wp_sp", wayPointId);
            edit_wp_sp.commit();

            String wp_msg = "WayPoint Coordinates: " + (getCol(wayPointId)-1) + "," + (getRow(wayPointId)-1);
            mBluetoothConnectionService.write(wp_msg.getBytes());
        }

        private void removeStartPoint(int id) {
            setColor(startPointId, "#FFFFFF", "bordered", "#FFFFFF");

            startPointId = 0;
            sp_str = "-";
            startpoint_coord.setText("-");

            sp_sp = getActivity().getSharedPreferences("sp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_sp_sp = sp_sp.edit();
            edit_sp_sp.putInt("sp_sp", 290);
            edit_sp_sp.commit();
        }

        private void setStartPoint(Integer id) {
            startPointId = id;
            setColor(startPointId, "#00FF00", "bordered", "#CCFFCC");

            sp_str = "(" + (getCol(id) - 1) + ", " + (getRow(id) - 1) + ")";
            startpoint_coord.setText(sp_str);

            sp_sp = getActivity().getSharedPreferences("sp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_sp_sp = sp_sp.edit();
            edit_sp_sp.putInt("sp_sp", startPointId);
            edit_sp_sp.commit();

            String sp_msg = "StartPoint Coordinates: " + (getCol(startPointId)-1) + "," + (getRow(startPointId)-1);
            mBluetoothConnectionService.write(sp_msg.getBytes());
        }

        private void showArduinoPosition(char dir) {
            if (oldPositionId == null) {
            }
            else {
                setColor(oldPositionId, "#FFFFFF", "bordered", "#FFFFFF");
            }
            setColor(startPointId, "#00FF00", "bordered", "#CCFFCC");
            setColor(positionId, "#7EC0EE", "robot", "#7EC0EE");
            setColor(wayPointId, "#FF0000", "single", "");

            //todo: show direction of the arduino
            //todo remove wp in exp
        }

        public void showArrows() {
            for(Integer[] i: arrowId){
                GradientDrawable background = new GradientDrawable();
                background.setStroke(1, Color.parseColor("#000000"));
                Integer cor = i[0];
                Integer dir = i[1];
                switch (dir){
                    //u
                    case 30:
                        Drawable arrow_u = ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_u, null);
                        mmArena.getChildAt(cor).setBackground(arrow_u);
                        break;
                    //d
                    case 13:
                        Drawable arrow_d = ContextCompat.getDrawable(getActivity(), R.drawable.arrow_d);
                        mmArena.getChildAt(cor).setBackground(arrow_d);
                        break;
                    //l
                    case 21:
                        Drawable arrow_l = ContextCompat.getDrawable(getActivity(), R.drawable.arrow_l);
                        mmArena.getChildAt(cor).setBackground(arrow_l);
                        break;
                    //r
                    case 27:
                        Drawable arrow_r = ContextCompat.getDrawable(getActivity(), R.drawable.arrow_r);
                        mmArena.getChildAt(cor).setBackground(arrow_r);
                        break;
                }
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

                case ("robot"): { //todo robot color
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
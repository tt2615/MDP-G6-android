package com.mdp.mdpandroidapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;

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

    // wp sp portion
    private Button waypoint_button;
    private Button startpoint_button;
    private Button reset_button;
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
    int wayPointId = 0;
    int startPointId = 0;
    String wp_str = "-";
    String sp_str = "-";
    SharedPreferences wp_sp;
    SharedPreferences sp_sp;
    public static final String DEFAULTCOORD = "-";

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
                        switch (receivedMessage.substring(0,2)){
                            case "po":
                                int po_x = receivedMessage.charAt(3);
                                int po_y = receivedMessage.charAt(5);
                                Log.d(TAG,"android position at: "+po_x+","+po_y);
                                updatePosition(po_x,po_y);
                                mDeviceMessagesListAdapter.add("android position at: "+po_x+","+po_y);
                                break;
                            case "nt":
                                int nt_x = receivedMessage.charAt(3);
                                int nt_y = receivedMessage.charAt(5);
                                Log.d(TAG,"discovered normal terrain at: "+nt_x+","+nt_y);
                                discoverNormalTerrain(nt_x,nt_y);
                                mDeviceMessagesListAdapter.add("discovered normal terrain at: "+nt_x+","+nt_y);
                                break;
                            case "ob":
                                int ob_x = receivedMessage.charAt(3);
                                int ob_y = receivedMessage.charAt(5);
                                Log.d(TAG,"discovered obstacle: "+ob_x+","+ob_y);
                                discoverObstacle(ob_x,ob_y);
                                mDeviceMessagesListAdapter.add("discovered obstacle at: "+ob_x+","+ob_y);
                                break;
                            case "ar":
                                int ar_x = receivedMessage.charAt(3);
                                int ar_y = receivedMessage.charAt(5);
                                char ar_dir = receivedMessage.charAt(7);
                                Log.d(TAG,"discovered arrow: "+ar_x+","+ar_y+","+ar_dir);
                                discoverArrow(ar_x,ar_y,ar_dir);
                                mDeviceMessagesListAdapter.add("discovered arrow: "+ar_x+","+ar_y+","+ar_dir);
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

    private void updatePosition(int po_x, int po_y) {
        //todo: update map position
    }

    private void discoverNormalTerrain(int nt_x, int nt_y) {
        //todo: update discovered normal terrain position
    }

    private void discoverObstacle(int ob_x, int ob_y) {
        //todo update discovered obstacle position
    }

    private void discoverArrow(int ar_x, int ar_y, char ar_dir) {
        //todo update discovered arrow position and direction
    }






    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View exploreView = inflater.inflate(R.layout.fragment_explore, container, false);

        waypoint_button = exploreView.findViewById(R.id.waypoint_button);
        startpoint_button = exploreView.findViewById(R.id.startpoint_button);
        reset_button = exploreView.findViewById(R.id.reset_button);
        button_status = exploreView.findViewById(R.id.button_status);
        //todo: the default text can be edited in xml
        button_status.setText("None");
        fastpath_button = exploreView.findViewById(R.id.fastpath_button);
        explore_button = exploreView.findViewById(R.id.explore_button);
        auto_button = exploreView.findViewById(R.id.auto_button);
        manual_button = exploreView.findViewById(R.id.manual_button);
        update_button = exploreView.findViewById(R.id.update_button);
        cancel_button = exploreView.findViewById(R.id.cancel_button);
        algo_mode = exploreView.findViewById(R.id.algo_mode);

        waypoint_button = exploreView.findViewById(R.id.waypoint_button);
        startpoint_button = exploreView.findViewById(R.id.startpoint_button);
        reset_button = exploreView.findViewById(R.id.reset_button);
        button_status = exploreView.findViewById(R.id.button_status);
        button_status.setText("None");
        mDeviceMessages = (ListView) exploreView.findViewById(R.id.MsgReceived);
        mDeviceMessagesListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mDeviceMessages.setAdapter(mDeviceMessagesListAdapter);

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

        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArena.removeStartPoint(startPointId);
                mArena.removeWayPoint(wayPointId);
                set_mode(ModeIdle);
                button_status.setText("None");
                startpoint_button.setText("StartPoint");
                waypoint_button.setText("WayPoint");

                wayPointId = 0;
                startPointId = 0;
            }
        });

        waypoint_coord = exploreView.findViewById(R.id.waypoint_coord);
        wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
        wp_str = wp_sp.getString("wp_sp", DEFAULTCOORD);
        waypoint_coord.setText(wp_str);
        startpoint_coord = exploreView.findViewById(R.id.startpoint_coord);
        sp_sp = getActivity().getSharedPreferences("sp_sp", Context.MODE_PRIVATE);
        sp_str = sp_sp.getString("sp_sp", DEFAULTCOORD);
        startpoint_coord.setText(sp_str);

        fastpath_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // execute fastest path algo
                fastpath_button.setEnabled(false);
                explore_button.setEnabled(false);
                cancel_button.setEnabled(true);
                algo_mode.setText("Fastest Path");

                String start_message = "st[1]";
                mBluetoothConnectionService.write(start_message.getBytes());
                String start_point_message = "sp[" + mArena.mRow+1+","+mArena.mCol+1+"]";
                mBluetoothConnectionService.write(start_point_message.getBytes());
                String way_point_message = "wp[";
                mBluetoothConnectionService.write(way_point_message.getBytes());
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
            }
        });

        auto_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // auto update arduino position on gridlayout

                update_button.setEnabled(false);
                algo_mode.setText("Explore\n\nAuto");
            }
        });

        manual_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop auto updating (if it is auto updating)

                update_button.setEnabled(true);
                algo_mode.setText("Explore\n\nManual");
            }
        });

        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // updates gridlayout whenever this button is pressed
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

        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArena.removeStartPoint(startPointId);
                mArena.removeWayPoint(wayPointId);
                set_mode(ModeIdle);
                button_status.setText("None");
                startpoint_button.setText("StartPoint");
                waypoint_button.setText("WayPoint");

                wayPointId = 0;
                startPointId = 0;

                explore_button.setEnabled(false);
                fastpath_button.setEnabled(false);
                auto_button.setEnabled(false);
                manual_button.setEnabled(false);
                update_button.setEnabled(false);
                cancel_button.setEnabled(false);
                algo_mode.setText("Stationary");

                wp_str = "-";
                sp_str = "-";
                wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit_wp_sp = wp_sp.edit();
                edit_wp_sp.putString("wp_sp", DEFAULTCOORD);
                edit_wp_sp.commit();
                waypoint_coord.setText(wp_str);
                sp_sp = getActivity().getSharedPreferences("sp_sp", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit_sp_sp = sp_sp.edit();
                edit_sp_sp.putString("sp_sp", DEFAULTCOORD);
                edit_sp_sp.commit();
                startpoint_coord.setText(sp_str);
            }
        });

        return exploreView;
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
    }

    private class Arena {
        Context mContext;
        GridLayout mArena;
        Integer mCount=0;
        Integer mCol=0;
        Integer mRow=0;
        TextView grid;

        public Arena(Context context, GridLayout arena){
            mContext= context;
            mArena = arena;
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
                                if (startPointId != 0 && wayPointId != 0) {
                                    fastpath_button.setEnabled(true);
                                    explore_button.setEnabled(true);
                                } else {
                                    fastpath_button.setEnabled(false);
                                    explore_button.setEnabled(false);
                                }
                            }
                        });
                    }
                    mArena.addView(grid);
                    mCount++;
                }
            }
        }

        private void removeWayPoint(int id) {
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#FFFFFF"));
            if (id%16 <= 0 || id >= 320){
                background.setStroke(1,Color.parseColor("#FFFFFF"));
            }
            else {
                background.setStroke(1,Color.parseColor("#000000"));
            }
            mArena.getChildAt(id).setBackground(background);

            wayPointId = 0;
            wp_str = "-";
            waypoint_coord.setText("-");
            wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_wp_sp = wp_sp.edit();
            edit_wp_sp.putString("wp_sp", DEFAULTCOORD);
            edit_wp_sp.commit();
        }

        private void setWayPoint(Integer id) {
            wayPointId = id;
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#FF0000"));
            background.setStroke(1, Color.parseColor("#000000"));
            mArena.getChildAt(id).setBackground(background);

            wp_str = "(" + (getRow(id) - 1) + ", " + (getCol(id) - 1) + ")";
            waypoint_coord.setText(wp_str);
            wp_sp = getActivity().getSharedPreferences("wp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_wp_sp = wp_sp.edit();
            edit_wp_sp.putString("wp_sp", wp_str);
            edit_wp_sp.commit();
        }

        private void removeStartPoint(int id) {
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#FFFFFF"));
            if (id%16 == 0 || id >= 320){
                background.setStroke(1,Color.parseColor("#FFFFFF"));
            }
            else {
                background.setStroke(1,Color.parseColor("#000000"));
            }
            mArena.getChildAt(id).setBackground(background);

            int surrounding_list[] = new int[8];
            surrounding_list[0] = id - 17;
            surrounding_list[1] = id - 16;
            surrounding_list[2] = id - 15;
            surrounding_list[3] = id - 1;
            surrounding_list[4] = id + 1;
            surrounding_list[5] = id + 15;
            surrounding_list[6] = id + 16;
            surrounding_list[7] = id + 17;

            if (id != 0) {
                for (int i = 0; i < surrounding_list.length; i++) {
                    if (surrounding_list[i] % 16 <= 0 || surrounding_list[i] >= 320 || surrounding_list[i] == wayPointId) {
                    } else {
                        GradientDrawable faded_background = new GradientDrawable();
                        faded_background.setColor(Color.parseColor("#FFFFFF"));
                        faded_background.setStroke(1, Color.parseColor("#000000"));
                        mArena.getChildAt(surrounding_list[i]).setBackground(faded_background);
                    }
                }
            }

            startPointId = 0;
            sp_str = "-";
            startpoint_coord.setText("-");
            sp_sp = getActivity().getSharedPreferences("sp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_sp_sp = sp_sp.edit();
            edit_sp_sp.putString("sp_sp", DEFAULTCOORD);
            edit_sp_sp.commit();
        }

        private void setStartPoint(Integer id) {
            startPointId = id;
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#00FF00"));
            background.setStroke(1, Color.parseColor("#000000"));
            mArena.getChildAt(startPointId).setBackground(background);

            int surrounding_list[] = new int[8];
            surrounding_list[0] = id - 17;
            surrounding_list[1] = id - 16;
            surrounding_list[2] = id - 15;
            surrounding_list[3] = id - 1;
            surrounding_list[4] = id + 1;
            surrounding_list[5] = id + 15;
            surrounding_list[6] = id + 16;
            surrounding_list[7] = id + 17;

            for (int i = 0; i < surrounding_list.length; i++) {
                if (surrounding_list[i] % 16 <= 0 || surrounding_list[i] >= 320 || surrounding_list[i] == wayPointId) {
                } else {
                    GradientDrawable faded_background = new GradientDrawable();
                    faded_background.setColor(Color.parseColor("#99ff99"));
                    faded_background.setStroke(1, Color.parseColor("#000000"));
                    mArena.getChildAt(surrounding_list[i]).setBackground(faded_background);
                }
            }
            sp_str = "(" + (getRow(id) - 1) + ", " + (getCol(id) - 1) + ")";
            startpoint_coord.setText(sp_str);
            sp_sp = getActivity().getSharedPreferences("sp_sp", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit_sp_sp = sp_sp.edit();
            edit_sp_sp.putString("sp_sp", sp_str);
            edit_sp_sp.commit();
        }

        private int getRow(Integer mCount) {
            return 20-mCount/16;
        }
        private int getCol(Integer mCount) {
            return mCount%16;
        }
    }
}
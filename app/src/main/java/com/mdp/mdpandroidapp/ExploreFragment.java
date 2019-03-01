package com.mdp.mdpandroidapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    public ExploreFragment() {
        // Required empty public constructor
    }

    // algo buttons
    private Button fastpath_button;
    private Button explore_button;
    private Button auto_button;
    private Button manual_button;
    private Button update_button;
    private Button cancel_button;
    private TextView algo_mode;

    // wp sp buttons
    private Button waypoint_button;
    private Button startpoint_button;
    private Button reset_button;
    private TextView button_status;
    private Arena mArena;

    int mode = 0;
    static final int ModeWayPoint = 1;
    static final int ModeStartPoint = 2;
    static final int ModeIdle =0;

    // class variable for waypoint and startpoint
    int wayPointId = 0;
    int startPointId = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View exploreView = inflater.inflate(R.layout.fragment_explore, container, false);

        waypoint_button = exploreView.findViewById(R.id.waypoint_button);
        startpoint_button = exploreView.findViewById(R.id.startpoint_button);
        reset_button = exploreView.findViewById(R.id.reset_button);
        button_status = exploreView.findViewById(R.id.button_status);
        button_status.setText("None");
        fastpath_button = exploreView.findViewById(R.id.fastpath_button);
        explore_button = exploreView.findViewById(R.id.explore_button);
        auto_button = exploreView.findViewById(R.id.auto_button);
        manual_button = exploreView.findViewById(R.id.manual_button);
        update_button = exploreView.findViewById(R.id.update_button);
        cancel_button = exploreView.findViewById(R.id.cancel_button);
        algo_mode = exploreView.findViewById(R.id.algo_mode);

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
            }
        });

        fastpath_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // execute fastest path algo

                fastpath_button.setEnabled(false);
                explore_button.setEnabled(false);
                cancel_button.setEnabled(true);
                algo_mode.setText("Fastest Path");
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
                    grid.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch(mode){
                                case ModeIdle:
                                    break;
                                case ModeWayPoint:
                                    if (gridId == startPointId){
                                        removeStartPoint(startPointId);
                                    }
                                    removeWayPoint(wayPointId);
                                    setWayPoint(gridId);
                                    break;
                                case ModeStartPoint:
                                    if (gridId == wayPointId){
                                        removeWayPoint(wayPointId);
                                    }
                                    removeStartPoint(startPointId);
                                    setStartPoint(gridId);
                                    break;
                            }
                            if (startPointId != 0 && wayPointId != 0) {
                                fastpath_button.setEnabled(true);
                                explore_button.setEnabled(true);
                            }
                            else {
                                fastpath_button.setEnabled(false);
                                explore_button.setEnabled(false);
                            }
                        }
                    });
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
        }

        private void setWayPoint(Integer id) {
            wayPointId=id;
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#FF0000"));
            background.setStroke(1,Color.parseColor("#000000"));
            mArena.getChildAt(id).setBackground(background);
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
        }

        private int getRow(Integer mCount) {
            return 20-mCount/16;
        }

        private int getCol(Integer mCount) {
            return mCount%16;
        }
    }
}

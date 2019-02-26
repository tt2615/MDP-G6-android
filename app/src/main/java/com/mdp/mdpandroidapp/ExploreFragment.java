
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



/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    public ExploreFragment() {
        // Required empty public constructor
    }

    // waypoint button and startpoint button
    private Button waypoint_button;
    private Button startpoint_button;
    private TextView button_status;
    private Arena mArena;

    int mode = 0;
    static final int ModeWayPoint = 1;
    static final int ModeStartPoint = 2;
    static final int ModeIdle =0;

    int wayPointId = 0;
    int startPointId = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View exploreView = inflater.inflate(R.layout.fragment_explore, container, false);

        // instantiate the two buttons, clickable, and status
        waypoint_button = exploreView.findViewById(R.id.waypoint_button);
        startpoint_button = exploreView.findViewById(R.id.startpoint_button);
        button_status = exploreView.findViewById(R.id.button_status);
        button_status.setText("none");

        // setonclicklistener waypoint
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

        // setonclicklistener startpoint
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

    public void set_coord(int row, int col, String point){
        Log.d("set coordinates", "coordinates for " + point + " set!");
    }

    public void unset_coord(String point){
        Log.d("unset coordinates", "coordinates for " + point + " unset!");
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
                    if(mCol==0&&mRow==0){}
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
                                    removeWayPoint();
                                    setWayPoint(gridId);
                                    Log.d("Grid",Integer.toString(grid.getId()));
                                    break;
                                case ModeStartPoint:
                                    removeStartPoint();
                                    setStartPoint(gridId);
                                    break;
                            }
                        }
                    });
                    mArena.addView(grid);
                    mCount++;
                }
            }
        }

        private void removeWayPoint() {
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#FFFFFF"));
            background.setStroke(1,Color.parseColor("#000000"));
            mArena.getChildAt(wayPointId).setBackground(background);
        }

        private void setWayPoint(Integer id) {
            wayPointId=id;
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#FF0000"));
            background.setStroke(1,Color.parseColor("#000000"));
            mArena.getChildAt(id).setBackground(background);
        }

        private void removeStartPoint() {
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#FFFFFF"));
            background.setStroke(1,Color.parseColor("#000000"));
            mArena.getChildAt(startPointId).setBackground(background);
        }

        private void setStartPoint(Integer id) {
            startPointId=id;
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#00FF00"));
            background.setStroke(1,Color.parseColor("#000000"));
            mArena.getChildAt(startPointId).setBackground(background);
        }

        private int getRow(Integer mCount) {
            return 20-mCount/16;
        }

        private int getCol(Integer mCount) {
            return mCount%16;
        }


    }
}


package com.mdp.mdpandroidapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    public ExploreFragment() {
        // Required empty public constructor
    }

    public class Box {
        private Button click_box;
        String grid_status;
        int row, col;

        private Box() {
            grid_status = "unoccupied";
            row = 1;
            col = 1;
        }
    }
    private Box box = new Box();

    // waypoint button and startpoint button
    private Button waypoint_button;
    private Button startpoint_button;
    private TextView button_status;
    int waypoint_mode, startpoint_mode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View exploreView = inflater.inflate(R.layout.fragment_explore, container, false);

        // code for gridlayout
        // xxx

        // instantiate the two buttons, clickable, and status
        waypoint_button = exploreView.findViewById(R.id.waypoint_button);
        startpoint_button = exploreView.findViewById(R.id.startpoint_button);
        box.click_box = exploreView.findViewById(R.id.click_test);
        button_status = exploreView.findViewById(R.id.button_status);
        button_status.setText("none");

        // setonclicklistener waypoint
        waypoint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waypoint_mode == 0) {
                    set_mode("waypoint");
                }
                else {
                    set_mode("neither");
                }
            }
        });

        // setonclicklistener startpoint
        startpoint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startpoint_mode == 0) {
                    set_mode("startpoint");
                }
                else {
                    set_mode("neither");
                }
            }
        });

        // setonclicklistener click_test
        box.click_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (box.grid_status == "unoccupied") {
                    if (waypoint_mode == 1){
                        box.grid_status = "waypoint";
                        set_coord(box.row, box.col, "waypoint");
                    }
                    else if (startpoint_mode == 1){
                        box.grid_status = "startpoint";
                        set_coord(box.row, box.col, "startpoint");
                    }
                }

                else if (box.grid_status == "waypoint"){
                    if (waypoint_mode == 1){
                        // do nothing
                    }
                    else if (startpoint_mode == 1){
                        box.grid_status = "startpoint";
                        set_coord(box.row, box.col, "startpoint");
                        unset_coord("waypoint");
                    }
                }

                else if (box.grid_status == "startpoint"){
                    if (startpoint_mode == 1){
                        // do nothing
                    }
                    else if (waypoint_mode == 1){
                        box.grid_status = "waypoint";
                        set_coord(box.row, box.col, "waypoint");
                        unset_coord("startpoint");
                    }
                }

                // send coordinates and info to RPi
            }
        });

        return exploreView;
    }

    public void set_mode(String point) {
        if (point == "waypoint") {
            startpoint_mode = 0;
            waypoint_mode = 1;
            button_status.setText("waypoint mode");
        }
        else if (point == "startpoint"){
            waypoint_mode = 0;
            startpoint_mode = 1;
            button_status.setText("startpoint mode");
        }
        else if (point == "neither"){
            waypoint_mode = 0;
            startpoint_mode = 0;
            button_status.setText("none");
        }
    }

    public void set_coord(int row, int col, String point){
        Log.d("set coordinates", "coordinates for " + point + " set!");
    }

    public void unset_coord(String point){
        Log.d("unset coordinates", "coordinates for " + point + " unset!");
    }
}

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

    // waypoint button and startpoint button
    private Button waypoint_button;
    private Button startpoint_button;
    private TextView click_test;
    private TextView textview_status;
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
        click_test = exploreView.findViewById(R.id.click_test);
        textview_status = exploreView.findViewById(R.id.textview_status);

        // setonclicklistener waypoint
        waypoint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set("waypoint");
            }
        });

        // setonclicklistener startpoint
        startpoint_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set("startpoint");
            }
        });

        // setonclicklistener click_test
        click_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if textview_status == unoccupied
                    // if waypoint_mode == 1
                        // mark this as waypoint
                        // save waypoint coordinates
                        // waypoint_mode = 0
                    // elif startpoint_mode == 1
                        // mark this as startpoint
                        // save startpoint coordinates
                        // startpoint_mode = 0

                // if textview_status == waypoint | textview_status == startpoint
                    // if textview_status == waypoint
                        // if waypoint_mode == 1
                            // do nothing
                        // if startpoint_mode == 1
                            // mark this as startpoint
                            // save startpoint coordinates
                            // remove waypoint coordinates

                    // if textview_status == startpoint
                        // if startpoint_mode == 1
                            // do nothing
                        // if waypoint_mode == 1
                            // mark this as waypoint
                            // save waypoint coordinates
                            // remove startpoint coordinates

                // send coordinates and info to RPi
            }
        });

        return exploreView;
//        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    public void set(String point) {
        if (point == "waypoint") {
            startpoint_mode = 0;
            waypoint_mode = 1;
            textview_status.setText("waypoint mode");
            Log.d("setbutton", "waypoint set");
        }
        else if (point == "startpoint"){
            waypoint_mode = 0;
            startpoint_mode = 1;
            textview_status.setText("startpoint mode");
            Log.d("setbutton", "startpoint set");
        }
    }

}

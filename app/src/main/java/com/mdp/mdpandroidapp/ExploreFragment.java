package com.mdp.mdpandroidapp;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    Map mMap;


    public ExploreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_explore, container, false);


        GridLayout map = (GridLayout)getActivity().findViewById(R.id.map);
        mMap = new Map(map,this.getActivity());


        return rootView;


    }

    private class Map {
        GridLayout mmMap;
        Context mmContext;


        public Map(GridLayout map, Context context) {
            mmMap = map;
            mmContext = context;
            createMap(mmMap);
        }

        public void createMap(GridLayout map){
            TextView grid;
            Integer count = 1;

            for (int row = 0; row < 20; row++) {
                for (int col = 0; col < 15; col++) {
                    grid = new TextView((Activity)mmContext);
                    grid.setId(count);

                    grid.setText(count.toString());
                    Log.d("MAP",grid.getText().toString());
                    grid.setTextColor(Color.parseColor("#FFFF9F"));
                    grid.setWidth(34);
                    grid.setHeight(34);

                    map.addView(grid);
                    count++;
                }

            }
        }
    }
}

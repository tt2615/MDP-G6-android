package com.mdp.mdpandroidapp;

import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MDFFragment extends Fragment {

    public MDFFragment() {

    }

    SharedPreferences sp_waypoint;
    SharedPreferences sp_startpoint;
    SharedPreferences sp_MDF1;
    SharedPreferences sp_MDF2;
    SharedPreferences sp_image_str;

    public static final String DEFAULT = "N/A";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View MDFFragmentView = inflater.inflate(R.layout.fragment_mdf, container, false);

        TextView wp_str_tv = MDFFragmentView.findViewById(R.id.wp_display);
        TextView sp_str_tv = MDFFragmentView.findViewById(R.id.sp_display);
        TextView MDF_string_1_tv = MDFFragmentView.findViewById(R.id.MDF1_display);
        TextView MDF_string_2_tv = MDFFragmentView.findViewById(R.id.MDF2_display);
        TextView image_string_tv = MDFFragmentView.findViewById(R.id.image_string_display);

        sp_waypoint = ((MainActivity)getActivity()).getSharedPreference(MainActivity.WP_SP);
        int wp_str_id = sp_waypoint.getInt("sp_waypoint_id", 1);
        String wp_str = "(" + getCol(wp_str_id).toString() + ", " +  getRow(wp_str_id).toString() + ")";
        wp_str_tv.setText(wp_str);

        sp_startpoint = ((MainActivity)getActivity()).getSharedPreference(MainActivity.SP_SP);
        int sp_str_id = sp_startpoint.getInt("sp_startpoint_id", 1);
        String sp_str = "(" + getCol(sp_str_id).toString() + ", " +  getRow(sp_str_id).toString() + ")";
        sp_str_tv.setText(sp_str);

        sp_MDF1 = ((MainActivity)getActivity()).getSharedPreference(MainActivity.MDF1);
        String mdf1_str = sp_MDF1.getString("sp_mdf1", DEFAULT);
        MDF_string_1_tv.setText(mdf1_str);

        sp_MDF2 = ((MainActivity)getActivity()).getSharedPreference(MainActivity.MDF2);
        String mdf2_str = sp_MDF2.getString("sp_mdf2", DEFAULT);
        MDF_string_2_tv.setText(mdf2_str);

        sp_image_str = ((MainActivity)getActivity()).getSharedPreference(MainActivity.ARROW);
        String image_str = sp_image_str.getString("sp_arrow", DEFAULT);
        image_string_tv.setText(image_str);

        return MDFFragmentView;
    }

    private Integer getCol(int id) {
        return id%16-1;
    }
    private Integer getRow(int id) {
        return 20-id/16-1;
    }
}

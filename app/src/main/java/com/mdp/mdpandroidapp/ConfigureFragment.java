package com.mdp.mdpandroidapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigureFragment extends Fragment {

    public ConfigureFragment() {
        // Required empty public constructor
    }
    private Button configure_F1;
    private EditText text_F1;
    SharedPreferences sp_f1;

    private Button configure_F2;
    private EditText text_F2;
    SharedPreferences sp_f2;

    public static final String DEFAULT = "N/A";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragConfigureView = inflater.inflate(R.layout.fragment_configure, container, false);

        // initialize & instantiate textview to display the current F1 and F2 values
        TextView textview_f1 = fragConfigureView.findViewById(R.id.f1var);
        TextView textview_f2 = fragConfigureView.findViewById(R.id.f2var);

        // instantiate the EditText and Button for setting F1 and F2
        configure_F1 = fragConfigureView.findViewById(R.id.f1Configure);
        text_F1 = fragConfigureView.findViewById(R.id.f1Value);
        configure_F2 = fragConfigureView.findViewById(R.id.f2Configure);
        text_F2 = fragConfigureView.findViewById(R.id.f2Value);

        // display the persistent string for F1 and F2 when the ConfigureFragment is run
        sp_f1 = getActivity().getSharedPreferences("f1_user_preferences", Context.MODE_PRIVATE);
        String f1_str = sp_f1.getString("f1_user_preferences", DEFAULT);
        textview_f1.setText(f1_str);
        sp_f2 = getActivity().getSharedPreferences("f2_user_preferences", Context.MODE_PRIVATE);
        String f2_str = sp_f2.getString("f2_user_preferences", DEFAULT);
        textview_f2.setText(f2_str);

        // set string for F1
        configure_F1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String user_input_f1 = text_F1.getText().toString();
                SharedPreferences sp_f1 = getActivity().getSharedPreferences("f1_user_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit_sp_f1 = sp_f1.edit();
                edit_sp_f1.putString("f1_user_preferences", user_input_f1);
                edit_sp_f1.commit();
                load_f1();
            }
        });

        // set string for F2
        configure_F2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String user_input_f2 = text_F2.getText().toString();
                SharedPreferences sp_f2 = getActivity().getSharedPreferences("f2_user_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit_sp_f2 = sp_f2.edit();
                edit_sp_f2.putString("f2_user_preferences", user_input_f2);
                edit_sp_f2.commit();
                load_f2();
            }
        });

        return fragConfigureView;
    }

    // these two functions get the sharedprefs data and refreshes the current textview for f1 and f2 respectively
    // load_f1 is run whenever the configure button for F1 is pressed
    public void load_f1() {
        SharedPreferences sp_f1 = getActivity().getSharedPreferences("f1_user_preferences", Context.MODE_PRIVATE);
        TextView textview_f1 = getActivity().findViewById(R.id.f1var);
        String f1_str = sp_f1.getString("f1_user_preferences", DEFAULT);
        textview_f1.setText(f1_str);
    }

    // load_f2 is run whenever the configure button for F2 is pressed
    public void load_f2() {
        SharedPreferences sp_f2 = getActivity().getSharedPreferences("f2_user_preferences", Context.MODE_PRIVATE);
        TextView textview_f2 = getActivity().findViewById(R.id.f2var);
        String f2_str = sp_f2.getString("f2_user_preferences", DEFAULT);
        textview_f2.setText(f2_str);
    }
}

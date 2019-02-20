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

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigureFragment extends Fragment {

    public ConfigureFragment() {
        // Required empty public constructor
    }
    private Button configure_F1;
    private EditText text_F1;
//    private Button configure_F2;
//    private EditText text_F2;
    SharedPreferences sp;
    public static final String DEFAULT = "N/A";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragConfigureView = inflater.inflate(R.layout.fragment_configure, container, false);
        TextView textview = fragConfigureView.findViewById(R.id.f1var);
        configure_F1 = fragConfigureView.findViewById(R.id.f1Configure);
        text_F1 = fragConfigureView.findViewById(R.id.f1Value);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String f1_str = sp.getString("user_preferences", DEFAULT);
        textview.setText(f1_str);

        configure_F1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String user_input = text_F1.getText().toString();
//                SharedPreferences sp = getActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
                sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor edit_sp = sp.edit();
                edit_sp.putString("user_preferences", user_input);
                edit_sp.commit();
                Log.d("function1", user_input);
                load();
            }
        });
        return fragConfigureView;
    }

    public void load() {
//        SharedPreferences sp = getActivity().getSharedPreferences("user_preferences.xml", Context.MODE_PRIVATE);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        TextView textview = getActivity().findViewById(R.id.f1var);

        String f1_str = sp.getString("user_preferences", DEFAULT);
        Log.d("function1", f1_str);
        if (f1_str.equals(DEFAULT)) {
            Toast.makeText(getActivity(), "No Function Data Found", Toast.LENGTH_SHORT).show();
            textview.setText(f1_str);
        } else {
            Toast.makeText(getActivity(), "Function Loaded", Toast.LENGTH_SHORT).show();
            Log.d("function1", f1_str);
            textview.setText(f1_str);
        }
    }
}

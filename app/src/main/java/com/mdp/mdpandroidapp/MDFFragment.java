package com.mdp.mdpandroidapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import java.math.BigInteger;

public class MDFFragment extends Fragment {

    public MDFFragment() {

    }

    private Arena mArena;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GridLayout arenaLayout = (GridLayout)getActivity().findViewById(R.id.arena);
        mArena = new Arena(getActivity(),arenaLayout);
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

            showArena();
            showPoints();
            showArrows();
        }

        private void showPoints() {
            int wp_id = sp_waypoint.getInt("sp_waypoint_id", 1);
            int sp_id = sp_startpoint.getInt("sp_startpoint_id", 1);
            setColor(wp_id, "#FF0000", "single", "");
            setColor(sp_id, "#00FF00", "bordered", "#CCFFCC");
        }

        private void showArena() {
            int descriptor2Ptr = 0;

            String mapDescriptor1 = sp_MDF1.getString("sp_mdf1","N/A");
            String mapDescriptor2 = sp_MDF2.getString("sp_mdf2","N/A");

            BigInteger bi1 = new BigInteger(mapDescriptor1, 16);
            String mapDescriptor1Bin = bi1.toString(2);
            String mapDescriptor2Bin = "";

            String MDS2length = "%" + mapDescriptor2.length()*4 + "s";
            BigInteger bi2 = new BigInteger(mapDescriptor2, 16);
            mapDescriptor2Bin = bi2.toString(2);
            mapDescriptor2Bin = String.format(MDS2length, mapDescriptor2Bin).replace(" ", "0");

            for (int i=2;i<302;i++){
                int id = posToId(i);
                if(mapDescriptor1Bin.charAt(i)=='0'){
                    this.setColor(id,"#CCCCCC","single","");
                }
                else if(mapDescriptor1Bin.charAt(i)=='1'){
                    if(mapDescriptor2Bin.charAt(descriptor2Ptr)=='0'){
                        this.setColor(id,"#FFEBCD","single","");
                    }
                    else if (mapDescriptor2Bin.charAt(descriptor2Ptr)=='1'){
                        this.setColor(id,"#000000","single","");
                    }
                    descriptor2Ptr++;
                }
            }
        }

        private int posToId(int i) {
            i-=2;
            return (19-i/15)*16+(i%15)+1;
        }

        private void createArena() {
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#CCCCCC"));
            background.setStroke(1,Color.parseColor("#000000"));
            for (int row=0;row<21; row++){
                for(int col=0; col<16; col++){
                    grid = new TextView(mContext);
                    grid.setId(mCount);
                    grid.setWidth(40);
                    grid.setHeight(40);
                    grid.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

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

                    mmArena.addView(grid);
                    mCount++;
                }
            }
        }

        // "Arrows found: [x,y,d] "

        public void showArrows() {
            String image_str = sp_image_str.getString("sp_arrow", DEFAULT);
            String[] arrow_split_space = image_str.split(" ");

            if(arrow_split_space.length<2) return;

            for(int i = 2; i < arrow_split_space.length; i++){
                //[x,y,d]
                String[] ar_unparsed = arrow_split_space[i].split(",");
                //[x //y //d]
                Integer ar_coord = corToId(Integer.parseInt(ar_unparsed[0].substring(1)), Integer.parseInt(ar_unparsed[1]));

                GradientDrawable background = new GradientDrawable();
                background.setStroke(1, Color.parseColor("#000000"));
                Drawable arrow_u = ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_u, null);
                mmArena.getChildAt(ar_coord).setBackground(arrow_u);
            }
        }

        private Integer corToId(int po_x, int po_y) {
            Integer cor=(po_x + 1) + (19 - po_y) * 16;
            return cor;
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
            }
        }
        private int getRow(Integer mCount) {
            return 20-mCount/16;
        }
        private int getCol(Integer mCount) {
            return mCount%16;
        }
    }

    private Integer getCol(int id) {
        return id%16-1;
    }
    private Integer getRow(int id) {
        return 20-id/16-1;
    }
}

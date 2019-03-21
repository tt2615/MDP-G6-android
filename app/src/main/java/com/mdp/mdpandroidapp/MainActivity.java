package com.mdp.mdpandroidapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mdp.mdpandroidapp.BluetoothConnectionService;
import com.mdp.mdpandroidapp.ConfigureFragment;
import com.mdp.mdpandroidapp.ConnectFragment;
import com.mdp.mdpandroidapp.ControlFragment;
import com.mdp.mdpandroidapp.ExploreFragment;
import com.mdp.mdpandroidapp.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BluetoothConnectionService mBluetoothConnectionService = BluetoothConnectionService.getInstance();
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private SharedPreferences wp_sp;
    private SharedPreferences sp_sp;
    private SharedPreferences mdf1_sp;
    private SharedPreferences mdf2_sp;
    private SharedPreferences image_sp;

    static final int WP_SP = 0;
    static final int SP_SP = 1;
    static final int MDF1 = 2;
    static final int MDF2 = 3;
    static final int ARROW = 4;


    public SharedPreferences getSharedPreference (int sharedPreferenceIndex){
        SharedPreferences tmp = null;
        switch (sharedPreferenceIndex){
            case WP_SP:
                wp_sp = getSharedPreferences("sp_waypoint_id", Context.MODE_PRIVATE);
                tmp = wp_sp;
                break;
            case SP_SP:
                sp_sp = getSharedPreferences("sp_startpoint_id", Context.MODE_PRIVATE);
                tmp = sp_sp;
                break;
            case MDF1:
                mdf1_sp = getSharedPreferences("sp_mdf1", Context.MODE_PRIVATE);
                tmp = mdf1_sp;
                break;
            case MDF2:
                mdf2_sp = getSharedPreferences("sp_mdf2", Context.MODE_PRIVATE);
                tmp = mdf2_sp;
                break;
            case ARROW:
                image_sp = getSharedPreferences("sp_arrow", Context.MODE_PRIVATE);
                tmp = image_sp;
                break;
        }
        return tmp;
    }

    public  BluetoothConnectionService getBluetoothConnectionService() {
        return mBluetoothConnectionService;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        return mBluetoothAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = new Fragment();

        if (id == R.id.nav_connect) {
            setTitle("Connect");
            fragment = new ConnectFragment();
        } else if (id == R.id.nav_control) {
            setTitle("Control");
            fragment = new ControlFragment();
        } else if (id == R.id.nav_explore) {
            setTitle("Explore");
            fragment = new ExploreFragment();
        } else if (id == R.id.nav_configure) {
            setTitle("Configure");
            fragment = new ConfigureFragment();
        } else if (id == R.id.nav_mdf) {
            setTitle("MDP AY1819S2 GROUP 6");
            fragment = new MDFFragment();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_main,fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

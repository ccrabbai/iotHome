package com.example.android.navgation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //Classes used in the Firebase Database API
    // 1: Reference to chatly database

    //2: Reference to the specific part of the database

    //3.To read messages from the database, you add a ChildEventListener
    private ValueEventListener mValueEventListener;

    //4.To create an instance of the authentiaction class
    private FirebaseAuth mFireBaseAuth;

    public static final int RC_SIGN_IN = 1;
    public static final String ANONYMOUS = "anonymous";
    public String mUsername;

    //5.Creating the authentifcation state listener
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDeviceDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get instance of the database
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // Get reference to specific part of the database
        mDeviceDatabaseReference = mFirebaseDatabase.getReference().child("homeDevices");

        //Get instance of firebaseAuth
        mFireBaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Setting up the auth listener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            //checking if the user is signed in or not
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is not signed in
                    Toast.makeText(MainActivity.this, "You are now signed in", Toast.LENGTH_SHORT).show();
                    }else {
                    OnSignedOutCleanUp();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),

                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedFragment(id);
        return true;
    }

    private void displaySelectedFragment(int itemId){
        //creating fragment object
        Fragment fragment = null;
        Log.i("Fragment", "Fragment Entered");
        //initializing the fragment object which is selected
        switch(itemId){
            case R.id.light:
                Log.i("LightFragment", "Light Fragment Entered");
                fragment = new LightFragment();
                break;
            case R.id.ventilation:
                Log.i("VentilationFragment", "Ventilation Fragment Entered");
                fragment = new VentilationFragment();
                break;
            case R.id.temperature:
                Log.i("TemperatureFragment", "Temperature Fragment Entered");
                fragment = new TemperatureFragment();
                break;
            case R.id.tell_home_status:
                Log.i("HomeFragment", "Home Fragment Entered");
                fragment = new HomeCondition();
                break;
        }
        //replacing the fragment
        if(fragment!=null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame,fragment);
            ft.commit();
        }
        DrawerLayout drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in shown from the method onActivityResult!", Toast.LENGTH_SHORT).show();
                // finish();
                //sign in waas cancelled by the user, finish up the activity
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in unsuccessful\nNO INTERNET ACCESS\nConnect device to the internet".toUpperCase(), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        mFireBaseAuth.addAuthStateListener(mAuthStateListener);
        //attachDataBaseReadListener();
        Fragment frag = new HomeCondition();
        FragmentTransaction ftt = getSupportFragmentManager().beginTransaction();
        ftt.replace(R.id.content_frame,frag);
        ftt.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //detachDataBaseReadListener();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (mAuthStateListener != null){
            mFireBaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void OnSignedInInitialize(String username) {
        mUsername = username;
        //attachDataBaseReadListener();
    }

    private void OnSignedOutCleanUp() {
        mUsername = ANONYMOUS;
//        mMessageAdapter.clear();
        //detachDataBaseReadListener();
    }
}
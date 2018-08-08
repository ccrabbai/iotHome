package com.example.android.navgation;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class HomeCondition extends Fragment {

    //Classes used in the Firebase Database API
    // 1: Reference to chatly database
    private FirebaseDatabase mFirebaseDatabase;

    //2: Reference to the specific part of the database
    private DatabaseReference mDeviceDatabaseReference;

    // 3: A listener that reads the devices state from the db
    private ValueEventListener mValueEventListener;

    //General temperature data
    private TextView genTempTV;
    private ProgressBar genTempPB;

    private RelativeLayout homeStatus;
    private FloatingActionButton fab;

    //Gerneral light data
    private RadioButton gen_light_radio_btn_sleep_mode;
    private RadioButton gen_light_radio_btn_on_mode;
    private RadioButton gen_light_radio_btn_off_mode;
    private ImageView gen_light_on_image;
    private ImageView gen_light_off_image;

    //Gerneral fan data
    private RadioButton gen_fan_radio_btn_sleep_mode;
    private RadioButton gen_fan_radio_btn_on_mode;
    private RadioButton gen_fan_radio_btn_off_mode;
    private ImageView gen_fan_on_image;
    private ImageView gen_fan_off_image;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Current Device State");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // Get reference to specific part of the database
        mDeviceDatabaseReference = mFirebaseDatabase.getReference().child("homeDevices");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.content_home_condition, container, false);

        fab = view.findViewById(R.id.fab);

        //General device display
        genTempTV = view.findViewById(R.id.gen_temp_tv);
        genTempPB = view.findViewById(R.id.gen_temp_pb);

        homeStatus = (RelativeLayout) view.findViewById(R.id.tell_home_status);
        homeStatus.setVisibility(View.VISIBLE);

        //General light data
        gen_light_radio_btn_sleep_mode = (RadioButton) view.findViewById(R.id.gen_radio_btn_light_sleep);
        gen_light_radio_btn_on_mode = (RadioButton) view.findViewById(R.id.gen_radio_btn_light_on);
        gen_light_radio_btn_off_mode = (RadioButton) view.findViewById(R.id.gen_radio_btn_light_off);
        gen_light_on_image = (ImageView) view.findViewById(R.id.gen_light_image_on);
        gen_light_off_image = (ImageView) view.findViewById(R.id.gen_light_image_off);

        //General fan data
        gen_fan_radio_btn_sleep_mode = (RadioButton) view.findViewById(R.id.gen_radio_btn_fan_sleep);
        gen_fan_radio_btn_on_mode = (RadioButton) view.findViewById(R.id.gen_radio_btn_fan_on);
        gen_fan_radio_btn_off_mode = (RadioButton) view.findViewById(R.id.gen_radio_btn_fan_off);
        gen_fan_on_image = (ImageView) view.findViewById(R.id.gen_fan_image_on);
        gen_fan_off_image = (ImageView) view.findViewById(R.id.gen_fan_image_off);

        return view;
    }

    public void attachDataBaseReadListener(){
        if (mValueEventListener == null) {
            //The anonymous method for childEventLis tener
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    float genCurrentTempValue;
                    genCurrentTempValue = dataSnapshot.getValue(DeviceState.class).getTemp();
                    int genCurrentLightValue = dataSnapshot.getValue(DeviceState.class).getLightState();
                    int genCurrentFanValue = dataSnapshot.getValue(DeviceState.class).getFanState();
                    long oldTime = dataSnapshot.getValue(DeviceState.class).getOldTime();

                    long currentTime = System.currentTimeMillis()/1000;
                    Log.i("CURRENT TIME",""+currentTime);
                    Log.i("OLD TIME",""+oldTime);
                    Log.i("TIME DIFFERENCE",""+(currentTime - oldTime));
                   // Log.i("CURRENT TIME2", new Timestamp() );

                    if (currentTime - oldTime < 5){
                        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.success)));
                        fab.setImageResource(R.drawable.connected);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar.make(view, "Hardware connected to the internet".toUpperCase(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        });
                    }else{
                        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.failed)));
                        fab.setImageResource(R.drawable.not_connected);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar.make(view, "Hardware not connected to the internet\n\tPlease wait .. .. .. .. ..".toUpperCase(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        });
                    }

                    //Temperature
                    genTempPB.setProgress((int) genCurrentTempValue);
                    genTempPB.setBackgroundColor(getResources().getColor(R.color.loaderColorDefault));
                    if (genCurrentTempValue < 20){
                        genTempTV.setText(genCurrentTempValue + " Degrees: Cold".toUpperCase());
                    }else if(genCurrentTempValue > 20 && genCurrentTempValue < 40){
                        genTempTV.setText(genCurrentTempValue + " Degrees: Normal".toUpperCase());
                    }else if (genCurrentTempValue > 40 && genCurrentTempValue < 70){
                        genTempTV.setText(genCurrentTempValue + " Degrees: Meduim".toUpperCase());
                    }else{
                        genTempTV.setText(genCurrentTempValue + " Degrees: High".toUpperCase());
                        genTempPB.setBackgroundColor(getResources().getColor(R.color.red));
                    }

                    //Light
                    switch (genCurrentLightValue){
                        case 1:
                            gen_light_on_image.setVisibility(View.VISIBLE);
                            gen_light_off_image.setVisibility(View.INVISIBLE);
                            gen_light_radio_btn_on_mode.setChecked(true);
                            gen_light_radio_btn_off_mode.setChecked(false);
                            gen_light_radio_btn_sleep_mode.setChecked(false);
                            break;
                        case 0:
                            gen_light_on_image.setVisibility(View.INVISIBLE);
                            gen_light_off_image.setVisibility(View.VISIBLE);
                            gen_light_radio_btn_off_mode.setChecked(true);
                            gen_light_radio_btn_on_mode.setChecked(false);
                            gen_light_radio_btn_sleep_mode.setChecked(false);
                            break;
                        case 2:
                            gen_light_on_image.setVisibility(View.VISIBLE);
                            gen_light_off_image.setVisibility(View.INVISIBLE);
                            gen_light_radio_btn_sleep_mode.setChecked(true);
                            gen_light_radio_btn_on_mode.setChecked(false);
                            gen_light_radio_btn_off_mode.setChecked(false);
                            break;
                        default:
                    }

                    //Fan
                    switch (genCurrentFanValue){
                        case 1:
                            gen_fan_on_image.setVisibility(View.VISIBLE);
                            gen_fan_off_image.setVisibility(View.INVISIBLE);
                            gen_fan_radio_btn_on_mode.setChecked(true);
                            gen_fan_radio_btn_off_mode.setChecked(false);
                            gen_fan_radio_btn_sleep_mode.setChecked(false);
                            break;
                        case 0:
                            gen_fan_on_image.setVisibility(View.INVISIBLE);
                            gen_fan_off_image.setVisibility(View.VISIBLE);
                            gen_fan_radio_btn_off_mode.setChecked(true);
                            gen_fan_radio_btn_on_mode.setChecked(false);
                            gen_fan_radio_btn_sleep_mode.setChecked(false);
                            break;
                        case 2:
                            gen_fan_on_image.setVisibility(View.VISIBLE);
                            gen_fan_off_image.setVisibility(View.INVISIBLE);
                            gen_fan_radio_btn_sleep_mode.setChecked(true);
                            gen_fan_radio_btn_on_mode.setChecked(false);
                            gen_fan_radio_btn_off_mode.setChecked(false);
                            break;
                        default:
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            //Add the childEventListener to the database reference
            mDeviceDatabaseReference.addValueEventListener(mValueEventListener);
            homeStatus.setVisibility(View.INVISIBLE);
        }
    }

    private void detachDataBaseReadListener(){
        if (mValueEventListener != null) {
            mDeviceDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        homeStatus.setVisibility(View.VISIBLE);
        attachDataBaseReadListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        detachDataBaseReadListener();
    }
}
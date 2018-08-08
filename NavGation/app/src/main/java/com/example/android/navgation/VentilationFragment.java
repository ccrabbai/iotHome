package com.example.android.navgation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class VentilationFragment extends Fragment {

    // Various states of the fan
    private static final int mFanOn = 1;
    private static final int mFanOff = 0;
    private static final int mFanSleep = 2;
    private int fanCurrentState;

    //Classes used in the Firebase Database API
    // 1: Reference to chatly database
    private FirebaseDatabase mFirebaseDatabase;

    //2: Reference to the specific part of the database
    private DatabaseReference mDeviceDatabaseReference;

    // 3: A listener that reads the devices state from the db
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener;

//    //4.To create an instance of the authentiaction class
//     private FirebaseAuth mFirebaseAuth;
//
//    //5.Creating the authentifcation state listener
//    private FirebaseAuth.AuthStateListener mAuthStateListener;
//

    private RadioButton fan_radio_btn_on_mode;
    private RadioButton fan_radio_btn_off_mode;
    private RadioButton fan_radio_btn_sleep_mode;
    private Button fan_on;
    private Button fan_off;
    private ProgressBar fan_progress_bar;
    private RelativeLayout tell_fan_status;
    private ImageView on_image;
    private ImageView off_image;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Ventilation Control");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // Get reference to specific part of the database
        mDeviceDatabaseReference = mFirebaseDatabase.getReference().child("homeDevices");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ventilation, container, false);

        fan_on = (Button) view.findViewById(R.id.fan_btn_on);
        fan_off = (Button) view.findViewById(R.id.fan_btn_off);
        fan_radio_btn_sleep_mode = (RadioButton) view.findViewById(R.id.radio_btn_fan_sleep);
        fan_radio_btn_on_mode = (RadioButton) view.findViewById(R.id.radio_btn_fan_on);
        fan_radio_btn_off_mode = (RadioButton) view.findViewById(R.id.radio_btn_fan_off);
        fan_progress_bar = (ProgressBar) view.findViewById(R.id.fan_progress_bar);
        tell_fan_status = (RelativeLayout) view.findViewById(R.id.tell_fan_status);
        on_image = (ImageView) view.findViewById(R.id.fan_image_on);
        off_image = (ImageView) view.findViewById(R.id.fan_image_off);

        int checkFanCurrentState;

        fan_on.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Update the state of the fan to ON(1)
                if (fanCurrentState == 1){
                    Toast.makeText(getContext(), "Fan Already ON", Toast.LENGTH_SHORT).show();
                }else{
                    mDeviceDatabaseReference.child("fanState").setValue(mFanOn);
                    tell_fan_status.setVisibility(View.VISIBLE);
                    //Toast.makeText(getContext(), "on clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fan_off.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Update the state of the fan to OFF(0)
                if (fanCurrentState == 0){
                    Toast.makeText(getContext(), "Fan Already OFF", Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(getContext(), "off clicked", Toast.LENGTH_SHORT).show();
                    mDeviceDatabaseReference.child("fanState").setValue(mFanOff);
                    tell_fan_status.setVisibility(View.VISIBLE);
                }
            }
        });

        fan_radio_btn_sleep_mode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Update the state of the fan to SLEEP MODE(2)
                if (fanCurrentState == 2){
                    Toast.makeText(getContext(), "Fan Already in SLEEP MODE", Toast.LENGTH_SHORT).show();
                }else{
                    mDeviceDatabaseReference.child("fanState").setValue(mFanSleep);
                    tell_fan_status.setVisibility(View.VISIBLE);
                    //Toast.makeText(getContext(), "sleep clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private void attachDataBaseReadListener(){
        if (mValueEventListener == null) {
            //The anonymous method for childEventListener
//            mChildEventListener = new ChildEventListener() {
//                @Override
//                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    Toast.makeText(getContext(), "onChildAdded", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                    DeviceState deviceState = dataSnapshot.getValue(DeviceState.class);
//                    int fanState = 1;
//                    if (deviceState != null) {
//                        fanState = 0;
//                    }
//                    switch (fanState){
//                        case 1:
//                            tell_fan_status.setVisibility(View.INVISIBLE);
//                            on_image.setVisibility(View.VISIBLE);
//                            off_image.setVisibility(View.INVISIBLE);
//                            fan_progress_bar.setProgress(100);
//                            fan_radio_btn_on_mode.setChecked(true);
//                            Toast.makeText(getContext(), "Fan On", Toast.LENGTH_SHORT).show();
//                            break;
//                        case 0:
//                            tell_fan_status.setVisibility(View.INVISIBLE);
//                            on_image.setVisibility(View.INVISIBLE);
//                            off_image.setVisibility(View.VISIBLE);
//                            fan_progress_bar.setProgress(0);
//                            fan_radio_btn_off_mode.setChecked(true);
//                            Toast.makeText(getContext(), "Fan Switched Off", Toast.LENGTH_SHORT).show();
//                            break;
//                        case 2:
//                            tell_fan_status.setVisibility(View.INVISIBLE);
//                            on_image.setVisibility(View.VISIBLE);
//                            off_image.setVisibility(View.INVISIBLE);
//                            fan_progress_bar.setProgress(30);
//                            fan_radio_btn_sleep_mode.setChecked(true);
//                            Toast.makeText(getContext(), "Sleep mode activated.", Toast.LENGTH_SHORT).show();
//                            break;
//                        default:
//                            Toast.makeText(getContext(), "No available mode.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onChildRemoved(DataSnapshot dataSnapshot) {}
//
//                @Override
//                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {}
//            };

            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    fanCurrentState = dataSnapshot.getValue(DeviceState.class).getFanState();
                    switch (fanCurrentState){
                        case 1:
                            tell_fan_status.setVisibility(View.INVISIBLE);
                            on_image.setVisibility(View.VISIBLE);
                            off_image.setVisibility(View.INVISIBLE);
                            fan_progress_bar.setProgress(100);
                            fan_radio_btn_on_mode.setChecked(true);
                            Toast.makeText(getContext(), "Fan Switched ON", Toast.LENGTH_SHORT).show();
                            break;
                        case 0:
                            tell_fan_status.setVisibility(View.INVISIBLE);
                            on_image.setVisibility(View.INVISIBLE);
                            off_image.setVisibility(View.VISIBLE);
                            fan_progress_bar.setProgress(0);
                            fan_radio_btn_off_mode.setChecked(true);
                            Toast.makeText(getContext(), "Fan Switched OFF", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            tell_fan_status.setVisibility(View.INVISIBLE);
                            on_image.setVisibility(View.VISIBLE);
                            off_image.setVisibility(View.INVISIBLE);
                            fan_progress_bar.setProgress(30);
                            fan_radio_btn_sleep_mode.setChecked(true);
                            Toast.makeText(getContext(), "SLEEP MODE Activated.", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getContext(), "No available mode.", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            //Add the childEventListener to the database reference
            mDeviceDatabaseReference.addValueEventListener(mValueEventListener);
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
        attachDataBaseReadListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        detachDataBaseReadListener();
    }
}
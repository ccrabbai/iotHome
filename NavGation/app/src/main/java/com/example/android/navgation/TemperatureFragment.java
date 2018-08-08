//package com.example.android.navgation;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//
//public class LightFragment extends Fragment {
//
//    public LightFragment() {}
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getActivity().setTitle("Light Control");
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_light, container, false);
//    }
//}

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
import android.widget.TextView;
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

public class TemperatureFragment extends Fragment {

    public float currentTempValue;

    //Classes used in the Firebase Database API
    // 1: Reference to chatly database
    private FirebaseDatabase mFirebaseDatabase;

    //2: Reference to the specific part of the database
    private DatabaseReference mDeviceDatabaseReference;

    // 3: A listener that reads the devices state from the db
    private ValueEventListener mValueEventListener;

    private TextView tempValueTV;
    private ProgressBar tempProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Temperature Control Unit");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // Get reference to specific part of the database
        mDeviceDatabaseReference = mFirebaseDatabase.getReference().child("homeDevices");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_temperature, container, false);

        tempValueTV = (TextView) view.findViewById(R.id.tvTemp);
        tempProgressBar = (ProgressBar) view.findViewById(R.id.temp_progress_bar);
        return view;
    }

    public void attachDataBaseReadListener(){
        if (mValueEventListener == null) {
            //The anonymous method for childEventListener
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    currentTempValue = dataSnapshot.getValue(DeviceState.class).getTemp();
                    tempProgressBar.setProgress((int) currentTempValue);
                    if (currentTempValue < 20){
                        tempValueTV.setText(currentTempValue + " Degrees: Cold".toUpperCase());
                    }else if(currentTempValue > 20 && currentTempValue < 40){
                        tempValueTV.setText(currentTempValue + " Degrees: Normal".toUpperCase());
                    }else if (currentTempValue > 40 && currentTempValue < 70){
                        tempValueTV.setText(currentTempValue + " Degrees: Meduim".toUpperCase());
                    }else{
                        tempValueTV.setText(currentTempValue + " Degrees: High".toUpperCase());
                        tempProgressBar.setBackgroundColor(getResources().getColor(R.color.red));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
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
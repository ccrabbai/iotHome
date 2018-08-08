package com.example.android.navgation;

/**
 * Created by onyekachukwu on 3/25/2018.
 */

public class DeviceState {

    private int fanState;
    private int lightState;
    private  float temp;
    private long oldTime;

    public DeviceState() {
    }

    public DeviceState(int fanState,int lightState, float temp, long oldTime) {
        this.fanState = fanState;
        this.lightState = lightState;
        this.temp = temp;
        this.oldTime = oldTime;
    }



    public int getFanState() {
        return fanState;
    }

    public int getLightState() {
        return lightState;
    }

    public float getTemp() {
        return temp;
    }

    public long getOldTime() {
        return oldTime;
    }

    public void setFanState(int fanState) {
        this.fanState = fanState;
    }
    public void setLightState(int lightState) {
        this.lightState = lightState;
    }
    public void setOldTime(long oldTime) {
        this.oldTime = oldTime;
    }
}

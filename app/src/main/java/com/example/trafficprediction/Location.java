package com.example.trafficprediction;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sohailyarkhan on 26/03/16.
 */
public class Location {
    public String src;
    public String des;
    public String time;
    public Integer isDelay;
    public LatLng srcLatLng;
    public LatLng desLatLng;

    public Location(String src, String des, String time, Integer isDelay, LatLng srcLatLng, LatLng desLatLng) {
        super();
        this.src = src;
        this.des = des;
        this.time = time;
        this.srcLatLng = srcLatLng;
        this.desLatLng = desLatLng;
        this.isDelay = isDelay;
    }

    public String getSrc(){
        return src;
    }

    public String getDes(){
        return des;
    }

    public String getTime(){
        return time;
    }

    public Integer getDelay(){
        return isDelay;
    }

    public void setSrc(String src){
        this.src = src;
    }

    public void setDes(String des){
        this.des = des;
    }

    public void setTime(String time){
        this.time = time;
    }

    public void setDelay(Integer isDelay){
        this.isDelay = isDelay;
    }
}

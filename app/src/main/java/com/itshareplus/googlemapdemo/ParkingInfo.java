package com.itshareplus.googlemapdemo;

/**
 * Created by 貴花 on 2017/9/9.
 */

public class ParkingInfo {

    private String lat;
    private String lon;
    private String empty;

    public ParkingInfo(){}

    public ParkingInfo(String lat, String lon, String empty) {
        this.lat = lat;
        this.lon = lon;
        this.empty = empty;
    }

    public String getLat() {

        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getEmpty(){

        return empty;
    }

}

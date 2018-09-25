package com.example.dzudge;

import org.mapsforge.core.model.LatLong;

import java.util.Date;

    class TrackPoint {
        LatLong _latLong;
        double _ele;
        Date _time;
        TrackPoint(LatLong latLong,double ele,Date time){
            _latLong=latLong;
            _ele=ele;
            _time=time;
        }
        @Override
        public String toString(){
            return "緯度:"+_latLong.latitude+"\n經度:"+_latLong.longitude+"\n"+
                    "標高:"+_ele+"\n時刻:"+_time;
        }

    }

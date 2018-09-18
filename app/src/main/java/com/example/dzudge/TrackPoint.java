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
            return _latLong.toString()+",ele:"+_ele+",time:"+_time;
        }

    }

package com.example.dzudge;

import org.mapsforge.core.model.LatLong;

import java.util.Date;

class WayPoint extends TrackPoint {
    String _name;
    String _desc;
    String _sym;
    WayPoint(LatLong latLong, double ele, Date time, String name, String desc, String sym){
        super(latLong, ele, time);
        _name=name;
        _desc=desc;
        _sym=sym;
    }
    @Override
    public String toString(){
        return super.toString()+",name:"+_name+",desc:"+_desc+",sym:"+_sym;
    }
}

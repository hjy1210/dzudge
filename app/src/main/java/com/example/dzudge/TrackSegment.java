package com.example.dzudge;

import java.util.ArrayList;

class TrackSegment{
    ArrayList<TrackPoint> _trackPoints;
    TrackSegment(ArrayList<TrackPoint> trackPoints){
        _trackPoints=trackPoints;
    }
    @Override
    public String toString(){
        if (_trackPoints==null) {
            return "";
        } else {
            return ""+_trackPoints.size();
        }
    }

}

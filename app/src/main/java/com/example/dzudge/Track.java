package com.example.dzudge;

import java.util.ArrayList;

class Track{
    String _name;
    String _desc;
    ArrayList<TrackSegment> _trksegs;
    Track(String name, String desc, ArrayList<TrackSegment> trksegs){
        _name=name;
        _desc=desc;
        _trksegs =trksegs;
    }
    @Override
    public String toString(){
        if (_trksegs !=null) return ":"+ _trksegs.size()+":"+ _trksegs.get(0);
        else return "";
    }
}

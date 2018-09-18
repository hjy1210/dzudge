package com.example.dzudge;

import java.util.ArrayList;

class Track{
    String _name;
    String _desc;
    ArrayList<TrackSegment> _trks;
    Track(String name, String desc, ArrayList<TrackSegment> trks){
        _name=name;
        _desc=desc;
        _trks=trks;
    }
}

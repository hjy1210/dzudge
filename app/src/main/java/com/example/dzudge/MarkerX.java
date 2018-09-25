package com.example.dzudge;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.layer.overlay.Marker;

public class MarkerX extends Marker {
    String _msg;
    MarkerX(LatLong latLong, Bitmap bitmap, int x, int y, String msg){
        super(latLong,bitmap,x,y);
        _msg=msg;
    }
}

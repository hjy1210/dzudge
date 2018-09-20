package com.example.dzudge;

import android.util.Log;

import org.mapsforge.core.model.LatLong;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class Gpx {
    ArrayList<WayPoint> _wpts;
    ArrayList<Track> _trks;
    Gpx(ArrayList<WayPoint> wpts,ArrayList<Track> trks){
        _wpts=wpts;
        _trks=trks;
    }
    static TimeZone utc = TimeZone.getTimeZone("GMT");
    static SimpleDateFormat  m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static String test(){
        Date date;
        try {
            m_ISO8601Local.setTimeZone(utc);
            date=m_ISO8601Local.parse("2018-08-01T09:19:25Z"); //"2018-08-01T09:19:25Z"
            Log.i("Date",""+date.getTime());
            return date.toString();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }
    @Override
    public String toString(){
        return "trkssize:"+_trks.size()+_trks.get(0)+", wptssize:"+_wpts.size();
    }
}

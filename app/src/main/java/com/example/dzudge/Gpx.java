package com.example.dzudge;

import android.util.Log;

import org.mapsforge.core.model.LatLong;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Gpx {
    class TrackPoint {
        LatLong _latLong;
        double _ele;
        Calendar _time;
    }
    class WayPoint extends TrackPoint {
        String _name;
        String _cmt;
        String _desc;
        String _sym;
    }
    class TrackSegment{
        List<TrackPoint> _trackSegment;
    }

    class Track{
        String _name;
        List<TrackSegment> _trks;
    }

    List<WayPoint> _wpts;
    List<Track> _trks;
    static SimpleDateFormat  m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static TimeZone utc = TimeZone.getTimeZone("UTC");
    static DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
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
}

package com.example.dzudge;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import org.mapsforge.core.model.LatLong;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.DateFormat;
//import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
//import java.util.Locale;
import java.util.Locale;
import java.util.TimeZone;


class GpxUtils {
    private static TimeZone utc = TimeZone.getTimeZone("GMT");
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.CHINESE);
    static{
        dateFormatter.setTimeZone(utc);
    }
    private static LatLong getLatLong(Element waypointEle){
        return new LatLong(Double.parseDouble(waypointEle.getAttribute("lat")),Double.parseDouble(waypointEle.getAttribute("lon")));
    }
    private static double getElevation(Element waypointEle){
        NodeList nList=waypointEle.getElementsByTagName("ele");
        if (nList.getLength()==0) return 0;  ///// some <wpt> node has no <ele> child
        return Double.parseDouble(nList.item(0).getTextContent());
    }
    private static Date getTime(Element waypointEle){
        NodeList nList=waypointEle.getElementsByTagName("time");
        if (nList.getLength()==0) return null;  ///// some <wpt> element has not time child
        String time=nList.item(0).getTextContent();
        Date date;
        try{
            date=dateFormatter.parse(time);
            return date;
        } catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }
    private static String getText(Element waypointEle, String tagName){
        NodeList nList=waypointEle.getElementsByTagName(tagName);
        if (nList.getLength()>0)
            return nList.item(0).getTextContent();
        else
            return null;
        }
    static ArrayList<WayPoint> getWayPoints(Document doc){
        //dateFormatter.setTimeZone(utc);
        NodeList wpts=doc.getElementsByTagName("wpt");
        ArrayList<WayPoint> wayPoints=new ArrayList<>();
        for (int i=0;i<wpts.getLength();i++){
            LatLong latLong=getLatLong((Element)wpts.item(i));
            double ele=getElevation((Element)wpts.item(i));
            Date time=getTime((Element)wpts.item(i));
            String name=getText((Element)wpts.item(i),"name");
            String desc=getText((Element)wpts.item(i),"desc");
            String sym=getText((Element)wpts.item(i),"sym");
            wayPoints.add(new WayPoint(latLong,ele,time,name, desc, sym));
        }
        return wayPoints;
    }
    static ArrayList<Track> getTracks(Document doc){
        //dateFormatter.setTimeZone(utc);
        NodeList trks=doc.getElementsByTagName("trk");
        ArrayList<Track> tracks=new ArrayList<>();
        for (int i=0;i<trks.getLength();i++){
            String name=getText((Element)trks.item(i),"name");
            String desc=getText((Element)trks.item(i),"desc");
            //tracks.add(new Track(name,null,null));
            NodeList nTrkSegments=((Element) trks.item(i)).getElementsByTagName("trkseg");
            ArrayList<TrackSegment> trackSegments=new ArrayList<>();
            for (int j=0;j<nTrkSegments.getLength();j++){
                NodeList nTrackPoints=((Element)nTrkSegments.item(j)).getElementsByTagName("trkpt");
                ArrayList<TrackPoint> trackPoints=new ArrayList<>();
                for (int k=0;k<nTrackPoints.getLength();k++){
                    LatLong latLong=getLatLong((Element)nTrackPoints.item(k));
                    double ele=getElevation((Element)nTrackPoints.item(k));
                    Date time=getTime((Element)nTrackPoints.item(k));
                    TrackPoint trackPoint=new TrackPoint(latLong,ele,time);
                    trackPoints.add(trackPoint);
                }
                trackSegments.add(new TrackSegment(trackPoints));
            }
            tracks.add(new Track(name,desc,trackSegments));
        }
        return tracks;
    }

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";
    //private static TimeZone utc = TimeZone.getTimeZone("GMT");
    //private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.CHINESE);
    //static{
    //    dateFormatter.setTimeZone(utc);
    //}


    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "" + location.getLatitude() + ", " + location.getLongitude() + ","+location.getAltitude()+","+dateFormatter.format(new Date(location.getTime()));
    }

    static String getLocationTitle(Context context) {
       return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }
    static ArrayList<TrackPoint> extractWpts(String str){
        ArrayList<TrackPoint>trks=new ArrayList<>();
        String[] lines=str.split("\n");
        for (String line : lines) {
            String s1 = line.trim();
            if (s1.equals("")) continue;
            String[] fields = s1.split(",");
            String time = fields[3];
            Date date;
            try {
                date = dateFormatter.parse(time);
                trks.add(new TrackPoint(new LatLong(Double.parseDouble(fields[0]),
                        Double.parseDouble(fields[1])), Double.parseDouble(fields[2]), date));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return trks;
    }
}

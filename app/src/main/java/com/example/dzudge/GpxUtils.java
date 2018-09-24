package com.example.dzudge;

import android.net.Uri;

import org.mapsforge.core.model.LatLong;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GpxUtils {
    static TimeZone utc = TimeZone.getTimeZone("GMT");
    static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
    public static ArrayList<WayPoint> getWayPoints(Document doc){
        //dateFormatter.setTimeZone(utc);
        NodeList wpts=doc.getElementsByTagName("wpt");
        ArrayList<WayPoint> wayPoints=new ArrayList<WayPoint>();
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
    public static ArrayList<Track> getTracks(Document doc){
        //dateFormatter.setTimeZone(utc);
        NodeList trks=doc.getElementsByTagName("trk");
        ArrayList<Track> tracks=new ArrayList<Track>();
        for (int i=0;i<trks.getLength();i++){
            String name=getText((Element)trks.item(i),"name");
            String desc=getText((Element)trks.item(i),"desc");
            //tracks.add(new Track(name,null,null));
            NodeList nTrkSegments=((Element) trks.item(i)).getElementsByTagName("trkseg");
            ArrayList<TrackSegment> trackSegments=new ArrayList<TrackSegment>();
            for (int j=0;j<nTrkSegments.getLength();j++){
                NodeList nTrackPoints=((Element)nTrkSegments.item(j)).getElementsByTagName("trkpt");
                ArrayList<TrackPoint> trackPoints=new ArrayList<TrackPoint>();
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
}

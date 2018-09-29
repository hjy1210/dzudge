package com.example.dzudge;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int READ_REQUEST_GPX = 42;

    private MapDataStore mapDataStore;
    private Gpx gpx, myTrack;
    private String gpxString;
    private MapView mapView;
    private String map;
    private File mapFile;
    private byte zoomLevel;
    private TileCache tileCache;
    private ArrayList<Layer> trackLayers;
    private Layer lastPos;
    private boolean isRecording;
    private LocationManager locationManager;
    private String provider;
    private boolean inspectLifeCycle;
    private TrackSegment mySegment;
    private Layer myTrackLayer;
    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        zoomLevel = 10;
         /*
         * Before you make any calls on the mapsforge library, you need to initialize the
         * AndroidGraphicFactory. Behind the scenes, this initialization process gathers a bit of
         * information on your device, such as the screen resolution, that allows mapsforge to
         * automatically adapt the rendering for the device.
         * If you forget this step, your app will crash. You can place this code, like in the
         * Samples app, in the Android Application class. This ensures it is created before any
         * specific activity. But it can also be created in the onCreate() method in your activity.
         */
        AndroidGraphicFactory.createInstance(getApplication());
        /*
         * A MapView is an Android View (or ViewGroup) that displays a mapsforge map. You can have
         * multiple MapViews in your app or even a single Activity. Have a look at the mapviewer.xml
         * on how to create a MapView using the Android XML Layout definitions. Here we create a
         * MapView on the fly and make the content view of the activity the MapView. This means
         * that no other elements make up the content of this activity.
         */
        //mapView = new MapView(this);
        mapView=(MapView)findViewById(R.id.map);
        try {
            /*
             * We then make some simple adjustments, such as showing a scale bar and zoom controls.
             */
            mapView.setClickable(true);
            mapView.getMapScaleBar().setVisible(true);
            mapView.setBuiltInZoomControls(true);
            /*
             * To avoid redrawing all the tiles all the time, we need to set up a tile cache with an
             * utility method.
             */
            tileCache = AndroidUtil.createTileCache(this, "mapcache",
                    mapView.getModel().displayModel.getTileSize(), 1f,
                    mapView.getModel().frameBufferModel.getOverdrawFactor());
            //mapDataStore = getMapDataStore(map, tileCache);
            setMapDataStore(mapFile);
            //setMapCenter(mapDataStore, (byte) 13);
        } catch (Exception e) {
            /*
             * In case of map file errors avoid crash, but developers should handle these cases!
             */
            e.printStackTrace();
        }
        if (inspectLifeCycle) Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (inspectLifeCycle) Toast.makeText(this, "onStart", Toast.LENGTH_LONG).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        if (mapDataStore!=null && isRecording)
            locationManager.requestLocationUpdates(provider, 300, 5, this);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (inspectLifeCycle) Toast.makeText(this, "onRestart", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (inspectLifeCycle) Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (inspectLifeCycle) Toast.makeText(this, "onPause", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (inspectLifeCycle) Toast.makeText(this, "onStop", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onDestroy() {
        /*
         * Whenever your activity exits, some cleanup operations have to be performed lest your app
         * runs out of memory.
         */
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        if (inspectLifeCycle) Toast.makeText(this,"onDestroy",Toast.LENGTH_LONG).show();
        if(isRecording) locationManager.removeUpdates(this);
        super.onDestroy();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_GPX && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    gpx = readGpx(uri);
                    addTrack();
                    Toast.makeText(this,"gpx:"+gpx,Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu=menu;
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView textView;
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                gpxString = Gpx.test();
                Toast.makeText(this,gpxString,Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_selectgpx:
                openGpx(null);
                return true;
            case R.id.action_cleargpx:
                clearTrack();
                return true;
            case R.id.action_selectmap:
                openMap(null);
                return true;
            case R.id.action_centermap:
                setMapCenter();
                return true;
            case R.id.action_record:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }
                isRecording=!isRecording;
                //menu.findItem(R.id.action_stop).setEnabled(true);
                //menu.findItem(R.id.action_record).setEnabled(false);
                MenuItem it=menu.findItem(R.id.action_record);
                if (isRecording) {
                    it.setTitle("停止記錄");
                    myTrack=new Gpx(new ArrayList<WayPoint>(), new ArrayList<Track>());
                    mySegment=new TrackSegment(new ArrayList<TrackPoint>());
                    ArrayList<TrackSegment> segments=new ArrayList<TrackSegment>();
                    segments.add(mySegment);
                    Track track=new Track("first track","test",segments);
                    myTrack._trks.add(track);
                    locationManager.requestLocationUpdates(provider, 300, 5, this);
                }
                else {
                    it.setTitle("記錄軌跡");
                    locationManager.removeUpdates(this);
                    Toast.makeText(this, myTrack.toString(),Toast.LENGTH_LONG).show();
                }
               return true;
            case R.id.action_testing:
                intent = new Intent(this, EditMessageActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //LocationListener has public methods:onLocationChanged, onStatusChanged, onProviderEnabled,onProviderDisabled
    @Override
    public void onLocationChanged(Location location) {
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());
        LatLong center=new LatLong(lat,lng);
        if (lastPos!=null){
            mapView.getLayerManager().getLayers().remove(lastPos);
        }
        lastPos = Utils.createMarker(this,
                R.drawable.marker_green,center);
        mapView.getLayerManager().getLayers().add(lastPos);
        mapView.setCenter(center);
        //Toast.makeText(this,center.toString(),Toast.LENGTH_LONG).show();
        TrackPoint tpt=new TrackPoint(center,location.getAltitude(), Calendar.getInstance().getTime());
        //Toast.makeText(this,tpt.toString(),Toast.LENGTH_SHORT).show();
        mySegment._trackPoints.add(tpt);
        updateMyTrackLayer();
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    // onClickLeftShade, onClickLeftData prepare to use overlay options
    public void onClickLeftShade(View view){
        //Toast.makeText(this,"You tap me",Toast.LENGTH_SHORT);
        view.setVisibility(View.INVISIBLE);
        View btn=(Button) findViewById(R.id.left_data);
        btn.setVisibility(View.VISIBLE);
    }
    public void onClickLeftData(View view){
        //Toast.makeText(this,"You tap me",Toast.LENGTH_SHORT);
        view.setVisibility(View.INVISIBLE);
        View btn=(Button) findViewById(R.id.left_shade);
        btn.setVisibility(View.VISIBLE);
    }

    public void openMap(View view){
        ///// https://github.com/hedzr/android-file-chooser
        new ChooserDialog().with(this)
                .withFilter(false, false, "map")
                .withStartFile(Environment.getExternalStorageDirectory().toString())
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        //Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        setMapDataStore(pathFile);
                    }
                })
                .build()
                .show();
    }
    private void setMapDataStore(File file) {
        if (file==null) return;
        if (isRecording) locationManager.removeUpdates(this);
        mapDataStore = new MapFile(file);
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);
        /*
         * On its own a tileRendererLayer does not know where to display the map, so we need to
         * associate it with our mapView.
         */
        mapView.setZoomLevel(zoomLevel);
        mapView.getLayerManager().getLayers().clear();
        mapView.getLayerManager().getLayers().add(tileRendererLayer);
        if (isRecording) locationManager.requestLocationUpdates(provider, 300, 5, this);

    }
    private void setMapCenter() {
        if (mapDataStore==null) return;
        double maxLat = mapDataStore.boundingBox().maxLatitude;
        double minLat = mapDataStore.boundingBox().minLatitude;
        double maxLong = mapDataStore.boundingBox().maxLongitude;
        double minLong = mapDataStore.boundingBox().minLongitude;
        LatLong center = new LatLong((maxLat + minLat) / 2, (maxLong + minLong) / 2);
        mapView.setCenter(new LatLong((maxLat + minLat) / 2, (maxLong + minLong) / 2));
        mapView.setZoomLevel(zoomLevel);
    }

    public void openGpx(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_GPX);
    }
    private Gpx readGpx(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);
            ArrayList<WayPoint> wpts = GpxUtils.getWayPoints(doc);
            ArrayList<Track> trks = GpxUtils.getTracks(doc);
            return new Gpx(wpts, trks);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
        return null;
    }
    private void addTrack(){
        if (gpx==null) return;
        clearTrack();
        trackLayers=new ArrayList<Layer>();
        ArrayList<WayPoint> wpts=gpx._wpts;
        for (int i=0;i<wpts.size();i++){
            WayPoint wpt=wpts.get(i);
            LatLong latLong=wpt._latLong;
            MarkerX marker1 = Utils.createTappableMarkerX(this,
                    R.drawable.marker_red, wpts.get(i));
            trackLayers.add(marker1);
        }
        ArrayList<Track> trks=gpx._trks;
        for (int i=0;i<trks.size();i++) {
            Track trk=trks.get(i);
            for (int j = 0; j<trk._trksegs.size(); j++){
                TrackSegment trkSeg=trk._trksegs.get(j);
                Polyline polyline = new Polyline(Utils.createPaint(
                        AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE),
                        (int) (2 * mapView.getModel().displayModel.getScaleFactor()),
                        Style.STROKE), AndroidGraphicFactory.INSTANCE);
                List<LatLong> latLongs = new ArrayList<>();
                for (int k=0;k<trkSeg._trackPoints.size();k++){
                    latLongs.add(trkSeg._trackPoints.get(k)._latLong);
                }
                polyline.setPoints(latLongs);
                trackLayers.add(polyline);
            }
        }
        mapView.getLayerManager().getLayers().addAll(trackLayers);
    }
    private void clearTrack() {
        if (trackLayers!=null) {
            for (int i=0;i<trackLayers.size();i++)
                mapView.getLayerManager().getLayers().remove(trackLayers.get(i));
        }
    }
    private void updateMyTrackLayer(){
        if (myTrackLayer!=null) mapView.getLayerManager().getLayers().remove(myTrackLayer);
        Polyline polyline = new Polyline(Utils.createPaint(
                AndroidGraphicFactory.INSTANCE.createColor(Color.RED),
                (int) (2 * mapView.getModel().displayModel.getScaleFactor()),
                Style.STROKE), AndroidGraphicFactory.INSTANCE);
        List<LatLong> latLongs = new ArrayList<>();
        for (int k=0;k<mySegment._trackPoints.size();k++){
            latLongs.add(mySegment._trackPoints.get(k)._latLong);
        }
        polyline.setPoints(latLongs);
        myTrackLayer=polyline;
        mapView.getLayerManager().getLayers().add(myTrackLayer);
    }
}

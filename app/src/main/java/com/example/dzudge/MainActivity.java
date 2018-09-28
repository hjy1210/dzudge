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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int READ_REQUEST_GPX = 42;

    private MapDataStore mapDataStore;
    private Gpx gpx;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        //setContentView(R.layout.activity_main);
        //map = "GTs/map/taiwan_ML.map";
        //mapFile = new File(Environment.getExternalStorageDirectory(), map);
        zoomLevel = 10;
        //mapView=(MapView) findViewById(R.id.map);
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
        mapView=(MapView)findViewById(R.id.map);
        //mapView = new MapView(this);
        //setContentView(mapView);

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
        Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();

    }
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
    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, "onStart", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(this, "onRestart", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
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
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "onPause", Toast.LENGTH_LONG).show();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Toast.makeText(this, "onStop", Toast.LENGTH_LONG).show();
    }

    private void setMapCenter() {
        /*
         * The map also needs to know which area to display and at what zoom level.
         * Note: this map position is specific to Berlin area.
         */
        //mapView.setCenter(new LatLong(52.517037, 13.38886));
        if (mapDataStore==null) return;
        double maxLat = mapDataStore.boundingBox().maxLatitude;
        double minLat = mapDataStore.boundingBox().minLatitude;
        double maxLong = mapDataStore.boundingBox().maxLongitude;
        double minLong = mapDataStore.boundingBox().minLongitude;
        LatLong center = new LatLong((maxLat + minLat) / 2, (maxLong + minLong) / 2);
        mapView.setCenter(new LatLong((maxLat + minLat) / 2, (maxLong + minLong) / 2));
        mapView.setZoomLevel(zoomLevel);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //locationManager.requestLocationUpdates(provider, 300, 5, this);
        /*Circle circle = new Circle(center, 50, Utils.createPaint(
                AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE), 0,
                Style.FILL), null);
        lastPos=circle;
        mapView.getLayerManager().getLayers().add(circle);
        Toast.makeText(this,"wait",Toast.LENGTH_SHORT).show();
        mapView.getLayerManager().getLayers().remove(lastPos);
        center=new LatLong((maxLat + minLat) / 2+0.01, (maxLong + minLong) / 2+0.01);
        circle = new Circle(center, 50, Utils.createPaint(
                AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE), 0,
                Style.FILL), null);
        mapView.getLayerManager().getLayers().add(circle);*/

    }

    @Override
    protected void onDestroy() {
        /*
         * Whenever your activity exits, some cleanup operations have to be performed lest your app
         * runs out of memory.
         */
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        Toast.makeText(this,"onDestroy",Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    public void openGpx(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_GPX);
    }

    public void openMap0(View view){
        final FileChooser dialog=new FileChooser(this);
        dialog.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(final File file) {
                setMapDataStore(file);
            }
        });
        dialog.setExtension(".map");
        dialog.showDialog();
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
        locationManager.removeUpdates(this);
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
        //setMapCenter(mapDataStore,zoomLevel);
        //locationManager.removeUpdates(this);

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
                    //textView.setText(readGpx(uri));
                    gpx = readGpx(uri);
                    addTrack();
                    Toast.makeText(this,"gpx:"+gpx,Toast.LENGTH_LONG).show();
                    int z = 0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
            //mapView.getLayerManager().getLayers().add(marker1);
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
                //mapView.getLayerManager().getLayers().add(polyline);
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
                isRecording=true;
                locationManager.requestLocationUpdates(provider, 300, 5, this);
                return true;
            case R.id.action_stop:
                isRecording=false;
                locationManager.removeUpdates(this);
                return true;
            case R.id.action_testing:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                //textView = (TextView) findViewById(R.id.textView);
                //textView.setText("楊宏章");
                intent = new Intent(this, EditMessageActivity.class);
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private Gpx readGpx(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        //StringBuilder sb=new StringBuilder();
        try {
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);
            ArrayList<WayPoint> wpts = GpxUtils.getWayPoints(doc);
            ArrayList<Track> trks = GpxUtils.getTracks(doc);
            //sb.append("size="+wpts.size()+"\n\n");
            //for (int i=0;i<wpts.size();i++){
            //    sb.append(wpts.get(i)+"\n");
            //}
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

    @Override
    public void onLocationChanged(Location location) {
        double lat = (location.getLatitude());
        double lng = (location.getLongitude());
        LatLong center=new LatLong(lat,lng);
        if (lastPos!=null){
            mapView.getLayerManager().getLayers().remove(lastPos);
        }
        //lastPos = new Circle(center, 5, Utils.createPaint(
        //            AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE), 0,
        //            Style.FILL), null);
        lastPos = Utils.createMarker(this,
                R.drawable.marker_green,center);
        mapView.getLayerManager().getLayers().add(lastPos);
        mapView.setCenter(center);
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
}

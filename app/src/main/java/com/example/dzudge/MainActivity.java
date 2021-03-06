package com.example.dzudge;

import com.obsez.android.lib.filechooser.ChooserDialog;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
import android.widget.ImageView;
//import android.widget.TextView;
import android.widget.Toast;


import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.TileCache;
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
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import android.support.design.widget.Snackbar;


public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,SensorEventListener {
    private static final int READ_REQUEST_GPX = 42;

    private MapDataStore mapDataStore;
    private Gpx gpx, myTrack;
    private MapView mapView;
    //private String map;
    //private File mapFile;
    private byte zoomLevel;
    private TileCache tileCache;
    private ArrayList<Layer> trackLayers;
    private Layer lastPos;
    private boolean isRecording;
    //private LocationManager locationManager;
    //private String provider;
    private boolean inspectLifeCycle;
    private TrackSegment mySegment;
    private Layer myTrackLayer;
    Menu menu;

    ///// copy from LocationUpdatesForegroundService
    //private ListView listView;
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // for compass
    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;


    // UI elements.
    //private Button mRequestLocationUpdatesButton;
    //private Button mRemoveLocationUpdatesButton;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inspectLifeCycle=false;
        myReceiver = new MyReceiver();
        if (GpxUtils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Criteria criteria = new Criteria();
        //provider = locationManager.getBestProvider(criteria, false);

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
        mapView=findViewById(R.id.map);
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
            //setMapDataStore(mapFile);
            //setMapCenter(mapDataStore, (byte) 13);
        } catch (Exception e) {
            /*
             * In case of map file errors avoid crash, but developers should handle these cases!
             */
            e.printStackTrace();
        }
        if (inspectLifeCycle) Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();

        // for compass
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        mPointer = findViewById(R.id.pointer);


    }
    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        if (inspectLifeCycle) Toast.makeText(this, "onStart", Toast.LENGTH_LONG).show();
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }*/
        //if (mapDataStore!=null && isRecording && mService!=null)
            //locationManager.requestLocationUpdates(provider, 300, 5, this);
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
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
        String str=PreferenceManager.getDefaultSharedPreferences(this).getString(
                "hjy", "");
        if (!str.equals("")) {
            //Toast.makeText(this,str,Toast.LENGTH_LONG).show();
            ArrayList<TrackPoint> trks=GpxUtils.extractWpts(str);
            if (trks.size()>0) updateCursorWithTrack(trks);
            ////// ODO
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("hjy","").apply();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));

        // for compass
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);


    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
        if (inspectLifeCycle) Toast.makeText(this, "onPause", Toast.LENGTH_LONG).show();

        // for compass
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }
    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

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
        if(isRecording)
            //locationManager.removeUpdates(this);
            mService.removeLocationUpdates();
        super.onDestroy();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_GPX && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri;
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
        String gpxString;

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
                isRecording=!isRecording;
                //menu.findItem(R.id.action_stop).setEnabled(true);
                //menu.findItem(R.id.action_record).setEnabled(false);
                MenuItem it=menu.findItem(R.id.action_record);
                if (isRecording) {
                    it.setTitle("停止記錄");
                    myTrack=new Gpx(new ArrayList<WayPoint>(), new ArrayList<Track>());
                    mySegment=new TrackSegment(new ArrayList<TrackPoint>());
                    ArrayList<TrackSegment> segments=new ArrayList<>();
                    segments.add(mySegment);
                    Track track=new Track("first track","test",segments);
                    myTrack._trks.add(track);
                    //locationManager.requestLocationUpdates(provider, 300, 5, this);
                    if (checkPermissions()) {
                        mService.requestLocationUpdates();
                    } else {
                        requestPermissions();
                    }
                }
                else {
                    it.setTitle("記錄軌跡");
                    //locationManager.removeUpdates(this);
                    Toast.makeText(this, myTrack.toString(),Toast.LENGTH_LONG).show();
                    mService.removeLocationUpdates();
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


    // onClickLeftShade, onClickLeftData prepare to use overlay options
    public void onClickLeftShade(View view){
        //Toast.makeText(this,"You tap me",Toast.LENGTH_SHORT);
        view.setVisibility(View.INVISIBLE);
        View btn= findViewById(R.id.left_data);
        btn.setVisibility(View.VISIBLE);
    }
    public void onClickLeftData(View view){
        //Toast.makeText(this,"You tap me",Toast.LENGTH_SHORT);
        view.setVisibility(View.INVISIBLE);
        View btn= findViewById(R.id.left_shade);
        btn.setVisibility(View.VISIBLE);
    }

    public void openMap(View view){
        ///// https://github.com/hedzr/android-file-chooser
         new ChooserDialog(this)
                //.withLayoutView(R.layout.activity_main)
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
        if (isRecording)
            //locationManager.removeUpdates(this);
            mService.removeLocationUpdates();
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
        if (isRecording)
            //locationManager.requestLocationUpdates(provider, 300, 5, this);
            mService.requestLocationUpdates();

    }
    private void setMapCenter() {
        if (mapDataStore==null) return;
        double maxLat = mapDataStore.boundingBox().maxLatitude;
        double minLat = mapDataStore.boundingBox().minLatitude;
        double maxLong = mapDataStore.boundingBox().maxLongitude;
        double minLong = mapDataStore.boundingBox().minLongitude;
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
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);
            ArrayList<WayPoint> wpts = GpxUtils.getWayPoints(doc);
            ArrayList<Track> trks = GpxUtils.getTracks(doc);
            return new Gpx(wpts, trks);
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void addTrack(){
        if (gpx==null) return;
        clearTrack();
        trackLayers=new ArrayList<>();
        ArrayList<WayPoint> wpts=gpx._wpts;
        for (int i=0;i<wpts.size();i++){
            //WayPoint wpt=wpts.get(i);
            //LatLong latLong=wpt._latLong;
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

        LatLong center = latLongs.get(latLongs.size()-1);
        if (lastPos!=null){
            mapView.getLayerManager().getLayers().remove(lastPos);
        }
        float degree=0;
        int count=latLongs.size();
        if (count>1){
            LatLong last=latLongs.get(count-1);
            LatLong last2=latLongs.get(count-2);
            //double dist=last.distance(last2);
            //if (dist>5){
            double angle=Math.atan2(last.longitude-last2.longitude,last.latitude-last2.latitude);
            degree=(float)(angle*180/Math.PI);
            //}
        }
        lastPos = Utils.createRotateMarker(this,
                R.drawable.navigation,center,degree-45);
        mapView.getLayerManager().getLayers().add(lastPos);
        mapView.setCenter(center);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(GpxUtils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(GpxUtils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }

    }
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                //Toast.makeText(MainActivity.this, GpxUtils.getLocationText(location),
                //        Toast.LENGTH_SHORT).show();
                ArrayList<TrackPoint> trackPoints=new ArrayList<>();
                trackPoints.add(new TrackPoint(new LatLong(location.getLatitude(),location.getLongitude()),
                        location.getAltitude(),new Date(location.getTime())));
                updateCursorWithTrack(trackPoints);
            }
        }
    }
    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        MenuItem it=menu.findItem(R.id.action_record);
        if (requestingLocationUpdates) {
            //mRequestLocationUpdatesButton.setEnabled(false);
            //mRemoveLocationUpdatesButton.setEnabled(true);
            isRecording=true;
            it.setTitle("停止紀錄");
        } else {
            //mRequestLocationUpdatesButton.setEnabled(true);
            //mRemoveLocationUpdatesButton.setEnabled(false);
            isRecording=false;
            it.setTitle("開始紀錄");
        }
    }
    private void updateCursorWithTrack(ArrayList<TrackPoint> trackPoints){
        //double lat = (location.getLatitude());
        //double lng = (location.getLongitude());
        //LatLong center=new LatLong(lat,lng);
        /*LatLong center = trackPoints.get(trackPoints.size()-1)._latLong;
        if (lastPos!=null){
            mapView.getLayerManager().getLayers().remove(lastPos);
        }
        float degree=0;
        int count=trackPoints.size();
        if (count>1){
            LatLong last=trackPoints.get(count-1)._latLong;
            LatLong last2=trackPoints.get(count-2)._latLong;
            //double dist=last.distance(last2);
            //if (dist>5){
                double angle=Math.atan2(last.longitude-last2.longitude,last.latitude-last2.latitude);
                degree=(float)(angle*180/Math.PI);
            //}
        }
        lastPos = Utils.createRotateMarker(this,
                R.drawable.marker_green,center,degree);
        mapView.getLayerManager().getLayers().add(lastPos);
        mapView.setCenter(center); */
        //Toast.makeText(this,center.toString(),Toast.LENGTH_LONG).show();
        //TrackPoint tpt=new TrackPoint(center,location.getAltitude(), Calendar.getInstance().getTime());
        //Toast.makeText(this,tpt.toString(),Toast.LENGTH_SHORT).show();
        mySegment._trackPoints.addAll(trackPoints);
        updateMyTrackLayer();

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ODO Auto-generated method stub

    }


}

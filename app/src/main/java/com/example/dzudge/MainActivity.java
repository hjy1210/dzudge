package com.example.dzudge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.mapsforge.map.reader.MapFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {
    private static final int READ_REQUEST_GPX = 42;
    private static final int READ_REQUEST_MAP = 43;
    public static final String EXTRA_MAP = "com.example.dzudge.MAP";
    private MapDataStore mapDataStore;
    private Gpx gpx;
    private String gpxString;
    private MapView mapView;
    private String map;
    private byte zoomLevel;
    private TileCache tileCache;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        map = "GTs/map/taiwan_ML.map";
        zoomLevel = 13;
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
        mapView = new MapView(this);
        setContentView(mapView);
        //setContentView(R.layout.activity_main);
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
            mapDataStore = getMapDataStore(map, tileCache);
            setMapCenter(mapDataStore, (byte) 13);
        } catch (Exception e) {
            /*
             * In case of map file errors avoid crash, but developers should handle these cases!
             */
            e.printStackTrace();
        }
    }

    private void setMapCenter(MapDataStore mapDataStore, byte zoomLevel) {
        /*
         * The map also needs to know which area to display and at what zoom level.
         * Note: this map position is specific to Berlin area.
         */
        //mapView.setCenter(new LatLong(52.517037, 13.38886));
        double maxLat = mapDataStore.boundingBox().maxLatitude;
        double minLat = mapDataStore.boundingBox().minLatitude;
        double maxLong = mapDataStore.boundingBox().maxLongitude;
        double minLong = mapDataStore.boundingBox().minLongitude;
        mapView.setCenter(new LatLong((maxLat + minLat) / 2, (maxLong + minLong) / 2));
        mapView.setZoomLevel(zoomLevel);
    }

    @NonNull
    private MapDataStore getMapDataStore(String map, TileCache tileCache) {
        /*
         * Now we need to set up the process of displaying a map. A map can have several layers,
         * stacked on top of each other. A layer can be a map or some visual elements, such as
         * markers. Here we only show a map based on a mapsforge map file. For this we need a
         * TileRendererLayer. A TileRendererLayer needs a TileCache to hold the generated map
         * tiles, a map file from which the tiles are generated and Rendertheme that defines the
         * appearance of the map.
         */
        File mapFile = new File(Environment.getExternalStorageDirectory(), map);
        MapDataStore mapDataStore = new MapFile(mapFile);
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

        /*
         * On its own a tileRendererLayer does not know where to display the map, so we need to
         * associate it with our mapView.
         */
        mapView.getLayerManager().getLayers().clear();
        mapView.getLayerManager().getLayers().add(tileRendererLayer);
        return mapDataStore;
    }

    @Override
    protected void onDestroy() {
        /*
         * Whenever your activity exits, some cleanup operations have to be performed lest your app
         * runs out of memory.
         */
        mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    public void openGpx(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_GPX);
    }

    public void openMap(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_MAP);
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
                    Toast.makeText(this,"gpx:"+gpx,Toast.LENGTH_LONG).show();
                    int z = 0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == READ_REQUEST_MAP && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                //try {
                    //textView.setText(readGpx(uri));
                    //loadMap(uri);
                    String filePath = uri.getPath();
                    map = filePath.substring(filePath.indexOf(":") + 1); /////很古怪的繞路方式，應有更好的辦法
                    mapDataStore=getMapDataStore(map,tileCache);
                    setMapCenter(mapDataStore,zoomLevel);

                //} catch (IOException e) {
                //    e.printStackTrace();
                //}
            }
        }
    }

    /*private void loadMap(Uri uri) throws IOException {
        String filePath = uri.getPath();
        filePath = filePath.substring(filePath.indexOf(":") + 1); /////很古怪的繞路方式，應有更好的辦法
        Intent intent = new Intent(this, DisplayMapActivity.class);
        intent.putExtra(EXTRA_MAP, filePath);

        startActivity(intent);

    }*/

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
            case R.id.action_selectmap:
                openMap(null);
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
}

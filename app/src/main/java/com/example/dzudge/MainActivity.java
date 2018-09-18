package com.example.dzudge;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private Gpx gpx;
    private String gpxString;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gpxString=Gpx.test();
    }
    public void openGpx(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    //textView.setText(readGpx(uri));
                    gpx=readGpx(uri);
                    int z=0;
                 }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        TextView textView;
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                //textView = (TextView) findViewById(R.id.textView);
                //textView.setText("Action_Settings");
                return true;
            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                //textView = (TextView) findViewById(R.id.textView);
                //textView.setText("楊宏章");
                openGpx(null);
                return true;
            case R.id.action_testing:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                //textView = (TextView) findViewById(R.id.textView);
                //textView.setText("楊宏章");
                Intent intent=new Intent(this,EditMessageActivity.class);
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    private Gpx readGpx(Uri uri) throws IOException{
        InputStream inputStream = getContentResolver().openInputStream(uri);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        //StringBuilder sb=new StringBuilder();
        try {
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);
            ArrayList<WayPoint> wpts= GpxUtils.getWayPoints(doc);
            ArrayList<Track> trks=GpxUtils.getTracks(doc);
            //sb.append("size="+wpts.size()+"\n\n");
            //for (int i=0;i<wpts.size();i++){
            //    sb.append(wpts.get(i)+"\n");
            //}
            return new Gpx(wpts,trks);
        } catch(ParserConfigurationException e){
            e.printStackTrace();
        }
        catch (SAXException e) {
            e.printStackTrace();
        } finally{
            inputStream.close();
        }
        return null;
    }

}

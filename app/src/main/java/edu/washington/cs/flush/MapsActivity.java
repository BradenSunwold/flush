package edu.washington.cs.flush;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Gravity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.Typeface;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private double left, right, top, bottom;
    Button search;
    CheckBox acc;
    CheckBox uni;
    CheckBox chan;
    private static final String TAG = "MapsActivity";
    private int REQUEST_CODE;
    private FileParse fp;
    private File mFile;
    private boolean accessible = false;
    private boolean unisex = false;
    private boolean changingStation = false;
    private String[] params = new String[5];
    
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mMap.clear();
                    checkMarkers();
                } catch (java.io.FileNotFoundException e){
                    Log.e(TAG, "Failed to find file" + e);
                }
            }
        });

        acc = (CheckBox) findViewById(R.id.accessible);
        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accessible = !accessible;
            }
        });

        uni = (CheckBox) findViewById(R.id.unisex);
        uni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unisex = !unisex;
            }
        });

        chan = (CheckBox) findViewById(R.id.changingstation);
        chan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changingStation = !changingStation;
            }
        });

        try {
            readFile();
        } catch (java.lang.Exception e){
            Log.e(TAG, "Failed to parse the file" + e);
        }

    }

    public void readFile() throws java.lang.Exception {
        InputStream is = getResources().openRawResource(R.raw.uw_buildings);
        File Dir = getDir("buildings", Context.MODE_PRIVATE);
        mFile = new File(Dir, "uw_buildings.txt");
        FileOutputStream os = new FileOutputStream(mFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();
        fp = new FileParse(mFile.getAbsolutePath());

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    private boolean checkCoordinates(LatLng ll){
        double latitude = ll.latitude;
        double longitude = ll.longitude;
        return ((latitude > 0 && latitude < top) || (latitude < 0 && latitude > top))
                && ((latitude > 0 && latitude > bottom) || (latitude < 0 && latitude < bottom))
                && ((longitude > 0 && longitude < left) || (longitude < 0 && longitude > left))
                && ((longitude > 0 && longitude > right) || (longitude < 0 && longitude < right));
    }



    private void addMarker() {
        LatLng latLng = new LatLng(Double.parseDouble(fp.getLat()), Double.parseDouble(fp.getLong()));
        params[0] = fp.getFloor() != null ? fp.getFloor() : "?";
        params[1] = fp.getStalls() != null ? fp.getStalls() : "?";
        params[2] = fp.getHandicap() != null ? fp.getHandicap() : "?";
        params[3] = fp.getUnisex() != null ? fp.getUnisex() : "?";
        params[4] = fp.getChangingStation() != null ? fp.getChangingStation() : "?";

        String snippet =
                "Floor: " + params[0] + "\n" +
                        "Stalls: " + params[1] + "\n" +
                        "Accessible: " + params[2] + "\n" +
                        "Unisex: " + params[3] + "\n" +
                        "ChangingStation: " + params[4];
        mMap.addMarker(new MarkerOptions().position(latLng).title(fp.getBuildingName()).snippet(snippet));
    }

    private void checkMarkers() throws java.io.FileNotFoundException{
        VisibleRegion vr = mMap.getProjection().getVisibleRegion();
        left = vr.latLngBounds.southwest.longitude;
        top = vr.latLngBounds.northeast.latitude;
        right = vr.latLngBounds.northeast.longitude;
        bottom = vr.latLngBounds.southwest.latitude;

        // Add a markers in uw
        Scanner input = new Scanner(mFile);
        String line = input.nextLine();
        while (input.hasNextLine()) {
            line = input.nextLine();
            fp.findData(line);
            if (unisex && accessible && changingStation) {
                if ((fp.getUnisex() != null && fp.getUnisex().equals("Y")) &&
                        (fp.getHandicap() != null && fp.getHandicap().equals("Y")) &&
                        (fp.getChangingStation() != null && fp.getChangingStation().equals("Y"))) {
                    addMarker();
                }
            } else if (unisex && accessible) {
                if ((fp.getUnisex() != null && fp.getUnisex().equals("Y")) &&
                        (fp.getHandicap() != null && fp.getHandicap().equals("Y"))) {
                    addMarker();
                }
            } else if (unisex && changingStation) {
                if ((fp.getUnisex() != null && fp.getUnisex().equals("Y")) &&
                        (fp.getChangingStation() != null && fp.getChangingStation().equals("Y"))){
                    addMarker();
                }
            } else if (accessible && changingStation) {
                if ((fp.getHandicap() != null && fp.getHandicap().equals("Y")) &&
                        (fp.getChangingStation() != null && fp.getChangingStation().equals("Y"))){
                    addMarker();
                }
            } else if (accessible) {
                if (fp.getHandicap() != null && fp.getHandicap().equals("Y")){
                    addMarker();
                }
            } else if (changingStation) {
                if (fp.getChangingStation() != null && fp.getChangingStation().equals("Y")){
                    addMarker();
                }
            } else if (unisex) {
                if (fp.getUnisex() != null && fp.getUnisex().equals("Y")) {
                    addMarker();
                }
            } else {
                addMarker();
            }

        }


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                startActivityForResult(new Intent(MapsActivity.this, RateActivity.class), REQUEST_CODE);
            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }


            @Override
            public View getInfoContents(Marker marker) {

                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }
    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}

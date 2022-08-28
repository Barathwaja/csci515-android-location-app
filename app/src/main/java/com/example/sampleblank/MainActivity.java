package com.example.sampleblank;


import static com.example.sampleblank.Constants.DIRECTION_URL;
import static com.example.sampleblank.Constants.RESPONSE_FORMAT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sampleblank.model.Store;
import com.example.sampleblank.util.DirectionsJSONParser;
import com.example.sampleblank.util.viewHolder;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // CONSTANTS
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DEFAULT_ZOOM = 10;
    private static final int REQUEST_CODE = 100;

    // Properties
    private GoogleMap map;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng originLocation;
    private LatLng destinationLocation;
    private Polyline polyline;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;


    // Shimmer Animated Effect
    private ShimmerFrameLayout shimmerFrameLayout;
    public String _cacheStoreName = "";

    public TextView errorMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout);

        recyclerView = findViewById(R.id.recyclerView);

        errorMessage = findViewById(R.id.errorMessage);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //Google Maps Settings Customization
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setPadding(0, 0, 0, 1000);

        getMyLocation();
        loadFirebaseData();
    }


    /**
     * To check whether the Mobile application has given Location Permission or Not
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (!verifyAllPermissions(grantResults)) {
                Toast.makeText(getApplicationContext(), "No sufficient permissions", Toast.LENGTH_LONG).show();
            } else {
                getMyLocation();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * @param grantResults
     * @return
     */
    private boolean verifyAllPermissions(int[] grantResults) {

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    /**
     * If permission is granted then returns my current location
     */
    private void getMyLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Inside onLocation Changed Function");

                map.clear();

                originLocation = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(originLocation, DEFAULT_ZOOM));

                if (originLocation != null && destinationLocation != null) {
                    drawRoute();
                }
            }
        };

        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                map.setMyLocationEnabled(true);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        10000,
                        0,
                        locationListener);

            } else {
                Log.d(TAG, "Permission Check Failed");

                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
            }
        }
    }


    private void drawRoute() {
        String url = getDirectionsURL(originLocation, destinationLocation);

        DownloadTask downloadTask = new DownloadTask();

        downloadTask.execute(url);
    }


    private String getDirectionsURL(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String key = "key=" + getString(R.string.google_maps_key);

        String parameters = str_origin + "&" + str_dest + "&" + key;

        return DIRECTION_URL + RESPONSE_FORMAT + "?" + parameters;
    }


    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        Log.d(TAG, "Inside Download URL Function");

        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.e(TAG, "Exception occurred on download" + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d(TAG, "DownloadTask : " + data);
            } catch (Exception e) {
                Log.e(TAG, "Download Task Function Failed" + e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }


    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                Log.d(TAG, "Inside Parse Exception");
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (point.get("lat") != null || point.get("lng") != null) {
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (polyline != null) {
                    polyline.remove();
                }
                polyline = map.addPolyline(lineOptions);

                Marker marker = map.addMarker(new MarkerOptions()
                        .position(destinationLocation)
                        .title(_cacheStoreName));
                marker.showInfoWindow();
            } else
                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This function will load the data's from the Firebase Cloud Database
     */
    private void loadFirebaseData() {
        try {
            Query query = FirebaseDatabase.getInstance().getReference().child("store");

            FirebaseRecyclerOptions<Store> options = new FirebaseRecyclerOptions.Builder<Store>()
                    .setQuery(query, new SnapshotParser<Store>() {
                        @Override
                        public Store parseSnapshot(@NotNull DataSnapshot snapshot) {
                            return new Store((Long) snapshot.child("storeId").getValue(),
                                    snapshot.child("storeName").getValue().toString(),
                                    snapshot.child("storeOffer").getValue().toString(),
                                    (Double) snapshot.child("storeLatitude").getValue(),
                                    (Double) snapshot.child("storeLongitude").getValue(),
                                    snapshot.child("storeOfferDescription").getValue().toString(),
                                    snapshot.child("storeOfferExpiration").getValue().toString(),
                                    snapshot.child("storeImage").getValue().toString()
                            );
                        }
                    })
                    .build();

            adapter = new FirebaseRecyclerAdapter<Store, viewHolder>(options) {
                @Override
                public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item, parent, false);

                    return new viewHolder(view);
                }

                @Override
                protected void onBindViewHolder(viewHolder holder,
                                                @SuppressLint("RecyclerView") final int position,
                                                Store storeModel) {

                    holder.setStoreTitle(storeModel.getStoreName());
                    holder.setStoreOffer(storeModel.getStoreOffer());
                    holder.setStoreOfferDescription(storeModel.getStoreOfferDescription());
                    holder.setStoreOfferExpiration("Exp. - ".concat(storeModel.getStoreOfferExpiration()));

                    Glide.with(holder.storeLogo.getContext())
                            .load(storeModel.getStoreImage())
                            .placeholder(R.drawable.ic_shop_default_icon)
                            .into(holder.storeLogo);

                    holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (storeModel.getStoreLatitude() != null && storeModel.getStoreLongitude() != null) {
                                _cacheStoreName = storeModel.getStoreName();

                                destinationLocation = new LatLng(
                                        storeModel.getStoreLatitude(),
                                        storeModel.getStoreLongitude()
                                );

                                drawRoute();
                            }
                        }
                    });

                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
            };

            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.startListening();
        } catch (Exception e) {
            Log.e(TAG, "Exception" + e.getMessage());

            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            errorMessage.setVisibility(View.VISIBLE);
        }

    }
}
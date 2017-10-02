package com.itshareplus.googlemapdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Modules.DirectionFinderListener;
import Modules.Route;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback
        ,LocationListener
        ,GoogleMap.OnMapClickListener
        ,GoogleApiClient.ConnectionCallbacks
        ,GoogleApiClient.OnConnectionFailedListener
        ,DirectionFinderListener
        ,NavigationView.OnNavigationItemSelectedListener
        ,GoogleMap.OnPolylineClickListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mDatabase;

    private Button btnFindPath;
    private String destination;
    private double curLat;
    private double curLng;
    private String origin;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private View header;
    private TextView currentUser;
    private Menu menu;
    private Toolbar toolbar;

    Marker myMarker;
//    private ToggleButton pMarkerOnOff; //開關按鈕
    private List<Marker> markers = new ArrayList<Marker>(); //儲存Marker資訊之陣列
    final List<Double> parkinglotLatList = new ArrayList<>(); //儲存緯度之陣列
    final List<Double> parkinglotLngList = new ArrayList<>(); //儲存經度之陣列
    final List<String> parkinglotNameList = new ArrayList<>(); //儲存停車場名稱之陣列
    final List<String> parkinglotLeftspaceList = new ArrayList<>(); //儲存停車場剩餘車位之陣列
    final List<String> parkinglotChargewayList = new ArrayList<>(); //儲存停車場收費資訊之陣列
    final List<String> parkinglotHrsList = new ArrayList<>(); //儲存停車場營業時間之陣列
    final List<String> parkinglotAddressList = new ArrayList<>(); //儲存停車場地址之陣列

    final List<String> emptyData = new ArrayList<>();
    final List<String> latitudeList = new ArrayList<>();
    final List<String> longitudeList = new ArrayList<>();
    final List<Polyline> polylinesList = new ArrayList<>();
    final List<String> polylinesId = new ArrayList<>();
    private int  polyLineSize;
    private ActionBarDrawerToggle mtoggle;

    private Button BtnOpen;
    private Button BtnClose;

    private static final String TAG = "FUCK";
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private EditText location_tf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        toolbar = (Toolbar) findViewById(R.id.nav_action);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference("publicParkInfo");
        mDatabase.push().setValue(myMarker);

        ImageButton BtnSearch = (ImageButton) findViewById(R.id.Bsearch);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final Geocoder geocoder = new Geocoder(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        BtnSearch.setOnClickListener(new Button.OnClickListener() {

            @Override

            public void onClick(View v) {

                String location = location_tf.getText().toString();
                List<Address> addressList = null;

                if (location != null || !location.equals("")) {
                    try {
                        List<Address> list = geocoder.getFromLocationName(location, 1);
                        Address address = list.get(0);
                        double lat = address.getLatitude();
                        double lng = address.getLongitude();
                        LatLng ll = new LatLng(lat,lng);
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
                        mMap.moveCamera(update);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    Address address = addressList.get(0);
//                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.2f));
                } else {

                }
            }

        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mtoggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mtoggle);
        mtoggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();

        header = navigationView.inflateHeaderView(R.layout.nav_header_main);
        currentUser = (TextView) header.findViewById(R.id.userState);
        menu = navigationView.getMenu();

        BtnOpen = (Button) findViewById(R.id.BtnOpen);
        BtnClose = (Button) findViewById(R.id.BtnClose);
        BtnOpen.setVisibility(View.GONE);
        BtnClose.setVisibility(View.GONE);

        BtnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(markers.isEmpty()) {
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                //須建立一個ParkinglotInfo的class 詳細見ParkinglotInfo.java
                                ParkinglotInfo parkinglotMarker = d.getValue(ParkinglotInfo.class);
                                double parkinglotLat = parkinglotMarker.lat; //停車場緯度
                                double parkinglotLng = parkinglotMarker.lng; //停車場經度
                                destination = Double.toString(parkinglotLat) + "," + Double.toString(parkinglotLng);
                                String parkinglotName = parkinglotMarker.name; //停車場名稱
                                String parkinglotLeftspace = parkinglotMarker.leftspace; //停車場剩餘位置
                                String parkinglotChargeway = parkinglotMarker.chargeway; //停車場收費方式
                                String parkinglotHrs = parkinglotMarker.businessHrs;    //停車場營業時間
                                String parkinglotAddress = parkinglotMarker.location;   //停車場地址
                                //將資料存至相關陣列
                                parkinglotLatList.add(parkinglotLat);
                                parkinglotLngList.add(parkinglotLng);
                                parkinglotNameList.add(parkinglotName);
                                parkinglotAddressList.add(parkinglotAddress);
                                parkinglotLeftspaceList.add(parkinglotLeftspace);
                                parkinglotChargewayList.add(parkinglotChargeway);
                                parkinglotHrsList.add(parkinglotHrs);
                            }
                            //確認陣列大小
                            int arrayListSize = parkinglotAddressList.size();
                            Log.d("陣列長度", String.valueOf(arrayListSize));
                            //利用counter依序使用陣列中的資料建立Marker
                            for (Integer counter = 0; counter <= arrayListSize - 1; counter++) {
                                //將經緯度資料設為location供建立Marker時使用
                                LatLng location = new LatLng(parkinglotLatList.get(counter), parkinglotLngList.get(counter));
                                //建立Marker
                                myMarker = mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(parkinglotNameList.get(counter))
                                        .snippet("剩餘車位: " + parkinglotLeftspaceList.get(counter) +
                                                "\n收費方式: " + parkinglotChargewayList.get(counter) +
                                                "\n營業時間: " + parkinglotHrsList.get(counter) +
                                                "\n地址: " + parkinglotAddressList.get(counter)).visible(true));
                                myMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_placeholder));
                                //將各Marker存入陣列
                                markers.add(myMarker);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else{
                    Log.d("陣列狀態" , "陣列已經有資料了");
                }
            }
        });

        BtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!markers.isEmpty()) {
                    int arrayListSize = parkinglotAddressList.size();
                    //利用counter將Marker依序移除
                    for (Integer counter = 0; counter <= arrayListSize - 1; counter++) {
                        markers.get(counter).remove();
                    }
                    //將儲存各Marker的陣列清空
                    markers.clear();
                    parkinglotLatList.clear();
                    parkinglotLngList.clear();
                    parkinglotNameList.clear();
                    parkinglotAddressList.clear();
                    parkinglotLeftspaceList.clear();
                    parkinglotChargewayList.clear();
                    parkinglotHrsList.clear();
                } else{
                    Log.d("陣列狀態" , "陣列已經是空的了");
                }
            }
        });



    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnMapClickListener(this);
        googleMap.setOnPolylineClickListener(this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);

            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMapClickListener(this);
//        curLat = mLastLocation.getLatitude();
//        curLng = mLastLocation.getLongitude();
        origin = Double.toString(curLat)+","+Double.toString(curLng);
        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(adapter);
        checkEmpty();



    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationRequest mLocationRequest;
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
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                location_tf.setText(place.getAddress());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));


        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            double currentZoom = 15.5;
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("目前縮放大小" , String.valueOf(cameraPosition.zoom));
                if(cameraPosition.zoom >= currentZoom){
                    BtnOpen.performClick();
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){

                        @Override
                        public boolean onMarkerClick(Marker marker) {

                            Toast.makeText(MapsActivity.this , marker.getTitle(),Toast.LENGTH_SHORT ).show();
                            return false;
                        }
                    });
                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng latLng) {
                            for(Marker marker : markers){

                            }
                        }
                    });
                } else{
                    Toast.makeText(MapsActivity.this , "請將地圖放大一點查看停車場在哪" , Toast.LENGTH_SHORT).show();
                    BtnClose.performClick();
                }
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onStart(){
        super.onStart();
        checkStatus();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if(mtoggle.onOptionsItemSelected(item)){
           return true;
       }

       return super.onOptionsItemSelected(item);
    }
//
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_register) {
            startActivity(new Intent(MapsActivity.this , RegisterPage.class));

        } else if (id == R.id.nav_login) {
            startActivity(new Intent(MapsActivity.this, LoginPage.class));
        } else if(id == R.id.nav_logout) {

            mAuth.signOut();
            startActivity(new Intent(MapsActivity.this , MapsActivity.class));

        } else if (id == R.id.nav_update) {
            startActivity(new Intent(MapsActivity.this , SaveUserInfo.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void checkStatus(){

        FirebaseUser checkUser= mAuth.getCurrentUser();

        final MenuItem navUpdate = menu.findItem(R.id.nav_update);
        final MenuItem navLogin = menu.findItem(R.id.nav_login);
        final MenuItem navRegister = menu.findItem(R.id.nav_register);
        final MenuItem navLogout = menu.findItem(R.id.nav_logout);

        if(checkUser != null){

            DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference memberRef = RootRef.child("member").child(checkUser.getUid()).child("name");

            memberRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String Name = dataSnapshot.getValue(String.class);
                    currentUser.setText("歡迎，" + Name);

                    navUpdate.setVisible(true);
                    navLogout.setVisible(true);
                    navLogin.setVisible(false);
                    navRegister.setVisible(false);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        } else{
            currentUser.setText("歡迎，請登入");
            navUpdate.setVisible(false);
            navLogout.setVisible(false);
            navLogin.setVisible(true);
            navRegister.setVisible(true);
        }

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    private void checkEmpty() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("parking");


        for (Integer counter = 1 ; counter<=6 ; counter++){

            DatabaseReference setRef = databaseReference.child(String.valueOf(counter));
            setRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot parkSnapshot : dataSnapshot.getChildren()) {

                        ParkingInfo parkingInfo = parkSnapshot.getValue(ParkingInfo.class);
                        String empty = parkingInfo.getEmpty();
                        String latitude = parkingInfo.getLat();
                        String longitude = parkingInfo.getLon();
                        emptyData.add(empty);
                        latitudeList.add(latitude);
                        longitudeList.add(longitude);
                    }
                    drawLine();
                    emptyData.clear();
                    latitudeList.clear();
                    longitudeList.clear();

                    polyLineSize = polylinesList.size();
                    Log.d("polyLineSize" , String.valueOf(polyLineSize));
                    Log.d("polyLineId" , String.valueOf(polylinesId));
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        AlertDialog.Builder warmningDialog = new AlertDialog.Builder(MapsActivity.this);
        warmningDialog.setTitle("注意地圖上標線");
        warmningDialog.setMessage("紅色標線表示該區域所剩空位不多" + '\n'+
                "綠色標線表示該區域還有空位" + '\n'+
                "藍色標線表示該區域空位還有很多");

        warmningDialog.setPositiveButton("好", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        warmningDialog.show();



    }
    private void drawLine() {

        int sizeOfList = latitudeList.size();

        float countYes = Collections.frequency(emptyData, "yes");
        float countNo = Collections.frequency(emptyData, "no");
        float emptyRate = countYes/(countYes+countNo);
        Log.d("yes" , String.valueOf(countYes));
        Log.d("no" , String.valueOf(countNo));
        Log.d("emptyRate " , String.valueOf(emptyRate));

        double startLat = Double.parseDouble(latitudeList.get(0));
        double endLat  = Double.parseDouble(latitudeList.get(sizeOfList-1));
        double startLon = Double.parseDouble(longitudeList.get(0));
        double endLon  = Double.parseDouble(longitudeList.get(sizeOfList-1));


        if( 1 >= emptyRate && emptyRate >= 0.85 ){

            LatLng startPlace = new LatLng(startLat,startLon);
            LatLng endPlace = new LatLng(endLat,endLon);
            Polyline polyline = this.mMap.addPolyline(new PolylineOptions().add(startPlace).add(endPlace).width(12).color(Color.rgb(65,105,225)));
            polylinesList.add(polyline);
            polyline.setClickable(true);
            polylinesId.add(polyline.getId());
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPlace,15));


        }
        if (0.85 > emptyRate && emptyRate >= 0.4){
            LatLng startPlace = new LatLng(startLat,startLon);
            LatLng endPlace = new LatLng(endLat,endLon);

            Polyline polyline = this.mMap.addPolyline(new PolylineOptions().add(startPlace).add(endPlace).width(12).color(Color.rgb(78,238,148)));
            polylinesList.add(polyline);
            polyline.setClickable(true);
            polylinesId.add(polyline.getId());
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPlace,15));

        }
        if(0.4 > emptyRate && emptyRate!=0){
            LatLng startPlace = new LatLng(startLat,startLon);
            LatLng endPlace = new LatLng(endLat,endLon);

            Polyline polyline = this.mMap.addPolyline(new PolylineOptions().add(startPlace).add(endPlace).width(12).color(Color.rgb(205,85,85)));
            polylinesList.add(polyline);
            polyline.setClickable(true);
            polylinesId.add(polyline.getId());
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPlace,15));

        }

    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onPolylineClick(Polyline polyline) {

        String id = polyline.getId();
        Toast.makeText(MapsActivity.this , id , Toast.LENGTH_SHORT).show();

    }
}

package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearByDriverRequests;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengerLatitudes;
    private ArrayList<Double> passengerLongitudes;
    private ArrayList<String> requestcarUsernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnGetRequests=findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

         listView=findViewById(R.id.requestListView);
        nearByDriverRequests=new ArrayList<>();

        passengerLatitudes= new ArrayList<>();
        passengerLongitudes =new ArrayList<>();

        requestcarUsernames=new ArrayList<>();


        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,nearByDriverRequests);

        listView.setAdapter(adapter);
        nearByDriverRequests.clear();

        locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);

        if(Build.VERSION.SDK_INT<23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

           initailaizeLoactionListener();

        }
        listView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.driverLogoutItem){
            ParseUser. logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if(e==null){
                    finish();
                }}
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {


        locationManager= (LocationManager)getSystemService(LOCATION_SERVICE);

        if(Build.VERSION.SDK_INT<23) {

            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestsListView(currentDriverLocation);

        }else if(Build.VERSION.SDK_INT>=23){

            if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(DriverRequestListActivity.this,new  String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);


            } else {

                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);
            }
        }
    }

    private void updateRequestsListView(Location driverLocation) {

        if(driverLocation !=null){

            saveDriverLocationToParse(driverLocation);


            final ParseGeoPoint driverCurrentLocation= new ParseGeoPoint(driverLocation.getLatitude(),driverLocation.getLongitude());

            ParseQuery<ParseObject> requestCarQuery=ParseQuery.getQuery("RequestCar");

            requestCarQuery.whereNear("passengerLocation",driverCurrentLocation);
            requestCarQuery.whereDoesNotExist("driverOfMe");
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {
                        if (objects.size() > 0) {

                            if(nearByDriverRequests.size()>0){
                                nearByDriverRequests.clear();
                            }

                            if(passengerLongitudes.size()>0){
                                nearByDriverRequests.clear();
                            }

                            if (passengerLatitudes.size()>0){
                                passengerLatitudes.clear();
                            }


                            for (ParseObject nearRequest : objects) {

                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRequest.get("passengerLocation");
                                Double milesdiatanceToPassenger = driverCurrentLocation.distanceInMilesTo(pLocation);

                                //5.345975893*10
                                //53.37123732
                                //57 /10=5.7
                                float roundedDistanceValue = Math.round(milesdiatanceToPassenger * 10) / 10;
                                nearByDriverRequests.add("There are " + roundedDistanceValue + " miles to" + nearRequest.get("username"));

                                passengerLatitudes.add(pLocation.getLatitude());
                                passengerLongitudes.add(pLocation.getLongitude());

                                requestcarUsernames.add(nearRequest.get("username") + "");
                            }

                        }
                        else{
                            Toast.makeText(DriverRequestListActivity.this,"Sorry. There are no requests yet",Toast.LENGTH_LONG).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });



    }}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode== 1000 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(DriverRequestListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {

                initailaizeLoactionListener();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

               Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //Toast.makeText(this,"Clicked",Toast.LENGTH_LONG).show();

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION )== PackageManager.PERMISSION_GRANTED){
        Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(cdLocation!=null) {
                Intent intent = new Intent(this, ViewLocationMapActivity.class);
                intent.putExtra("dLatitude", cdLocation.getLatitude());
                intent.putExtra("dLongitude", cdLocation.getLongitude());
                intent.putExtra("pLatitude", passengerLatitudes.get(position));
                intent.putExtra("pLongitude", passengerLongitudes.get(position));


                intent.putExtra("rUsername", requestcarUsernames.get(position));
                startActivity(intent);
            }
         }
    }

    private void initailaizeLoactionListener(){

        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


    }

    private void saveDriverLocationToParse(Location location){

        ParseUser driver= ParseUser.getCurrentUser();
        ParseGeoPoint driverLocation= new ParseGeoPoint(location.getLatitude(),location.getLongitude());
        driver.put("driverLocation",driverLocation);
        
        driver.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){

                    Toast.makeText(DriverRequestListActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}

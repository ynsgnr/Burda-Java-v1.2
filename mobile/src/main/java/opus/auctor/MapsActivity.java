package opus.auctor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng location;
    Intent intent;
    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        intent = getIntent();
        location= new LatLng(intent.getExtras().getDouble("Lat"),intent.getExtras().getDouble("Long"));
        Snackbar.make(findViewById(R.id.map) ,getResources().getString(R.string.locationChooseInfo) , Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent result = new Intent();
        result.putExtra("Long", location.latitude);
        result.putExtra("Lat",location.latitude);
        setResult(Activity.RESULT_OK, result);
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
        // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(location));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,12));

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent();
                //send data to addClass activity intent.putExtra("key", value);
                intent.putExtra("Lat",latLng.latitude);
                intent.putExtra("Long",latLng.longitude);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                                @Override
                                                public void onMapLongClick(LatLng latLng) {
                                                    Intent intent = new Intent();
                                                    //send data to addClass activity intent.putExtra("key", value);
                                                    intent.putExtra("Lat",latLng.latitude);
                                                    intent.putExtra("Long",latLng.longitude);
                                                    setResult(Activity.RESULT_OK, intent);
                                                    finish();
                                                }
                                            }
        );
    }


}

package opus.auctor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.commons.lang3.SerializationUtils;

import java.util.Calendar;
import java.util.Date;

public class timedClassService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient client=null;
    LocationRequest mLocationRequest;
    String SERVICE="Timed Class Service";
    Class c;
    Intent tintent;
    PendingIntent pintent;
    AlarmManager alarm;
    Calendar cal;
    classInteractions mll;


    @Override
    public int onStartCommand (Intent intent,int i,int j){
        Log.d(SERVICE, "Started");
        if (intent != null) {
            tintent=intent;
            try {
                c = SerializationUtils.deserialize((byte[])intent.getSerializableExtra("class"));
            }
            catch (java.lang.RuntimeException e){
                e.printStackTrace();
            }
            Database db = new Database(getApplicationContext());
            if (c == null ) {
                c=db.GetClassById(Integer.decode(intent.getDataString()));
            }
            if (c!=null){
                db = new Database(getApplicationContext());
                if (db.getAtt(c, new Date(Calendar.getInstance().getTimeInMillis())) == 0){
                    c.setTime();
                    alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    Log.d(SERVICE, "Found class:" + c.s_name + " " + c.name + "in: " + intent.getExtras().toString());
                    cal = Calendar.getInstance();
                    if (c.isClassTime()) {
                        if (isLocationEnabled(getApplicationContext())) {
                            Log.i(SERVICE, "Location is enabled");
                            if (client.isConnected()) {
                                if(LocationServices.FusedLocationApi.getLocationAvailability(client)==null){
                                    classInteractions clIn = new classInteractions();
                                    clIn.initilaze(c, getApplicationContext(), client);
                                    clIn.sendNotification();
                                }
                                else {
                                    mll = new classInteractions();
                                    mll.initilaze(c, getApplicationContext(), client);
                                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED)
                                        LocationServices.FusedLocationApi.requestLocationUpdates(client, mLocationRequest, mll);
                                }
                            } else {
                                client.connect();
                                Log.d(SERVICE, "Waiting for google api");
                                Intent Tintnet = new Intent(getApplicationContext(), timedClassService.class);

                                byte data [] = SerializationUtils.serialize(c);
                                Tintnet.putExtra("class",data);
                                //Tintnet.putExtra("class", c);

                                Tintnet.setData(Uri.parse(Integer.toString(c.id)));
                                pintent = PendingIntent.getService(timedClassService.this, c.primaryTimeId/*request code*/, Tintnet, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);
                                alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                alarm.cancel(pintent);

                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.SECOND, 3);

                                if (Integer.valueOf(Build.VERSION.SDK_INT) >= 19) {
                                    Log.i("Add Class:", "Phone version is bigger then 19");
                                    alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
                                } else {
                                    Log.i("Add Class:", "Phone version is smaller then 19");
                                    alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pintent);
                                }
                            }
                        } else {
                            Log.i(SERVICE, "Location is disabled");
                            classInteractions clIn = new classInteractions();
                            clIn.initilaze(c, getApplicationContext(), client);
                            if (c.notify) {
                                Log.d("Timed Class Service", "Notification:" + c.notify);
                                clIn.sendNotification();
                            }
                        }
                    } else {
                        Log.i(SERVICE, "Time mismatch:" + c.time0.getString() + "-" + c.time1.getString() + ":"
                                + Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + ":" + Integer.toString(cal.get(Calendar.MINUTE)));
                        c.setAlarm(getApplicationContext());
                    }
                }
                else Log.i(SERVICE, "No class found");
            }
        }
        else Log.i(SERVICE,"No intent found");
        return START_NOT_STICKY;
    }



    @Override
    public void onConnected(Bundle connectionHint){
        mll = new classInteractions();
        mll.initilaze(c,getApplicationContext(),client);
        Log.i("PlayServices","Connected");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(client,mLocationRequest,mll);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if(c.notify) {
                Log.d("Timed Class Service", "Notification:" + c.notify);
                mll.sendNotification();
            }
        }
       else{
            Log.d(SERVICE,"created location update listener");
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("PlayServices", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        classInteractions clIn = new classInteractions();
        clIn.initilaze(c,getApplicationContext(),client);
        if(c.notify) {
            Log.d("Timed Class Service", "Notification:" + c.notify);
            clIn.sendNotification();
        }
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("PlayServices", "Connection suspended");
        client.connect();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(SERVICE,"Destroying Service");
        if (client.isConnected()) {
            client.disconnect();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(SERVICE,"Creating service");

        if (client == null) {
            Log.i(SERVICE,"Starting Play Services");
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            client.connect();
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
}

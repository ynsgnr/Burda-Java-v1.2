package opus.auctor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import opus.auctor.viewElements.CircularProgressBar;
import opus.auctor.viewElements.CircularProgressBar.ProgressAnimationListener;

public class classDetails extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {

    CircularProgressBar c;
    GoogleApiClient mGoogleApiClient;
    int MY_PERMISSIONS_LOCATION;
    GoogleMap mMap;
    LatLng location;
    Class tmp;
    int PLACE_PICKER_REQUEST = 1;
    SupportMapFragment mapFragment;
    CaldroidFragment caldroidFragment;
    HashMap <Date,Integer> att;
    int attended=0;
    int notAtt=0;
    Drawable notAttColor;
    Drawable attColor;
    Drawable noClassColor;
    int IS_DELETED_REQUEST = 2;
    ToggleButton notification;
    ArrayList<Date> outClass=new ArrayList<>();

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3646358189824390~7125508266");

        AdView mAdView = (AdView) findViewById(R.id.adViewDetails);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("FF65CB156F114B4BCE365F6FC45A0BBC")
                .build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        tmp = (Class) intent.getSerializableExtra("class");
        if(tmp==null){
            Log.e("Class Details","There is no class found!");
            finish();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(classDetails.this.getApplicationContext(),AddClass.class);
                //send data to addClass activity intent.putExtra("key", value);
                intent.putExtra("class",tmp);
                startActivityForResult(intent,IS_DELETED_REQUEST);
            }
        });

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.classContainer);
        //TODO add different colors per class
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rl.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.map_gradient_blue));
            //TODO find a solution for below jely bean
        }

        //else{rl.setBackgroundColor(); "#553959d6"

        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        ctl.setTitle(tmp.name);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        notification=(ToggleButton) findViewById(R.id.notificationsToggle);
        notification.setChecked(tmp.notify);
        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tmp.notify=isChecked;
                Database db = new Database(getApplicationContext());
                db.editClass(tmp);
                tmp.setAlarms(getApplicationContext());
            }
        });

        caldroidFragment = new CaldroidFragment();

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            Log.d("Instance",savedInstanceState.toString());
            caldroidFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE");
        }

        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);
            //args.putInt(CaldroidFragment.MIN_DATE,(int)tmp.Term.start.getTime());
            //args.putInt(CaldroidFragment.MAX_DATE,(int)tmp.Term.end.getTime());
            //TO-DO add theme to fix gray bg
            caldroidFragment.setArguments(args);
        }

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar1, caldroidFragment);
        t.commit();

        Drawable temp = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_corner, null);

        attColor = temp.getConstantState().newDrawable();
        attColor.setColorFilter(Color.parseColor("#FF009933"), PorterDuff.Mode.MULTIPLY );
        attColor.mutate();//Attended color

        notAttColor = temp.getConstantState().newDrawable();
        notAttColor.setColorFilter(Color.parseColor("#FFcc3300"), PorterDuff.Mode.MULTIPLY );
        notAttColor.mutate();//Not Attended color

        noClassColor = temp.getConstantState().newDrawable();
        noClassColor.setColorFilter(Color.parseColor("#FF0066cc"), PorterDuff.Mode.MULTIPLY );
        noClassColor.mutate();//No class color


        Database db = new Database(getApplicationContext());
        att = db.getAtt(tmp);
        Log.d("Database",att.toString());

        HashMap<Date,Drawable> data = new HashMap<>();
        outClass = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for(Map.Entry<Date, Integer> entry : att.entrySet()){
            cal.setTime(entry.getKey());
            if(tmp.checkDay(cal)) {
                Log.d("AttPer","Cheking:"+cal.get(Calendar.DAY_OF_MONTH)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.YEAR));
                //TODO birden fazla saat (Json objesi olabilir) onclick ile gösterilebilir (dialog?)
                if (entry.getValue() == -1) {
                    notAtt = notAtt + 1;
                    data.put(entry.getKey(), notAttColor);
                } else if (entry.getValue() == 1) {
                    attended = attended + 1;
                    data.put(entry.getKey(), attColor);
                } else if (entry.getValue() == 0) {
                    data.put(entry.getKey(), noClassColor);
                } else {
                    Log.e("Attendence", "Error at " + entry.getKey().toString());
                }
            }
            else{
                outClass.add(entry.getKey());
            }
        }

        Bundle args = new Bundle();
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);

        caldroidFragment.setDisableDates(outClass);
        caldroidFragment.setBackgroundDrawableForDates(data);
        caldroidFragment.setThemeResource(R.style.CaldroidDefaultArrowButton);
        caldroidFragment.setArguments(args);


        c = (CircularProgressBar) findViewById(R.id.circularprogressbar);
        c.setSubTitle(getResources().getString(R.string.attended));

        int percentage;

        if(notAtt!=0) {
            percentage=((attended*100)/(notAtt+attended));
        }
        else{
            percentage=100;
        }

        Log.d("Attandence",Integer.toString(percentage)+"="+Integer.toString(attended)+"/"+Integer.toString(attended+notAtt));

        c.animateProgressTo(0, (int)percentage, new ProgressAnimationListener() {

            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationProgress(int progress) {
                c.setProgress(progress);
                c.setTitle("%"+progress);
            }

            @Override
            public void onAnimationFinish() {
            }
        });

        c.setProgress((int)percentage);
        c.setTitle("%"+percentage);

        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                /*Toast.makeText(getApplicationContext(), formatter.format(date),
                        Toast.LENGTH_SHORT).show();
                */
                Snackbar.make(view, getResources().getString(R.string.calendarLongPress) , Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }

            @Override
            public void onChangeMonth(int month, int year) {
                /*String text = "month: " + month + " year: " + year;
                Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onLongClickDate(Date date, View view) {

                String status;
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"ClassDetailsActivity");
                mFirebaseAnalytics.logEvent("changed_att", bundle);

                if(!tmp.checkDay(cal)){
                    Snackbar.make(view,getResources().getString(R.string.noClassBefore)+" "+date.toString()+" "+getResources().getString(R.string.noClassAfter), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else if(att.get(date)!=null){
                    if (att.get(date) == -1) {
                        //Not attended
                        status=getResources().getString(R.string.noClass);
                        att.put(date,0);

                        caldroidFragment.setBackgroundDrawableForDate(noClassColor,date);
                        caldroidFragment.refreshView();

                        if(notAtt!=0){
                            notAtt=notAtt-2;
                        }

                        Log.d("Attandence","Attendent: "+Integer.toString(attended)+"-Not attended: "+Integer.toString(notAtt));

                    } else if (att.get(date) == 0) {
                        //No class or holiday
                        status=getResources().getString(R.string.attended);
                        att.put(date,1);

                        caldroidFragment.setBackgroundDrawableForDate(attColor,date);
                        caldroidFragment.refreshView();

                        attended=attended+2;

                        Log.d("Attandence","Attendent: "+Integer.toString(attended)+"-Not attended: "+Integer.toString(notAtt));

                    } else if (att.get(date) == 1) {
                        //Attended
                        status=getResources().getString(R.string.notAttended);
                        att.put(date,-1);

                        caldroidFragment.setBackgroundDrawableForDate(notAttColor,date);
                        caldroidFragment.refreshView();

                        notAtt=notAtt+2;
                        if(attended!=0){
                            attended=attended-2;
                        }

                        Log.d("Attandence","Attendent: "+Integer.toString(attended)+"-Not attended: "+Integer.toString(notAtt));
                    } else {
                        status=getResources().getString(R.string.error);
                        Log.d("Attandence", "Something wrong at " + date.toString());
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    Snackbar.make(view, calendar.get(Calendar.DAY_OF_MONTH)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.YEAR) + " " + getResources().getString(R.string.markedAs)+" "+ status, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    int percentage;

                    if((notAtt+attended)!=0) {
                        percentage=((attended*100)/(notAtt+attended));
                        Log.d("Attandence",Integer.toString(percentage)+"="+Integer.toString(attended)+"/"+Integer.toString(attended+notAtt));
                    }
                    else{
                        percentage=100;
                    }
                    c.animateProgressTo(c.getProgress(), (int)percentage, new ProgressAnimationListener() {

                        @Override
                        public void onAnimationStart() {
                        }

                        @Override
                        public void onAnimationProgress(int progress) {
                            c.setProgress(progress);
                            c.setTitle("%"+progress);
                        }

                        @Override
                        public void onAnimationFinish() {
                        }
                    });
                }
                else {
                    Snackbar.make(view, date.toString() +" "+ getResources().getString(R.string.notTerm) +" "+ tmp.Term.name, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void onCaldroidViewCreated() {
                /*Toast.makeText(getApplicationContext(),
                        "Caldroid view is created",
                        Toast.LENGTH_SHORT).show();*/
            }

        };
        caldroidFragment.setCaldroidListener(listener);
        //DONT FORGET REFRESH
        caldroidFragment.refreshView();
    }

    @Override
    public void onSaveInstanceState (Bundle outState){
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        if (caldroidFragment != null) {
            caldroidFragment.clearBackgroundDrawableForDates(outClass);
            caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }
        if (tmp.Term != null) {
            Database db = new Database(getApplicationContext());
            db.uptAtt(att,tmp);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("Details","changed ui");
        ((TextView)findViewById(R.id.s_nameTxt)).setText(tmp.s_name);
        ((TextView)findViewById(R.id.crn)).setText(tmp.code);
        ((TextView)findViewById(R.id.teacher)).setText(tmp.teacher);
        ((TextView)findViewById(R.id.classRoom)).setText(tmp.classLocation);
        if(tmp.Term==null){
            ((TextView)findViewById(R.id.term)).setText("No Term Found");
        }
        else{
            ((TextView)findViewById(R.id.term)).setText(tmp.Term.name);
        }
        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(tmp.name);

        Database db = new Database(getApplicationContext());
        att = db.getAtt(tmp);
        Log.d("Database",att.toString());

        HashMap<Date,Drawable> data = new HashMap<>();
        outClass = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for(Map.Entry<Date, Integer> entry : att.entrySet()){
            cal.setTime(entry.getKey());
            if(tmp.checkDay(cal)) {
                //TODO birden fazla saat (Json objesi olabilir) onclick ile gösterilebilir (dialog?)
                if (entry.getValue() == -1) {
                    notAtt = notAtt + 1;
                    data.put(entry.getKey(), notAttColor);
                } else if (entry.getValue() == 1) {
                    attended = attended + 1;
                    data.put(entry.getKey(), attColor);
                } else if (entry.getValue() == 0) {
                    data.put(entry.getKey(), noClassColor);
                } else {
                    Log.d("Attendnce", "Error at " + entry.getKey().toString());
                }
            }
            else{
                outClass.add(entry.getKey());
            }
        }

        caldroidFragment.clearDisableDates();
        caldroidFragment.setDisableDates(outClass);
        caldroidFragment.setBackgroundDrawableForDates(data);
        caldroidFragment.refreshView();

        int percentage;

        if(notAtt!=0) {
            percentage=((attended*100)/(notAtt+attended));
        }
        else{
            percentage=100;
        }

        Log.d("Attandence",Integer.toString(percentage)+"="+Integer.toString(attended)+"/"+Integer.toString(attended+notAtt));

        c.animateProgressTo(0, (int)percentage, new ProgressAnimationListener() {

            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationProgress(int progress) {
                c.setProgress(progress);
                c.setTitle("%"+progress);
            }

            @Override
            public void onAnimationFinish() {
            }
        });

        c.setProgress((int)percentage);
        c.setTitle("%"+percentage);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (tmp.Term != null) {
            Database db = new Database(getApplicationContext());
            db.uptAtt(att,tmp);
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.classLocation);
        if(mapFragment!=null)
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        location = new LatLng(tmp.geoFence.Lat,tmp.geoFence.Long);
        Log.d("Location",Double.toString(tmp.geoFence.Lat)+Double.toString(tmp.geoFence.Long));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        MarkerOptions a = new MarkerOptions().position(location);
        Marker m = googleMap.addMarker(a);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        }
        //Toast.makeText(getApplicationContext(), "Long:" + String.valueOf(Long) + "Lat:" + String.valueOf(Lat), Toast.LENGTH_LONG).show();

        UiSettings fixed = googleMap.getUiSettings();
        fixed.setScrollGesturesEnabled(false);
        fixed.setZoomGesturesEnabled(false);
        fixed.setRotateGesturesEnabled(false);
        fixed.setTiltGesturesEnabled(false);
        fixed.setMapToolbarEnabled(false);

        mMap=googleMap;

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng arg0) {
                //TODO open in google maps
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                                @Override
                                                public void onMapLongClick(LatLng latLng) {

                                                }
                                            }
        );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                mMap.clear();
                location = new LatLng(tmp.geoFence.Lat,tmp.geoFence.Long);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
                MarkerOptions a = new MarkerOptions().position(location);
                Marker m = mMap.addMarker(a);
            }
        }
        else if(requestCode==IS_DELETED_REQUEST && data!=null){
            final Class c = (Class) data.getExtras().getSerializable("class");
            if(data.getExtras().getBoolean("isDeleted")){
                finish();
            }
            else if (c!=null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (c.Term != null) {
                            Database db = new Database(getApplicationContext());
                            db.editClass(c);
                            c.setAlarms(getApplicationContext());
                        }
                    }
                }).start();
                tmp=c;
                /*
                Intent intent = new Intent(AddClass.this, timedClassService.class);
                PendingIntent pintent = PendingIntent.getService(AddClass.this, 0, intent,0);
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY*7 , pintent);*/
            }
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("PlayServices", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("PlayServices", "Connection suspended");
        mGoogleApiClient.connect();
    }
}
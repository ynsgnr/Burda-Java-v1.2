package opus.auctor;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import opus.auctor.viewElements.NonScrollListView;


public class AddClass extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {

    EditText s_name;
    EditText name;
    EditText classCode;
    EditText teacher;
    EditText notes;
    EditText classLocation;
    Calendar cal;
    Button timeButton0;
    Button timeButton1;
    Button addTime;
    int time0h;
    int time0m;
    int time1h;
    int time1m;
    int day;
    FloatingActionButton done;
    Spinner weekDay;
    Spinner terms;
    Button addTerm;
    TextView noTerm;
    LatLng location=null;
    FloatingActionButton del;

    ArrayList<term> termList;
    term T=new term();
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    int MY_PERMISSIONS_LOCATION;
    GoogleMap mMap;
    public double Long;
    public double Lat;
    SupportMapFragment mapFragment;
    Class tmp;
    //Boolean isMapBig=false;

    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        ((TextView)findViewById(R.id.courseCode)).requestFocus();//Change focus to prevent keyboard poping up
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent intent = getIntent();
        tmp = (Class) intent.getSerializableExtra("class");

        //Class data input
        s_name = (EditText) findViewById(R.id.s_name);//s_name stands for short name
        name = (EditText) findViewById(R.id.courseName);

        terms = (Spinner) findViewById(R.id.terms);
        addTerm = (Button) findViewById(R.id.addTerm);
        noTerm = (TextView) findViewById(R.id.noTermTxt);


        Database db = new Database(getApplicationContext());
        termList=db.terms();
        if (termList.size()==0){
            addTerm.setVisibility(View.VISIBLE);
            noTerm.setVisibility(View.VISIBLE);
            terms.setVisibility(View.INVISIBLE);
            addTerm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AddClass.this.getApplicationContext(),AddTerm.class);
                    AddClass.this.startActivity(intent);
                }
            });
        }
        else {
            addTerm.setVisibility(View.INVISIBLE);
            noTerm.setVisibility(View.INVISIBLE);
            terms.setVisibility(View.VISIBLE);

            ArrayAdapter <term> termAdapter = new ArrayAdapter<term>(this,android.R.layout.simple_spinner_item , termList);
            termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            terms.setAdapter(termAdapter);
            terms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    T=termList.get(position);
                    //TO-DO add ids
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        T.id=-1;

        //other datas
        classCode = (EditText) findViewById(R.id.crn);
        teacher = (EditText) findViewById(R.id.teacher);
        notes = (EditText) findViewById(R.id.notes);
        classLocation=(EditText) findViewById(R.id.classRoom);

        location = new LatLng(41.0761903, 29.0053389);

        del = (FloatingActionButton) findViewById(R.id.deleteClass);

        final ArrayList<Class.classTime> classTimes = new ArrayList<>();

        if(tmp!=null){
            Log.d("AddClass","Found class:"+tmp.s_name+", edit mode enabled");
            s_name.setText(tmp.s_name);
            name.setText(tmp.name);
            classLocation.setText(tmp.classLocation);
            if(tmp.classTimes!=null){
                //Populate classTimes
                Log.d("AddClass","Found classs times, populating listview");
                for(Map.Entry<Integer, Class.classTime> entry : tmp.classTimes.entrySet()) {
                    Class.classTime t = entry.getValue();
                    Log.d("AddClass","Added to listview: "+t.day+":"+t.startTime.getString()+"-"+t.endTime.getString());
                    classTimes.add(t);
                }
            }

            if(tmp.geoFence!=null)
                location = new LatLng(tmp.geoFence.Lat,tmp.geoFence.Long);

            classCode.setText(tmp.code);
            teacher.setText(tmp.teacher);
            notes.setText(tmp.notes);

            del.setVisibility(View.VISIBLE);
            del.bringToFront();
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddClass.this);
                    builder.setTitle(getResources().getString(R.string.delClass));
                    builder.setMessage(getResources().getString(R.string.delClassLong));

                    builder.setNegativeButton(getResources().getString(R.string.cancel2), new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    builder.setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //deleting class
                            Log.d("AddClass","Deleting class of "+tmp.s_name);
                            Database db = new Database(getApplicationContext());
                            db.deleteClass(tmp);

                            Intent result = new Intent();
                            result.putExtra("isDeleted", true);
                            setResult(Activity.RESULT_OK, result);

                            tmp.delAlarms(getApplicationContext());
                            finish();
                        }
                    });
                    builder.show();
                }
            });
        }

        NonScrollListView list = (NonScrollListView)findViewById(R.id.classTimes);
        list.setVisibility(View.VISIBLE);
        final timeAdapter times = new timeAdapter(this,classTimes);
        list.setAdapter(times);
        list.setStackFromBottom(true);

        Log.d("AddClass","Adding another time for class");

        if(tmp==null)
        {
            Class.classTime ct = new Class().new classTime();
            ct.endTime=null;
            ct.startTime=null;
            ct.day=Calendar.MONDAY;
            times.addItem(ct);
        }

        addTime = (Button) findViewById(R.id.addClassTime);
        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("AddClass","Adding another time for class");
                if(tmp==null)
                    tmp=new Class();
                Class.classTime ct = tmp.new classTime();
                ct.endTime=null;
                ct.startTime=null;
                ct.day=Calendar.MONDAY;
                times.addItem(ct);
            }
        });


        Lat = location.latitude;
        Long = location.longitude;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //finished

        done = (FloatingActionButton) findViewById(R.id.addClass_done);
        done.bringToFront();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View View) {

                if(T.id==-1)
                {
                    Snackbar.make(View ,getResources().getString(R.string.chooseTerm) , Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                    //Toast.makeText(getApplicationContext(),"Please Choose Term",Toast.LENGTH_LONG).show();
                else {
                    Database db = new Database(getApplicationContext());

                    final Intent result = new Intent();

                    ArrayList<Class.classTime> timeList=times.getItems();
                    Log.d("Add Class",timeList.toString());
                    int counter=0;
                    for(int i=0;i<timeList.size();i++){
                        if(timeList.get(i).startTime!=null || timeList.get(i).endTime!=null){
                            counter++;
                        }
                    }
                    if(counter<1){
                        Snackbar.make(View ,getResources().getString(R.string.chooseTime) , Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else {
                        if(intent.getSerializableExtra("class")==null){

                            //Add new class
                            Log.d("Add Class","Adding new class");
                            Class c = new Class();
                            c.Term = T;
                            c.s_name = s_name.getText().toString();
                            c.name = name.getText().toString();

                            c.setClassTimes(timeList);

                            c.code = classCode.getText().toString();
                            //if (tmp.code=="\0") tmp.code=tmp.s_name+time0h+time0m+time1h+time1m+day;
                            c.teacher = teacher.getText().toString();
                            c.notes = notes.getText().toString();
                            c.classLocation = classLocation.getText().toString();
                            Log.d("geofence", "create geofence");
                            //Create geofence
                            c.geoFence.Long = location.longitude;
                            c.geoFence.Lat = location.latitude;
                            c.geoFence.fenceRadius = 10; //in meters
                            c=db.addClass(c);
                            c.setAlarms(getApplicationContext());

                            result.putExtra("isDeleted", false);
                            setResult(Activity.RESULT_OK, result);

                            finish();
                        }
                        else {
                            //Edit class

                            //Add changing terms
                            tmp.s_name = s_name.getText().toString();
                            tmp.name = name.getText().toString();

                            tmp.setClassTimes(timeList);

                            tmp.code = classCode.getText().toString();
                            //if (tmp.code=="\0") tmp.code=tmp.s_name+time0h+time0m+time1h+time1m+day;
                            tmp.teacher = teacher.getText().toString();
                            tmp.notes = notes.getText().toString();
                            tmp.classLocation = classLocation.getText().toString();

                            tmp.geoFence.Long = location.longitude;
                            tmp.geoFence.Lat = location.latitude;
                            tmp.geoFence.fenceRadius = 10; //in meters

                            //tmp=db.editClass(tmp);
                            //tmp.setAlarms(getApplicationContext());
                            //Editing done in classDetails(in a thread)

                            if(tmp.Term!=null && tmp.Term.id!=-1 && tmp.Term.id!=T.id && T.id!=-1){
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddClass.this);
                                builder.setTitle(getResources().getString(R.string.losingAttData));
                                builder.setMessage(getResources().getString(R.string.changeClassLostData));

                                builder.setNegativeButton(getResources().getString(R.string.cancel2), new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });

                                builder.setPositiveButton(getResources().getString(R.string.continueTxt), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //termChange değiştir
                                        tmp.Term=T;
                                        result.putExtra("class",tmp);
                                        result.putExtra("isDeleted", false);
                                        setResult(Activity.RESULT_OK, result);
                                        finish();
                                    }
                                });
                                builder.show();
                            }
                            else{
                                tmp.Term=T;
                                result.putExtra("class",tmp);
                                result.putExtra("isDeleted", false);
                                setResult(Activity.RESULT_OK, result);
                                finish();
                            }
                        }
                    }
                }
        }});


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume(){
        super.onResume();
        Database db = new Database(getApplicationContext());
        termList=db.terms();
        if (termList.size()==0){
            addTerm.setVisibility(View.VISIBLE);
            noTerm.setVisibility(View.VISIBLE);
            terms.setVisibility(View.INVISIBLE);
            addTerm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AddClass.this.getApplicationContext(),AddTerm.class);
                    AddClass.this.startActivity(intent);
                }
            });
        }
        else {
            addTerm.setVisibility(View.INVISIBLE);
            noTerm.setVisibility(View.INVISIBLE);
            terms.setVisibility(View.VISIBLE);

            ArrayAdapter <term> termAdapter = new ArrayAdapter<term>(this,android.R.layout.simple_spinner_item , termList);
            termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            terms.setAdapter(termAdapter);
            terms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    T=termList.get(position);
                    //TO-DO add ids
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (tmp!=null && tmp.Term != null) {
                int spinnerId = 0;
                Log.d("Terms Spinner", "Starting:" + terms.getAdapter().getCount());
                for (int i = 0; i < terms.getAdapter().getCount(); i++) {
                    //Convert to while with boolean
                    Log.d("Terms Spinner", "In for:" + i);
                    if (termList.get(i).id == tmp.Term.id) {
                        Log.d("Terms Spinner", "Found location:" + i);
                        spinnerId = i;
                    }
                }
                terms.setSelection(spinnerId);
            }
        }

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

        Intent result = new Intent();
        result.putExtra("isDeleted", false);
        setResult(Activity.RESULT_OK, result);
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.classLocation);
        mapFragment.getMapAsync(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(getApplicationContext(), "Please Allow app to use location", Toast.LENGTH_LONG).show();
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_LOCATION);
                // MY_PERMISSIONS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null && location==null) {
                Lat = mLastLocation.getLatitude();
                Long = mLastLocation.getLongitude();
                location = new LatLng(Lat, Long);
            }
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap=googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        MarkerOptions a = new MarkerOptions().position(location);
        Marker m = mMap.addMarker(a);
        /*if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        }*/
        //Toast.makeText(getApplicationContext(), "Long:" + String.valueOf(Long) + "Lat:" + String.valueOf(Lat), Toast.LENGTH_LONG).show();

        final UiSettings fixed = mMap.getUiSettings();
        fixed.setAllGesturesEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(AddClass.this.getApplicationContext(),MapsActivity.class);
                //send data to addClass activity intent.putExtra("key", value);
                intent.putExtra("Lat",Lat);
                intent.putExtra("Long",Long);
                startActivityForResult(intent,PLACE_PICKER_REQUEST);
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                                @Override
                                                public void onMapLongClick(LatLng latLng) {
                                                    Intent intent = new Intent(AddClass.this.getApplicationContext(),MapsActivity.class);
                                                    //send data to addClass activity intent.putExtra("key", value);
                                                    intent.putExtra("Lat",Lat);
                                                    intent.putExtra("Long",Long);
                                                    startActivityForResult(intent,PLACE_PICKER_REQUEST);
                                                }
                                            }
        );

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Long = data.getExtras().getDouble("Long",0);
                Lat = data.getExtras().getDouble("Lat",0);
                location = new LatLng(Lat,Long);
                Log.d("location",Long+"-"+Lat);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
                MarkerOptions a = new MarkerOptions().position(location);
                mMap.clear();
                Marker m = mMap.addMarker(a);
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


    private boolean checkTimes(int time0h, int time0m, int time1h, int time1m) {
        int time0;
        int time1;

        time0 = time0h * 3600 + time0m * 60;
        time1 = time1h * 3600 + time1m * 60;
        if (time0 < time1)
            return true;
        else {
            Snackbar.make(findViewById(R.id.addClass) ,getResources().getString(R.string.checkTime), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return false;
        }
    }

    public class timeAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<Class.classTime> objects;

        private class ViewHolder {
            Spinner day;
            Button startTime;
            Button endTime;
            Button delete;
        }


        public timeAdapter(Context context, ArrayList<Class.classTime> objects) {
            inflater = LayoutInflater.from(context);
            this.objects = objects;
        }

        public void addItem (Class.classTime ct){
            objects.add(ct);
            notifyDataSetChanged();
        }

        public ArrayList<Class.classTime> getItems (){
            return objects;
        }

        public int getCount() {
            return objects.size();
        }

        public Class.classTime getItem(int position) {
            return objects.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean areAllItemsEnabled()
        {
            return true;
        }

        @Override
        public boolean isEnabled(int arg0)
        {
            return true;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d("Add Class","New object in listview: "+position);
            ViewHolder holder = null;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.add_class_time, null);
                holder.day = (Spinner) convertView.findViewById(R.id.weekDay);
                holder.startTime = (Button) convertView.findViewById(R.id.timebutton0);
                holder.endTime = (Button) convertView.findViewById(R.id.timebutton1);
                holder.delete = (Button) convertView.findViewById(R.id.delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Class.classTime t = objects.get(position);

            //weekDay chooser
            weekDay = holder.day;
            ArrayAdapter<CharSequence> weekDays = ArrayAdapter.createFromResource(getBaseContext(), R.array.weekDays, android.R.layout.simple_spinner_item);
            weekDays.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            weekDay.setAdapter(weekDays);
            weekDay.setSelection(0);
            switch (t.day){
                case Calendar.MONDAY:
                    holder.day.setSelection(0);
                    break;
                case Calendar.TUESDAY:
                    holder.day.setSelection(1);
                    break;
                case Calendar.WEDNESDAY:
                    holder.day.setSelection(2);
                    break;
                case Calendar.THURSDAY:
                    holder.day.setSelection(3);
                    break;
                case Calendar.FRIDAY:
                    holder.day.setSelection(4);
                    break;
                case Calendar.SATURDAY:
                    holder.day.setSelection(5);
                    break;
                case Calendar.SUNDAY:
                    holder.day.setSelection(6);
                    break;
            }
            weekDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapter, View v, int spinnerPos, long id) {
                    switch (spinnerPos){
                        case 0:
                            day=Calendar.MONDAY;
                            break;
                        case 1:
                            day=Calendar.TUESDAY;
                            break;
                        case 2:
                            day=Calendar.WEDNESDAY;
                            break;
                        case 3:
                            day=Calendar.THURSDAY;
                            break;
                        case 4:
                            day=Calendar.FRIDAY;
                            break;
                        case 5:
                            day=Calendar.SATURDAY;
                            break;
                        case 6:
                            day=Calendar.SUNDAY;
                            break;
                    }
                    objects.get(position).setDate(day);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    Toast.makeText(getApplicationContext(), "Please choose a day", Toast.LENGTH_SHORT).show();
                }
            });

            timeButton0=holder.startTime;
            timeButton1=holder.endTime;
            cal=Calendar.getInstance();
            if(objects.get(position).startTime==null || objects.get(position).endTime==null)
            {
                time0h=cal.get(Calendar.HOUR_OF_DAY);
                time0m=cal.get(Calendar.MINUTE);
                time1h=time0h;
                time1m=time0m+10;
                Log.d("Add Class","Time null");
                timeButton0.setText(getResources().getString(R.string.startTime));
                timeButton1.setText(getResources().getString(R.string.finishTime));
            }
            else {
                time0h=t.startTime.hour;
                time0m=t.startTime.minute;
                time1h=t.endTime.hour;
                time1m=t.endTime.minute;
                holder.startTime.setText(t.startTime.getString());
                Log.d("Add Class","Start time not null:"+t.startTime.getString());
                holder.endTime.setText(t.endTime.getString());
                Log.d("Add Class","End time not null:"+t.endTime.getString());
            }
            timeButton0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View View) {
                    TimePickerDialog tp1 = new TimePickerDialog(AddClass.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if(t.startTime==null || t.endTime==null) {
                                t.endTime = new Class().new Time();
                                t.startTime = new Class().new Time();
                                t.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 23, 59);
                            }
                            Log.d("Add Class","Time found is:"+t.startTime.hour+"-"+t.startTime.minute);
                            if (checkTimes(hourOfDay, minute, t.endTime.hour, t.endTime.minute)) {
                                t.setStartTime(hourOfDay,minute);
                                Log.d("AddClass","Setting start button to"+t.startTime.getString());
                                timeButton0.setText(t.startTime.getString());
                                objects.get(position).setStartTime(hourOfDay,minute);
                                notifyDataSetChanged();
                            }
                        }
                    }, time0h, time0m, true);
                    tp1.show();
                }
            });

            timeButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View View) {
                    TimePickerDialog tp1 = new TimePickerDialog(AddClass.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if(t.startTime==null || t.endTime==null) {
                                t.endTime = new Class().new Time();
                                t.startTime = new Class().new Time();
                                t.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 23, 59);
                            }
                            Log.d("Add Class","Time found is:"+t.startTime.hour+"-"+t.startTime.minute);
                            if (checkTimes(t.startTime.hour, t.startTime.minute, hourOfDay, minute)) {
                                t.setEndTime(hourOfDay,minute);
                                Log.d("AddClass","Setting start button to"+t.endTime.getString());
                                timeButton1.setText(t.endTime.getString());
                                objects.get(position).setEndTime(hourOfDay,minute);
                                notifyDataSetChanged();
                            }
                        }
                    }, time1h, time1m, true);
                    tp1.show();
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    objects.remove(position);
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }
}

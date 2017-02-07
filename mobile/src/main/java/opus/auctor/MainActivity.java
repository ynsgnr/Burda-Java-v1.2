package opus.auctor;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.database.sqlite.*;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import static android.R.attr.fillAfter;
import static android.R.attr.fillEnabled;
import static android.R.attr.icon;

public class MainActivity extends AppCompatActivity {

    //variable initializations
    FloatingActionButton fab;
    FloatingActionButton addClass;
    FloatingActionButton addTerm;

    TextView addClassTxt;
    TextView addTermTxt;

    private boolean FAB_Status;

    ArrayList<Class> classes;

    //Remove later
    ListView list;

    public ArrayList<Geofence> mGeofenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        //Check if first start
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Create new Pref File
        if (settings.getBoolean("first_launch", true)) {
            //Create SQlite db



            settings.edit().putBoolean("first_launch", false).commit();//usage of settings
        }*/

        mGeofenceList = new ArrayList<Geofence>();




        //find views
        fab = (FloatingActionButton) findViewById(R.id.fab);
        addClass = (FloatingActionButton) findViewById(R.id.addClass);
        addClassTxt = (TextView) findViewById(R.id.addClassTxt);
        addTerm = (FloatingActionButton) findViewById(R.id.addTerm);
        addTermTxt = (TextView) findViewById(R.id.addTermTxt);
        FAB_Status = false;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FAB_Status) {
                    //Close FAB menu
                    hideFAB();
                    FAB_Status = false;
                } else {
                    //Display FAB menu
                    expandFAB();
                    FAB_Status = true;
                }
                addClass.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    hideFAB();
                                                    FAB_Status=false;
                                                    Intent intent = new Intent(MainActivity.this.getApplicationContext(), AddClass.class);
                                                    //send data to addClass activity intent.putExtra("key", value);
                                                    MainActivity.this.startActivity(intent);
                                                }
                                            }
                );
                addTerm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideFAB();
                        FAB_Status=false;
                        Intent intent = new Intent(MainActivity.this.getApplicationContext(),AddTerm.class);
                        MainActivity.this.startActivity(intent);
                    }
                });

            }
            }
        );
    }

    @Override
    public void onResume(){
        super.onResume();
        Database db = new Database(getApplicationContext());
        classes=db.Classes();
        if (classes.size()==0){
            //Database is empty
            ArrayList <Class> l = new ArrayList<>();
            ClassAdapter c = new ClassAdapter(getApplicationContext(),l);
            list = (ListView)findViewById(R.id.classesMonday);
            list.setAdapter(c);

            list = (ListView)findViewById(R.id.classesTuesday);
            list.setAdapter(c);

            list = (ListView)findViewById(R.id.classesWednesday);
            list.setAdapter(c);

            list = (ListView)findViewById(R.id.classesThursday);
            list.setAdapter(c);

            list = (ListView)findViewById(R.id.classesFriday);
            list.setAdapter(c);
        }
        else{
            //crate weekly table
            ArrayList<Class> mondayList = new ArrayList<Class>();
            ArrayList<Class> tuesdayList = new ArrayList<Class>();
            ArrayList<Class> wednesdayList = new ArrayList<Class>();
            ArrayList<Class> thursdayList = new ArrayList<Class>();
            ArrayList<Class> fridayList = new ArrayList<Class>();
            ArrayList<Class> saturdayList = new ArrayList<Class>();
            ArrayList<Class> sundayList = new ArrayList<Class>();


            Class c;

            for(int i=0;i<classes.size();i++)
            {
                c=classes.get(i);
                //c.setAlarms(getApplicationContext());
                for(Map.Entry<Integer, Class.classTime> entry : c.classTimes.entrySet()) {
                    c.primaryTimeId=entry.getKey();
                    c.setTime();
                    Log.d("Main Table","Found time: "+c.primaryTimeId+"-"+c.day+"-"+c.s_name+" Time:"+c.time0.getString()+"-"+c.time1.getString());
                    Class tmp = new Class(c);
                    switch (c.day) {
                        case Calendar.MONDAY:
                            mondayList.add(tmp);
                            break;
                        case Calendar.TUESDAY:
                            tuesdayList.add(tmp);
                            break;
                        case Calendar.WEDNESDAY:
                            wednesdayList.add(tmp);
                            break;
                        case Calendar.THURSDAY:
                            thursdayList.add(tmp);
                            break;
                        case Calendar.FRIDAY:
                            fridayList.add(tmp);
                            break;
                        case Calendar.SATURDAY:
                            saturdayList.add(tmp);
                            break;
                        case Calendar.SUNDAY:
                            sundayList.add(tmp);
                            break;
                    }
                }
            }

            //Order by time
            sort(mondayList);
            sort(tuesdayList);
            sort(wednesdayList);
            sort(thursdayList);
            sort(fridayList);
            sort(saturdayList);
            sort(sundayList);

            ClassAdapter tmp0 = new ClassAdapter (this, mondayList);
            list = (ListView)findViewById(R.id.classesMonday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp0);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(MainActivity.this.getApplicationContext(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    MainActivity.this.startActivity(intent);
                }
            });

            ClassAdapter tmp1 = new ClassAdapter (this, tuesdayList);
            list = (ListView)findViewById(R.id.classesTuesday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp1);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(MainActivity.this.getApplicationContext(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    MainActivity.this.startActivity(intent);
                }
            });

            ClassAdapter tmp2 = new ClassAdapter (this, wednesdayList);
            list = (ListView)findViewById(R.id.classesWednesday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp2);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(MainActivity.this.getApplicationContext(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    MainActivity.this.startActivity(intent);
                }
            });

            ClassAdapter tmp3 = new ClassAdapter (this, thursdayList);
            list = (ListView)findViewById(R.id.classesThursday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp3);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(MainActivity.this.getApplicationContext(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    MainActivity.this.startActivity(intent);
                }
            });

            ClassAdapter tmp4 = new ClassAdapter (this, fridayList);
            list = (ListView)findViewById(R.id.classesFriday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp4);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(MainActivity.this.getApplicationContext(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    MainActivity.this.startActivity(intent);
                }
            });
        }
    }

    public void sort(ArrayList<Class> classList){
        if (classList.size() == 0) {
            return;
        }
        quickSort(classList, 0, classList.size() - 1);
    }

    private void quickSort(ArrayList<Class> classList,int lowerIndex, int higherIndex) {

        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number, I am taking pivot as middle index number
        Class c = classList.get(lowerIndex+(higherIndex-lowerIndex)/2);
        int pivot = c.classTimes.get(c.primaryTimeId).startTime.getStamp();
        // Divide into two arrays
        while (i <= j) {
            /**
             * In each iteration, we will identify a number from left side which
             * is greater then the pivot value, and also we will identify a number
             * from right side which is less then the pivot value. Once the search
             * is done, then we exchange both numbers.
             */
            c=classList.get(i);
            while (c.classTimes.get(c.primaryTimeId).startTime.getStamp() < pivot) {
                Log.d("Sort",c.classTimes.get(c.primaryTimeId).startTime.getStamp()+"<"+pivot);
                i++;
                c=classList.get(i);
            }
            c=classList.get(j);
            while (c.classTimes.get(c.primaryTimeId).startTime.getStamp() > pivot) {
                Log.d("Sort",c.classTimes.get(c.primaryTimeId).startTime.getStamp()+">"+pivot);
                j--;
                c=classList.get(j);
            }
            if (i <= j) {
                Collections.swap(classList,i,j);
                Log.d("Sort","swaping:"+i+" with "+j);
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSort(classList,lowerIndex, j);
        if (i < higherIndex)
            quickSort(classList,i, higherIndex);
    }

    public class ClassAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<Class> objects;

        private class ViewHolder {
            TextView textView1;
            TextView textView2;
            TextView textView3;
            TextView textView4;
            TextView textView5;
            TextView textView6;
        }

        public ClassAdapter(Context context, ArrayList<Class> objects) {
            inflater = LayoutInflater.from(context);
            this.objects = objects;
        }

        public int getCount() {
            return objects.size();
        }

        public Class getItem(int position) {
            return objects.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item, null);
                holder.textView1 = (TextView) convertView.findViewById(R.id.shortName);
                holder.textView2 = (TextView) convertView.findViewById(R.id.className);
                holder.textView3 = (TextView) convertView.findViewById(R.id.teacher);
                holder.textView4 = (TextView) convertView.findViewById(R.id.classLocation);
                holder.textView5 = (TextView) convertView.findViewById(R.id.time1);
                holder.textView6 = (TextView) convertView.findViewById(R.id.time2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Class tmp = objects.get(position);
            holder.textView1.setText(tmp.s_name);
            holder.textView2.setText(tmp.name);
            holder.textView3.setText(tmp.teacher);
            holder.textView4.setText(tmp.classLocation);
            holder.textView5.setText(tmp.time0.getString());
            holder.textView6.setText(tmp.time1.getString());
            Log.d("Main Table","Primary time of "+tmp.s_name+" is:"+tmp.primaryTimeId+"-"+tmp.classTimes.get(tmp.primaryTimeId).startTime.getString());
            return convertView;
        }
    }

    private void expandFAB() {


        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.d("Screen","Height dp is"+Float.toString(dpHeight));
        ObjectAnimator objectAnimator= ObjectAnimator.ofFloat(fab, "rotation",0,45);
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClass, "translationY",(int)(-(80*displayMetrics.density)));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClass,"alpha",0,1);
        objectAnimator.setDuration(450);
        objectAnimator.start();
        addClass.setVisibility(View.VISIBLE);
        addClass.setClickable(true);

        objectAnimator= ObjectAnimator.ofFloat(addClassTxt, "translationY",(int)(-(80*displayMetrics.density)));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClassTxt,"alpha",0,1);
        objectAnimator.setDuration(450);
        objectAnimator.start();
        addClassTxt.setVisibility(View.VISIBLE);

        //ADD TERM

        objectAnimator= ObjectAnimator.ofFloat(addTerm, "translationY",(int)(-(150*displayMetrics.density)));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addTerm,"alpha",0,1);
        objectAnimator.setDuration(450);
        objectAnimator.start();
        addTerm.setVisibility(View.VISIBLE);
        addTerm.setClickable(true);

        objectAnimator= ObjectAnimator.ofFloat(addTermTxt, "translationY",(int)(-(150*displayMetrics.density)));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addTermTxt,"alpha",0,1);
        objectAnimator.setDuration(450);
        objectAnimator.start();
        addTermTxt.setVisibility(View.VISIBLE);
    }

    private void hideFAB() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        double multiplerY = (double) size.y/1280;
        if (multiplerY<1) multiplerY=1;
        Log.d("Animation","Screen size is:"+Double.toString(size.y)+"-"+Double.toString(multiplerY)+".");

        ObjectAnimator objectAnimator= ObjectAnimator.ofFloat(fab, "rotation",45,90);
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClassTxt, "translationY",(int)(multiplerY*50));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClassTxt,"alpha",1,0);
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClass, "translationY",(int)(multiplerY*50));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClass,"alpha",1,0);
        objectAnimator.setDuration(450)
                .addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        addClass.setVisibility(View.INVISIBLE);
                        addClass.setClickable(false);
                        addClassTxt.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
        objectAnimator.start();

        //ADDTERM

        objectAnimator= ObjectAnimator.ofFloat(addTermTxt, "translationY",(int)(multiplerY*100));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addTermTxt,"alpha",1,0);
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addTerm, "translationY",(int)(multiplerY*100));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addTerm,"alpha",1,0);
        objectAnimator.setDuration(450)
                .addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        addTerm.setVisibility(View.INVISIBLE);
                        addTerm.setClickable(false);
                        addTermTxt.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
        objectAnimator.start();

    }
}
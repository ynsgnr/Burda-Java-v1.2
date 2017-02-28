package opus.auctor;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.Geofence;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //variable initializations
    FloatingActionButton fab;
    FloatingActionButton addClass;
    FloatingActionButton addTerm;
    Fragment fragment;
    private FirebaseAnalytics mFirebaseAnalytics;

    TextView addClassTxt;
    TextView addTermTxt;

    private boolean FAB_Status;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"Opus.Auctor");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        fragment = getFragmentManager().findFragmentById( R.id.fragment_weekly_program );

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3646358189824390~7125508266");

        final AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("FF65CB156F114B4BCE365F6FC45A0BBC")
                //TODO : .setLocation(location)
                .build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
                                  @Override
                                  public void onAdLoaded() {
                                      fab.bringToFront();
                                  }
                              });

        /*
        //Check if first start
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        //Create new Pref File
        if (settings.getBoolean("first_launch", true)) {
            //Create SQlite db



            settings.edit().putBoolean("first_launch", false).commit();//usage of settings
        }*/

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

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"Clicked on FAB");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

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
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"AddClassActivityStart");
                                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
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
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"AddTermActivityStart");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
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
        //getFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();

    }

    private void expandFAB() {


        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.d("Screen","Height dp is"+Float.toString(dpHeight));
        ObjectAnimator objectAnimator= ObjectAnimator.ofFloat(fab, "rotation",0,45);
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClass, "translationY",(int)(-(120*displayMetrics.density)));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClass,"alpha",0,1);
        objectAnimator.setDuration(450);
        objectAnimator.start();
        addClass.setVisibility(View.VISIBLE);
        addClass.setClickable(true);

        objectAnimator= ObjectAnimator.ofFloat(addClassTxt, "translationY",(int)(-(120*displayMetrics.density)));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addClassTxt,"alpha",0,1);
        objectAnimator.setDuration(450);
        objectAnimator.start();
        addClassTxt.setVisibility(View.VISIBLE);

        //ADD TERM

        objectAnimator= ObjectAnimator.ofFloat(addTerm, "translationY",(int)(-(190*displayMetrics.density)));
        objectAnimator.setDuration(450);
        objectAnimator.start();

        objectAnimator= ObjectAnimator.ofFloat(addTerm,"alpha",0,1);
        objectAnimator.setDuration(450);
        objectAnimator.start();
        addTerm.setVisibility(View.VISIBLE);
        addTerm.setClickable(true);

        objectAnimator= ObjectAnimator.ofFloat(addTermTxt, "translationY",(int)(-(190*displayMetrics.density)));
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
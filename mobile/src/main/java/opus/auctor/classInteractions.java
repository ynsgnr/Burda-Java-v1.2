package opus.auctor;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.apache.commons.lang3.SerializationUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by yunus on 5.2.2017.
 */

public class classInteractions implements com.google.android.gms.location.LocationListener{

    String SERVICE="classInteractions";
    Class c;
    Context context;
    int LOCATIONTOLARANCE=50;
    GoogleApiClient client;
    PendingIntent pintent;
    AlarmManager alarm;
    private FirebaseAnalytics mFirebaseAnalytics;

    public void initilaze(Class tmp, Context con, GoogleApiClient cli){
        Log.d(SERVICE,"Created class interactions object");
        c=tmp;
        if(c!=null)
            c.setTime();
        context=con;
        client=cli;

    }

    public void setClass (Class tmp){
        c=tmp;
        if(c!=null)
            c.setTime();
    }

    public void setContext (Context c){
        context=c;
    }

    @Override
    public void onLocationChanged(Location location){
        Log.d(SERVICE,Double.toString(location.getLatitude())+"-"+Double.toString(location.getLongitude()));
        Calendar cal= Calendar.getInstance();
        if( c!=null){ /*check if c exists*/
            Database db = new Database(context);

            int i=db.getAtt(c,new Date(Calendar.getInstance().getTimeInMillis()));
            if(i==0)/*check if service runned before*/ {
                Log.d(SERVICE,"getAtt returned: "+i);
                if (
                    /*check if time is right*/
                        c.isClassTime()
                    /*check if date is in c's term*/
                                && cal.compareTo(c.Term.start.getCal()) >= 0
                                && cal.compareTo(c.Term.end.getCal()) <= 0
                        ) {
                    Location classLocation = new Location("Class Location");
                    classLocation.setLatitude(c.geoFence.Lat);
                    classLocation.setLongitude(c.geoFence.Long);
                    HashMap<Date, Integer> att = new HashMap<Date, Integer>();
                    Log.i(SERVICE, "Location:" + Double.toString(location.getLatitude()) + "-" + Double.toString(location.getLongitude()));
                    Log.i(SERVICE, "Class Location:" + c.geoFence.Lat + "-" + c.geoFence.Long);
                    Log.d(SERVICE,"Distance:"+location.distanceTo(classLocation)+" Tolarance:"+LOCATIONTOLARANCE);

                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

                    if (location.distanceTo(classLocation) <= LOCATIONTOLARANCE ) {
                        Log.i(SERVICE, "signed as attended " + c.s_name);
                        att.put(new Date(Calendar.getInstance().getTimeInMillis()), 1);
                        db.uptAtt(att, c);

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"TimedClassService");
                        mFirebaseAnalytics.logEvent("changed_att_auto", bundle);

                        c.setAlarm(context);

                        if(c.notify) {
                            Log.d("Timed Class Service", "Notification:" + c.notify);
                            sendInClassNotification();
                        }
                    } else {
                        Log.i(SERVICE, "not attanded waiting untill end of time:" + c.s_name);
                        if (c.time1.hour == cal.get(Calendar.HOUR_OF_DAY)
                                && c.time1.minute <= cal.get(Calendar.MINUTE)
                                && cal.get(Calendar.MINUTE)-10<=c.time1.minute
                                ) {
                            att.put(new Date(Calendar.getInstance().getTimeInMillis()), -1);
                            db.uptAtt(att, c);

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"TimedClassService");
                            mFirebaseAnalytics.logEvent("changed_att_auto", bundle);

                            c.setAlarm(context);

                            if(c.notify) {
                                Log.d("Timed Class Service","Notification:"+c.notify);
                                sendOutOfClassNotification();
                            }
                            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);

                        } else {
                            //Setup alarm for 10 mins later
                            Intent Tintnet = new Intent(context,timedClassService.class);

                            byte data [] = SerializationUtils.serialize(c);
                            Tintnet.putExtra("class",data);

                            //Tintnet.putExtra("class",c);
                            Tintnet.setData(Uri.parse("id:"+c.id));
                            Log.d(SERVICE,"Creating alarm for "+c.id+"-"+c.s_name+" for 10 min later.");
                            pintent = PendingIntent.getService(context, c.primaryTimeId/*request code*/, Tintnet, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);
                            alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            alarm.cancel(pintent);

                            Calendar classTime=Calendar.getInstance();
                            classTime.add(Calendar.MINUTE,10);

                            if (Integer.valueOf(Build.VERSION.SDK_INT) >= 19) {
                                Log.i("Add Class:", "Phone version is bigger then 19");
                                alarm.setExact(AlarmManager.RTC_WAKEUP, classTime.getTimeInMillis(), pintent);
                            } else {
                                Log.i("Add Class:", "Phone version is smaller then 19");
                                alarm.setRepeating(AlarmManager.RTC_WAKEUP, classTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pintent);
                            }
                        }
                    }
                }
            }
            else
                Log.d(SERVICE,"getAtt returned: "+i);
        }
        else Log.d(SERVICE,"Empty Class");
        if(client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            client.disconnect();
        }
    }

    public void sendNotification() {
        Log.i(SERVICE,"Sending unable to determinate Notification");

        Intent dbIntent = new Intent(context, databaseService.class);
        dbIntent.setData(Uri.parse("id-att:"+c.id+"-"+1));
        dbIntent.putExtra("class",c);
        dbIntent.putExtra("att",1);
        dbIntent.putExtra("date",Calendar.getInstance().getTimeInMillis());
        PendingIntent attended = PendingIntent.getService(context,
                c.id/*request code*/, dbIntent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);

        dbIntent = new Intent(context, databaseService.class);
        dbIntent.setData(Uri.parse("id-att:"+c.id+"-"+0));
        dbIntent.putExtra("class",c);
        dbIntent.putExtra("att",0);
        dbIntent.putExtra("date",Calendar.getInstance().getTimeInMillis());
        PendingIntent noClass = PendingIntent.getService(context,
                c.id*1000/*request code*/, dbIntent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);

        dbIntent = new Intent(context, databaseService.class);
        dbIntent.setData(Uri.parse("id-att:"+c.id+"-"+-1));
        dbIntent.putExtra("class",c);
        dbIntent.putExtra("att",-1);
        dbIntent.putExtra("date",Calendar.getInstance().getTimeInMillis());
        PendingIntent notAttended = PendingIntent.getService(context,
                -c.id/*request code*/, dbIntent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logoblue)
                        .setContentTitle(context.getResources().getString(R.string.noLocation))
                        .setContentText(context.getResources().getText(R.string.attQuestionBefore)+c.s_name+"-"+c.name+context.getResources().getString(R.string.attQuestionAfter))
                        .setVibrate(new long[] {500, 1000, 500})
                        .setLights(ContextCompat.getColor(context,R.color.logoColor),1000,600)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .addAction(R.drawable.attanded, context.getResources().getString(R.string.attended), attended)
                        .addAction(R.drawable.cancalled, context.getResources().getString(R.string.cancel), noClass)
                        .addAction(R.drawable.notattanded, context.getResources().getString(R.string.skip), notAttended)
                        .setAutoCancel(true);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, classDetails.class);
        resultIntent.setData(Uri.parse("id:"+c.id));
        resultIntent.putExtra("class",c);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(classDetails.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        c.id,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(c.id, mBuilder.build());
    }

    public void sendInClassNotification(){
        Log.i(SERVICE,"Sending in class Notification");



        Intent dbIntent = new Intent(context, databaseService.class);
        dbIntent.setData(Uri.parse("id-att:"+c.id+"-"+0));
        dbIntent.putExtra("class",c);
        dbIntent.putExtra("att",0);
        dbIntent.putExtra("date",Calendar.getInstance().getTimeInMillis());
        PendingIntent noClass = PendingIntent.getService(context,
                c.id*1000/*request code*/, dbIntent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);

        dbIntent = new Intent(context, databaseService.class);
        dbIntent.setData(Uri.parse("id-att:"+c.id+"-"+-1));
        dbIntent.putExtra("class",c);
        dbIntent.putExtra("att",-1);
        dbIntent.putExtra("date",Calendar.getInstance().getTimeInMillis());
        PendingIntent notAttended = PendingIntent.getService(context,
                -c.id/*request code*/, dbIntent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logoblue)
                        .setContentTitle(context.getResources().getString(R.string.notifyAttended))
                        .setContentText(context.getResources().getString(R.string.notifyAttendedBefore)+c.s_name+"-"+c.name+context.getResources().getString(R.string.notifyAttendedAfter))
                        .setVibrate(new long[] {500, 1000, 500})
                        .setLights(ContextCompat.getColor(context,R.color.logoColor),1000,600)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .addAction(R.drawable.cancalled, context.getResources().getString(R.string.cancel), noClass)
                        .addAction(R.drawable.notattanded, context.getResources().getString(R.string.skip), notAttended)
                        .setAutoCancel(true);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, classDetails.class);
        resultIntent.setData(Uri.parse("id:"+c.id));
        resultIntent.putExtra("class",c);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(classDetails.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        c.id,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(c.id, mBuilder.build());
    }

    public void sendOutOfClassNotification(){
        Log.i(SERVICE,"Sending out of class Notification");

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"BackgroundDataService");
        mFirebaseAnalytics.logEvent("changed_att", bundle);

        Intent dbIntent = new Intent(context, databaseService.class);
        dbIntent.setData(Uri.parse("id-att:"+c.id+"-"+0));
        dbIntent.putExtra("class",c);
        dbIntent.putExtra("att",0);
        dbIntent.putExtra("date",Calendar.getInstance().getTimeInMillis());
        PendingIntent noClass = PendingIntent.getService(context,
                c.id*1000/*request code*/, dbIntent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);

        dbIntent = new Intent(context, databaseService.class);
        dbIntent.setData(Uri.parse("id-att:"+c.id+"-"+1));
        dbIntent.putExtra("class",c);
        dbIntent.putExtra("att",1);
        dbIntent.putExtra("date",Calendar.getInstance().getTimeInMillis());
        PendingIntent attended = PendingIntent.getService(context,
                c.id/*request code*/, dbIntent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logoblue)
                        .setContentTitle(context.getResources().getString(R.string.notifySkipper))
                        .setContentText(context.getResources().getString(R.string.notifySkipperBefore)+c.s_name+"-"+c.name+context.getResources().getString(R.string.notifySkipperAfter))
                        .setVibrate(new long[] {500, 1000, 500})
                        .setLights(ContextCompat.getColor(context,R.color.logoColor),1000,600)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .addAction(R.drawable.attanded, context.getResources().getString(R.string.attended),attended)
                        .addAction(R.drawable.cancalled, context.getResources().getString(R.string.cancel), noClass)
                        .setAutoCancel(true);

// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, classDetails.class);
        resultIntent.setData(Uri.parse("id:"+c.id));
        resultIntent.putExtra("class",c);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(classDetails.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        c.id,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(c.id, mBuilder.build());
    }

}

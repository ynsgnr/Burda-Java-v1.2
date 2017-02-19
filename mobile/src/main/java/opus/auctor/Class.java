package opus.auctor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yunus on 15.07.2016.
 */
@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class Class implements Serializable {

    int id;
    int primaryTimeId=0;
    String s_name;
    String name;
    HashMap<Integer,classTime> classTimes = new HashMap<>();
    Time time0=new Time();
    Time time1=new Time();
    int day=Calendar.MONDAY;
    String classLocation;
    String teacher;
    String code;
    String notes;
    GeoFence geoFence=new GeoFence();
    String geofenceId;
    int socialId;
    term Term = new term();
    int color;
    boolean notify=true;
    int TIMETOLERANCE=5;
    String SERVICE="Check Class Time";
    private static final long serialVersionUID = 3842076227447073948L;

    public Class (Class c){
        this.id=c.id;
        this.primaryTimeId=c.primaryTimeId;
        this.s_name=c.s_name;
        this.name=c.name;
        this.classTimes = c.classTimes;
        this.time0=c.time0;
        this.time1=c.time1;
        this.day=c.day;
        this.classLocation=c.classLocation;
        this.teacher=c.teacher;
        this.code=c.code;
        this.notes=c.notes;
        this.geoFence=c.geoFence;
        this.geofenceId=c.geofenceId;
        this.socialId=c.socialId;
        this.Term =c.Term;
        this.color=c.color;
        this.notify=c.notify;
        this.setTime();
    }

    public Class (){}

    public class classTime implements Serializable {
        Time startTime = new Time();
        Time endTime = new Time();
        int day;
        public void setDate(int d){
            Log.d("Class Time","Setting day to "+d);
            this.day=d;
        }
        public void setTime(int h0,int m0,int h1,int m1){
            this.startTime.hour=h0;
            this.startTime.minute=m0;
            this.endTime.hour=h1;
            this.endTime.minute=m1;
        }
        public void setStartTime(int h0,int m0){
            Log.d("Class Time","Setting start time to "+h0+":"+m0);
            this.startTime.hour=h0;
            this.startTime.minute=m0;
        }
        public void setEndTime(int h1,int m1){
            Log.d("Class Time","Setting end time to "+h1+":"+m1);
            this.endTime.hour=h1;
            this.endTime.minute=m1;
        }
        public String getString(){
            return day+":"+startTime.getString()+"-"+endTime.getString();
        }
    }

    public class Time implements Serializable {
        int hour;
        int minute;
        public String getString(){
            return String.format("%02d", this.hour)+":"+String.format("%02d", this.minute);
        }
        public int getStamp(){
            return this.hour*60+this.minute;
        }

    }

    public class GeoFence implements Serializable {
        double Long;
        double Lat;
        double fenceRadius;

        String getString(){return "Location: "+Double.toString(Long)+"-"+Double.toString(Lat);}
    }

    public void setClassTimes (HashMap<Integer,classTime> classts){
        Log.d("Class Times","Clearing times");
        classTimes.clear();
        for(Map.Entry<Integer, Class.classTime> entry : classts.entrySet()) {
            Integer id = entry.getKey();
            Class.classTime t = entry.getValue();
            Log.d("Class Times","Adding: "+id+"-"+t.getString());
            classTimes.put(id,t);
        }
    }

    public void setClassTimes (ArrayList<classTime> classts){
        Log.d("Class Times","Clearing times");
        classTimes.clear();
        for (int i=0;i<classts.size();i++){
            if(classts.get(i).startTime!=null || classts.get(i).endTime!=null){
                Log.d("Class Times","Adding: "+id+"-"+classts.get(i).getString());
                classTimes.put(i,classts.get(i));
            }
        }
    }

    public void setTime(int id){
        Log.d("Class Times","Setting times to: "+id+"->"+day+":"+time0.getString()+"-"+time1.getString());
        this.time0=classTimes.get(id).startTime;
        this.time1=classTimes.get(id).endTime;
        this.day=classTimes.get(id).day;
    }

    public void setTime(){
        Log.d("Class Times","Setting time to id of "+primaryTimeId);
        setTime(primaryTimeId);
    }

    public void addClassTimes (HashMap<Integer,classTime> classts){
        for(Map.Entry<Integer, Class.classTime> entry : classts.entrySet()) {
            Integer id = entry.getKey();
            Class.classTime t = entry.getValue();
            classTimes.put(id,t);
        }
    }

    public boolean checkDay (Calendar cal){
        Log.d("Class Time","Day send is:"+cal.get(Calendar.DAY_OF_WEEK));
        for(Map.Entry<Integer, Class.classTime> entry : classTimes.entrySet()) {
            Integer id = entry.getKey();
            Class.classTime t = entry.getValue();
            Log.d("Class Times","Checking for day in:"+id+":"+t.getString());
            if(cal.get(Calendar.DAY_OF_WEEK)==t.day){
                Log.d("Class Time","Day Match");
                return true;
            }
        }
        return false;
    }

    public String getshortName(){
        return this.s_name;
    }

    public String getclassName(){
        return this.name;
    }

    public String getTeacher(){
        return this.teacher;
    }

    public String getClassLocation(){
        return classLocation;
    }

    public void setAlarms(Context context) {
        //Create alarmmaneger for time
        Calendar cal = Calendar.getInstance();

        Intent intent = new Intent(context, timedClassService.class);
        intent.setData(Uri.parse(Integer.toString(this.id)));

        Intent startIntent = new Intent(context,classStartService.class);
        startIntent.setData(Uri.parse(Integer.toString(this.id)));

        for (Map.Entry<Integer, Class.classTime> entry : this.classTimes.entrySet()) {
            this.primaryTimeId = entry.getKey();
            this.setTime();

           /* intent.putExtra("class", this);
            startIntent.putExtra("class", this); */

            byte data [] = SerializationUtils.serialize(this);
            intent.putExtra("class",data);
            startIntent.putExtra("class",data);

            Class.classTime t = entry.getValue();
            cal.set(Calendar.HOUR_OF_DAY, t.startTime.hour);
            cal.set(Calendar.MINUTE, t.startTime.minute);
            cal.set(Calendar.DAY_OF_WEEK, t.day);
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            if(cal.compareTo(Calendar.getInstance())<=0){
                Log.d("Set Alarms","Setting alarm for next week");
                cal.add(Calendar.DAY_OF_YEAR,7);
            }
            Log.d("Set Alarms","Setting time for "+this.s_name+"-"+this.name+" to "+cal.get(Calendar.DAY_OF_WEEK)+":"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.YEAR));
            PendingIntent pintent = PendingIntent.getService(context, (int) primaryTimeId/*request code*/, intent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);
            AlarmManager alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

            alarm.cancel(pintent);

            if (Integer.valueOf(Build.VERSION.SDK_INT) >= 19) {
                Log.i("Add Class:", "Phone version is bigger then 19");
                alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
            } else {
                Log.i("Add Class:", "Phone version is smaller then 19");
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pintent);
            }

            cal.add(Calendar.MINUTE,-10);

            PendingIntent startPintent = PendingIntent.getService(context, (int) primaryTimeId*1000/*request code*/, startIntent, PendingIntent.FLAG_UPDATE_CURRENT /*flag*/);
            alarm.cancel(startPintent);

            if (Integer.valueOf(Build.VERSION.SDK_INT) >= 19) {
                Log.i("Add Class:", "Phone version is bigger then 19");
                alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), startPintent);
            } else {
                Log.i("Add Class:", "Phone version is smaller then 19");
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, startPintent);
                //TODO check for old phones
            }

        }
    }

    public void setAlarm (Context context){
        Intent startIntent = new Intent(context,classStartService.class);
        startIntent.setData(Uri.parse("id:" + this.id));
        Calendar cal = Calendar.getInstance();


        Intent intent = new Intent(context, timedClassService.class);
        intent.setData(Uri.parse("id:" + this.id));

        this.setTime();

        startIntent.putExtra("class", this);
        intent.putExtra("class", this);
        cal.set(Calendar.HOUR_OF_DAY, time0.hour);
        cal.set(Calendar.MINUTE, time0.minute);
        cal.set(Calendar.DAY_OF_WEEK, day);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        if(cal.compareTo(Calendar.getInstance())<=0){
            Log.d("Set Alarms","Setting alarm for next week");
            cal.add(Calendar.DAY_OF_YEAR,7);
        }
        Log.d("Set Alarms","Setting one time for "+this.s_name+"-"+this.name+" to "+cal.get(Calendar.DAY_OF_WEEK)+":"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.YEAR));
        PendingIntent pintent = PendingIntent.getService(context, (int) primaryTimeId/*request code*/, intent, PendingIntent.FLAG_UPDATE_CURRENT/*flag*/);
        AlarmManager alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        alarm.cancel(pintent);

        if (Integer.valueOf(Build.VERSION.SDK_INT) >= 19) {
            Log.i("Add Class:", "Phone version is bigger then 19");
            alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pintent);
        } else {
            Log.i("Add Class:", "Phone version is smaller then 19");
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pintent);
        }

        cal.add(Calendar.MINUTE,-10);
        Log.d("Set Alarms","Setting start for "+this.s_name+"-"+this.name+" to "+cal.get(Calendar.DAY_OF_WEEK)+":"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.MONTH)+"/"+cal.get(Calendar.YEAR));
        PendingIntent startPintent = PendingIntent.getService(context, (int) primaryTimeId*1000/*request code*/, startIntent, PendingIntent.FLAG_UPDATE_CURRENT /*flag*/);
        alarm.cancel(startPintent);

        if (Integer.valueOf(Build.VERSION.SDK_INT) >= 19) {
            Log.i("Add Class:", "Phone version is bigger then 19");
            alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), startPintent);
        } else {
            Log.i("Add Class:", "Phone version is smaller then 19");
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, startPintent);
            //TODO check for old phones
        }
    }

    public void delAlarms(Context context){
        Intent intent = new Intent(context, timedClassService.class);
        intent.setData(Uri.parse("id:"+this.id));
        intent.putExtra("class",this);
        Log.d("Service",this.getshortName()+"-"+intent.getExtras().toString());
        PendingIntent pintent;
        AlarmManager alarm;
        alarm = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        for(Map.Entry<Integer, Class.classTime> entry : this.classTimes.entrySet()) {
            long id = entry.getKey();
            Class.classTime t = entry.getValue();

            pintent = PendingIntent.getService(context, (int) id/*request code*/, intent, PendingIntent.FLAG_CANCEL_CURRENT/*flag*/);
            alarm.cancel(pintent);

            pintent = PendingIntent.getService(context, (int) id*1000, intent ,PendingIntent.FLAG_CANCEL_CURRENT);
            alarm.cancel(pintent);
        }
    }

    public boolean isClassTime(){
        this.setTime();

        Calendar cal = Calendar.getInstance();

        Calendar classStart = Calendar.getInstance();
        classStart.set(Calendar.HOUR_OF_DAY,this.time0.hour);
        classStart.set(Calendar.MINUTE,this.time0.minute-TIMETOLERANCE);
        classStart.set(Calendar.SECOND,0);
        classStart.set(Calendar.MILLISECOND,0);

        Calendar classEnd = Calendar.getInstance();
        classEnd.set(Calendar.HOUR_OF_DAY,this.time1.hour);
        classEnd.set(Calendar.MINUTE,this.time1.minute+TIMETOLERANCE);
        classEnd.set(Calendar.SECOND,0);
        classEnd.set(Calendar.MILLISECOND,0);

        if(cal.compareTo(classStart)>=0 && cal.compareTo(classEnd)<=0 && cal.get(Calendar.DAY_OF_WEEK)==this.day){
            Log.i(SERVICE,"it is class time "+this.s_name);
            return true;
        }
        else{
            Log.i(SERVICE,"it is not class time "+this.s_name);
            return false;
        }
    }

}

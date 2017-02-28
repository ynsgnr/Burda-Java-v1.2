package opus.auctor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ConcurrentModificationException;
import java.util.TimeZone;

public class setAlarmReceiver extends BroadcastReceiver {
    public setAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent i = new Intent(context, setAlarmService.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
        else if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED) ){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            String oldTimezone;
            try {
                oldTimezone = prefs.getString("LastKnownTimezone", "");
            }
            catch (ClassCastException e){
                Log.d("AlamReceivr","Catched exception");
                oldTimezone="";
                prefs.edit().putString("LastKnownTimezone", " ").apply();
                e.printStackTrace();
            }
            String newTimezone = TimeZone.getDefault().getID();

            long now = System.currentTimeMillis();

            if (oldTimezone == "" || oldTimezone==" " || TimeZone.getTimeZone(oldTimezone).getOffset(now) != TimeZone.getTimeZone(newTimezone).getOffset(now)) {
                prefs.edit().putString("LastKnownTimezone", newTimezone).apply();
                //update alarms
                Intent i = new Intent(context, setAlarmService.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(i);
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)){

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            Long oldTimeinMilis=prefs.getLong("LastKnownTime", 0);
            Long newTime=System.currentTimeMillis();
            if(oldTimeinMilis==0 || oldTimeinMilis!=newTime){
                prefs.edit().putLong("LastKnownTimezone", newTime).apply();
                Intent i = new Intent(context, setAlarmService.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(i);
            }
        }
        else if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
            Intent i = new Intent(context, setAlarmService.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
    }
}

package opus.auctor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.TimeZone;
import java.util.logging.Logger;

public class setAlarmReceiver extends BroadcastReceiver {
    public setAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Bootup","Burda! recevied something");
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.d("Bootup","Boot recevied, starting service");
            Intent i = new Intent(context, setAlarmService.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
        else if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED) ){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            String oldTimezone = prefs.getString("LastKnownTimezone", null);
            String newTimezone = TimeZone.getDefault().getID();

            long now = System.currentTimeMillis();

            if (oldTimezone == null || TimeZone.getTimeZone(oldTimezone).getOffset(now) != TimeZone.getTimeZone(newTimezone).getOffset(now)) {
                prefs.edit().putString("LastKnownTimezone", newTimezone).apply();
                Log.d("TimezoneChange","TimeZone time change");
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
                Log.d("TimeSettingsChange","System time changed setting up alarms");
                prefs.edit().putLong("LastKnownTimezone", newTime).apply();
                Intent i = new Intent(context, setAlarmService.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(i);
            }
        }
        else if(intent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
            Log.d("DateChange","System date changed setting up alarms");
            Intent i = new Intent(context, setAlarmService.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }


    }
}

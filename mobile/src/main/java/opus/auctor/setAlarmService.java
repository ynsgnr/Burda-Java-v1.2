package opus.auctor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

public class setAlarmService extends Service {
    public setAlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent,int startid){
        Log.d("Bootup","Service started");
        Database db = new Database(getApplicationContext());
        ArrayList<Class> classes = db.Classes();
        Log.d("setalarms","We have classes from the db lets go!");
        for(int i=0;i<classes.size();i++){
            Log.d("setalarms","Setting up alarms for class: "+classes.get(i).s_name+"-"+classes.get(i).name);
            classes.get(i).setAlarms(getApplicationContext());
        }
    }
}

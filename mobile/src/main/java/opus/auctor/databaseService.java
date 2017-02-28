package opus.auctor;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;
import java.util.HashMap;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class databaseService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "opus.auctor.action.FOO";
    private static final String ACTION_BAZ = "opus.auctor.action.BAZ";

    private FirebaseAnalytics mFirebaseAnalytics;

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "opus.auctor.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "opus.auctor.extra.PARAM2";

    public databaseService() {
        super("databaseService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, databaseService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, databaseService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (intent != null) {
            Class c = (Class) intent.getSerializableExtra("class");
            if(c!=null){
                int att = intent.getIntExtra("att",-2);
                if(att!=-2){
                    Long d=intent.getLongExtra("date",0);
                    if(d!=0){
                        Date date = new Date(d);
                        HashMap<Date, Integer> attMap = new HashMap<>();
                        Database db = new Database(getApplicationContext());
                        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(c.id);

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"BackgroundDataService");
                        mFirebaseAnalytics.logEvent("changed_att", bundle);

                        switch (att) {
                            case -1:
                                //Log.i("Database Service", "Class " + c.s_name + "-" + c.name + " id:" + c.id + " | Signed as not attended");
                                attMap.put(date, -1);
                                db.uptAtt(attMap, c);
                                //Toast.makeText(getApplicationContext(),c.s_name+"-"+c.name+" is attended",Toast.LENGTH_SHORT).show();
                                break;
                            case 0:
                                //Log.i("Database Service", "Class " + c.s_name + "-" + c.name + " id:" + c.id + " | Signed as no class");
                                attMap.put(date, 0);
                                db.uptAtt(attMap, c);
                                //Toast.makeText(getApplicationContext(),c.s_name+"-"+c.name+" is canceled",Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                //Log.i("Database Service", "Class " + c.s_name + "-" + c.name + " id:" + c.id + " | Signed as attended");
                                attMap.put(date, 1);
                                db.uptAtt(attMap, c);
                                //Toast.makeText(getApplicationContext(),c.s_name+"-"+c.name+" is skipped",Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

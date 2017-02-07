package opus.auctor;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;


public class classStartService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }


    @Override
    public int onStartCommand(Intent intent,int i ,int j) {
        Class c = SerializationUtils.deserialize((byte[])intent.getSerializableExtra("class"));
        Database db = new Database(getApplicationContext());
        if (c == null ) {
            Log.d("classstartservice","class null, get from db with the id of:"+intent.getDataString());
            c=db.GetClassById(Integer.decode(intent.getDataString()));
        }
        if (c != null && c.Term!=null && c.Term.isDateInTerm(Calendar.getInstance())){
            Log.d("classstarttime","checking hour and minute");
                c.setTime();
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR,c.time0.hour);
                cal.set(Calendar.MINUTE,c.time0.minute);
                cal.add(Calendar.MINUTE,-10);
            Log.d("clsassstarttime","Time is:"+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+" "+c.day+" "+cal.get(Calendar.DAY_OF_WEEK));
                if((cal.get(Calendar.DAY_OF_WEEK)-1)==c.day && cal.compareTo(Calendar.getInstance())>=0) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    Log.d("classStartService", "send class starting notification");
                    //SET Lesson starting notification
                    String strURL = "http://maps.google.com/maps/api/staticmap?&markers=color:0x0277bd%7C" + c.geoFence.Lat + "," + c.geoFence.Long + "&zoom=14&size=200x200";
                    Bitmap myBitmap = null;
                    try {
                        URL url = new URL(strURL);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        myBitmap = BitmapFactory.decodeStream(input);
                    } catch (IOException e) {
                        e.printStackTrace();
                        myBitmap = null;
                    } catch (NetworkOnMainThreadException e) {
                        e.printStackTrace();
                        myBitmap = null;
                    }
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(R.drawable.logoblue)
                                    .setContentTitle(getResources().getString(R.string.startingNotificationStart)+c.s_name + "-" + c.name + getResources().getString(R.string.startingNotificationEnd))
                                    .setContentText(getResources().getString(R.string.startingNotificationBody))
                                    .setVibrate(new long[]{1000, 1000})
                                    .setLights(ContextCompat.getColor(getApplicationContext(), R.color.logoColor), 300, 300)
                                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                    .setPriority(Notification.PRIORITY_HIGH)
                                    .setAutoCancel(true);
                    if (myBitmap != null) {

                        Bitmap output = Bitmap.createBitmap(myBitmap.getWidth(),
                                myBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(output);

                        final int color = 0xff424242;
                        final Paint paint = new Paint();
                        final Rect rect = new Rect(0, 0, myBitmap.getWidth(),
                                myBitmap.getHeight());

                        paint.setAntiAlias(true);
                        canvas.drawARGB(0, 0, 0, 0);
                        paint.setColor(color);
                        canvas.drawCircle(myBitmap.getWidth() / 2,
                                myBitmap.getHeight() / 2, myBitmap.getWidth() / 2, paint);
                        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                        canvas.drawBitmap(myBitmap, rect, rect, paint);
                        mBuilder.setLargeIcon(output);
                    }

                    Intent resultIntent = new Intent(getApplicationContext(), classDetails.class);
                    resultIntent.setData(Uri.parse("id:" + c.id));
                    resultIntent.putExtra("class", c);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    stackBuilder.addParentStack(classDetails.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    c.id,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    // mId allows you to update the notification later on.
                    mNotificationManager.notify(c.id, mBuilder.build());
                }
            else{
            Log.d("classStartService","Not time");
            }
        }
        else{
            Log.d("classStartService","null class");
        }
        return START_NOT_STICKY;
    }
}


package opus.auctor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telecom.Call;
import android.util.Log;
import android.widget.Toast;

public class Database extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "DataBase";

	private static final String TABLE_NAME = "classes";
	private static String CLASS_ID = "class_id";
	private static String COURSE_SHORT_NAME = "s_name";
    private static String COURSE_NAME="name";
    private static String CLASS = "classLocation";
    private static String TEACHER = "teacher";
    private static String COURSE_CODE = "code";
	private static String NOTES = "notes";
    private static String GEOFENCE_ID = "geofenceId";
	private static String LATITUDE = "lat";
	private static String LONGTITUDE = "long";
	private static String FENCERADIUS = "fRad";
    private static String SOCIAL_ID = "socialId";
	private static String COLOR = "color";
	private static String NOTIFICATON = "notify";

	private static final String TERM_TABLE="terms";
	private static String TERM_ID = "term_id";
	private static String TERM_NAME = "t_name";
	private static String TERM_START_DATE_DAY = "day0";
	private static String TERM_START_DATE_MONTH = "month0";
	private static String TERM_START_DATE_YEAR = "year0";
	private static String TERM_END_DATE_DAY = "day1";
	private static String TERM_END_DATE_MONTH = "month1";
	private static String TERM_END_DATE_YEAR = "year1";

	private static final String TIME_TABLE="times";
	private static String TIME_ID = "time_id";
	private static String START_TIME_H = "time0h";
	private static String START_TIME_M = "time0m";
	private static String END_TIME_H = "time1h";
	private static String END_TIME_M = "time1m";
	private static String DAY = "weekDay";

	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        Log.d("Database","creating table");
		String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ CLASS_ID + " INTEGER PRIMARY KEY," //0
                + COURSE_SHORT_NAME + " TEXT,"//1
                + COURSE_NAME + " TEXT,"//2
                + CLASS + " TEXT,"//4
                + TEACHER + " TEXT,"//5
                + COURSE_CODE + " TEXT,"//6
				+ NOTES + " TEXT,"//7
                + GEOFENCE_ID + " TEXT,"//8
				+ LATITUDE + " DOUBLE,"//9
				+ LONGTITUDE + " DOUBLE,"//10
				+ FENCERADIUS + " DOUBLE,"//11
				+ SOCIAL_ID + " INTEGER,"//12
				+ TERM_ID + " INTEGER,"//13
				+ COLOR + " INTEGER,"//14
				+ NOTIFICATON + " INTEGER"+")" //15
				;
		db.execSQL(CREATE_TABLE);

		CREATE_TABLE = "CREATE TABLE " + TERM_TABLE + "("
				+ TERM_ID + " INTEGER PRIMARY KEY,"
				+ TERM_NAME + " TEXT,"
				+ TERM_START_DATE_DAY + " INTEGER,"
				+ TERM_START_DATE_MONTH + " INTEGER,"
				+ TERM_START_DATE_YEAR + " INTEGER,"
				+ TERM_END_DATE_DAY + " INTEGER,"
				+ TERM_END_DATE_MONTH + " INTEGER,"
				+ TERM_END_DATE_YEAR + " INTEGER"+")"
				;
		db.execSQL(CREATE_TABLE);

		CREATE_TABLE = "CREATE TABLE " + TIME_TABLE + "("
				+ TIME_ID + " INTEGER PRIMARY KEY,"//0
				+ CLASS_ID + " INTEGER," //1
				+ START_TIME_H + " INTEGER,"//2
				+ START_TIME_M + " INTEGER,"//3
				+ END_TIME_H + " INTEGER,"//4
				+ END_TIME_M + " INTEGER,"//5
				+ DAY + " INTEGER"+")"//6
		;
		db.execSQL(CREATE_TABLE);
		Log.d("Database","Created tables");
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO wtf is this shit?
    }


	public void deleteClass(Class c){
		SQLiteDatabase db = this.getWritableDatabase();
		if(c.Term!=null && c.Term.id!=-1)
		db.delete("[" + c.Term.id + "]", CLASS_ID + " = ?",
				new String[] { String.valueOf(c.id) });
		 db.delete(TABLE_NAME, CLASS_ID + " = ?",
		            new String[] { String.valueOf(c.id) });
		db.delete(TIME_TABLE, CLASS_ID + " = ?",
				new String[] { String.valueOf(c.id) });

	}

	public void deleteClass(int id){
		deleteClass(GetClassById(id));
	}
	
	public Class addClass(Class tmp) {
        Log.d("Database","adding class to table");
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COURSE_SHORT_NAME, tmp.s_name);
        values.put(COURSE_NAME,tmp.name);
        values.put(CLASS,tmp.classLocation);
        values.put(TEACHER,tmp.teacher);
        values.put(COURSE_CODE,tmp.code);
		values.put(NOTES,tmp.notes);
        values.put(GEOFENCE_ID,tmp.geofenceId);
		values.put(LATITUDE,tmp.geoFence.Lat);
		values.put(LONGTITUDE,tmp.geoFence.Long);
		values.put(FENCERADIUS,tmp.geoFence.fenceRadius);
        values.put(SOCIAL_ID,tmp.socialId);
		values.put(TERM_ID,tmp.Term.id);
		values.put(COLOR,tmp.color);
		if(tmp.notify)
			values.put(NOTIFICATON,1);
		else
			values.put(NOTIFICATON,0);
        tmp.id=(int)db.insert(TABLE_NAME, null, values);

        values = new ContentValues();
        values.put(CLASS_ID,tmp.id);
        db.insert("[" + tmp.Term.id + "]", null, values);
		tmp.setClassTimes(setClassTimes(tmp));

		return tmp;
	}
	

    public Class GetClassById(int id){
		/*
		+ CLASS_ID + " INTEGER PRIMARY KEY," //0
                + COURSE_SHORT_NAME + " TEXT,"//1
                + COURSE_NAME + " TEXT,"//2
                + CLASS + " TEXT,"//4
                + TEACHER + " TEXT,"//5
                + COURSE_CODE + " TEXT,"//6
				+ NOTES + " TEXT,"//7
                + GEOFENCE_ID + " TEXT,"//8
				+ LATITUDE + " DOUBLE,"//9
				+ LONGTITUDE + " DOUBLE,"//10
				+ FENCERADIUS + " DOUBLE,"//11
				+ SOCIAL_ID + " INTEGER,"//12
				+ TERM_ID + " INTEGER,"//13
				+ COLOR + " INTEGER,"//14
				+ NOTIFICATON + "INTEGER," //15
		 */
		String selectQuery = "SELECT * FROM " + TABLE_NAME+ " WHERE id="+id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        Class classById = new Class();
        if(cursor.getCount() > 0){
			classById.id=cursor.getInt(0);
			classById.s_name=cursor.getString(1);
			classById.name=cursor.getString(2);
			classById.classLocation=cursor.getString(3);
			classById.teacher=cursor.getString(4);
			classById.code=cursor.getString(5);
			classById.notes=cursor.getString(6);
			classById.geofenceId=cursor.getString(7);
			classById.geoFence.Lat=cursor.getDouble(8);
			classById.geoFence.Long=cursor.getDouble(9);
			classById.geoFence.fenceRadius=cursor.getDouble(10);
			classById.socialId=cursor.getInt(11);
			classById.Term.id = cursor.getInt(12);
			classById.color=cursor.getInt(13);
			int notify=cursor.getInt(14);
			if(notify<=0){
				classById.notify=false;
			}
			else{
				classById.notify=true;
			}
			classById.setClassTimes(getClassTimes(classById));
			classById.Term=getTerm(classById.Term.id);
        }
        cursor.close();

		return classById;
	}
	
	public  ArrayList<Class> Classes(){
		/*
		+ CLASS_ID + " INTEGER PRIMARY KEY," //0
                + COURSE_SHORT_NAME + " TEXT,"//1
                + COURSE_NAME + " TEXT,"//2
                + CLASS + " TEXT,"//3
                + TEACHER + " TEXT,"//4
                + COURSE_CODE + " TEXT,"//5
				+ NOTES + " TEXT,"//6
                + GEOFENCE_ID + " TEXT,"//7
				+ LATITUDE + " DOUBLE,"//8
				+ LONGTITUDE + " DOUBLE,"//9
				+ FENCERADIUS + " DOUBLE,"//10
				+ SOCIAL_ID + " INTEGER,"//11
				+ TERM_ID + " INTEGER,"//12
				+ COLOR + " INTEGER,"//13
				+ NOTIFICATON + "INTEGER," //14*/
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + TABLE_NAME;
		Cursor cursor = db.rawQuery(selectQuery, null);
	    ArrayList<Class> classList = new ArrayList<Class>();
        Log.d("Database",Integer.toString(cursor.getCount()));
	    if (cursor.moveToFirst()) {
	        do {
	            Class tmp = new Class();
	            tmp.id=cursor.getInt(0);
                tmp.s_name=cursor.getString(1);
                tmp.name=cursor.getString(2);
                tmp.classLocation=cursor.getString(3);
                tmp.teacher=cursor.getString(4);
                tmp.code=cursor.getString(5);
                tmp.notes=cursor.getString(6);
                tmp.geofenceId=cursor.getString(7);
                tmp.geoFence.Lat=cursor.getDouble(8);
                tmp.geoFence.Long=cursor.getDouble(9);
                tmp.geoFence.fenceRadius=cursor.getDouble(10);
                tmp.socialId=cursor.getInt(11);
				tmp.Term.id = cursor.getInt(12);
				tmp.color=cursor.getInt(13);
				int notify=cursor.getInt(14);
				if(notify<=0){
					tmp.notify=false;
				}
				else{
					tmp.notify=true;
				}
				tmp.setClassTimes(getClassTimes(tmp));
				tmp.Term=getTerm(tmp.Term.id);
	            classList.add(tmp);
	        } while (cursor.moveToNext());
	    }

	    return classList;
	}


	public Class editClass (Class c) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    ContentValues values = new ContentValues();

		String selectQuery = "SELECT * FROM " + TABLE_NAME+ " WHERE "+CLASS_ID+"="+c.id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		int termId=0;
		if(cursor.getCount() > 0){
			termId = cursor.getInt(12);
			//Get term id
		}
		cursor.close();

		if(c.Term==null){
			c.Term = new term();
			c.Term.id=-1;
		}

		if(termId!=c.Term.id) {
			if(termId!=-1)
			db.delete("[" + termId + "]", CLASS_ID + " = ?",
					new String[] { String.valueOf(c.id) });
			if(c.Term.id!=-1){
				values.put(CLASS_ID,c.id);
				db.insert("[" + c.Term.id + "]", null, values);
				values.clear();
			}

		}
		values.put(COURSE_SHORT_NAME, c.s_name);
		values.put(COURSE_NAME, c.name);
		values.put(CLASS, c.classLocation);
		values.put(TEACHER, c.teacher);
		values.put(COURSE_CODE, c.code);
		values.put(NOTES, c.notes);
		values.put(GEOFENCE_ID, c.geofenceId);
		values.put(LATITUDE, c.geoFence.Lat);
		values.put(LONGTITUDE, c.geoFence.Long);
		values.put(FENCERADIUS, c.geoFence.fenceRadius);
		values.put(SOCIAL_ID, c.socialId);
		values.put(TERM_ID, c.Term.id);
		values.put(COLOR, c.color);
		if(c.notify)
			values.put(NOTIFICATON,1);
		else
			values.put(NOTIFICATON,0);
		// updating row
		db.update(TABLE_NAME, values, CLASS_ID + " =? ",
				new String[]{String.valueOf(c.id)});
		c.setClassTimes(setClassTimes(c));

		return c;
	}

	public void addTerm(term Term) {

		String CREATE_TABLE;

		Log.d("Database","adding term to table");
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(TERM_NAME, Term.name);
		values.put(TERM_START_DATE_DAY, Term.start.day);
		values.put(TERM_START_DATE_MONTH, Term.start.month);
		values.put(TERM_START_DATE_YEAR, Term.start.year);
		values.put(TERM_END_DATE_DAY, Term.end.day);
		values.put(TERM_END_DATE_MONTH,Term.end.month);
		values.put(TERM_END_DATE_YEAR,Term.end.year);
		Term.id=(int) db.insert(TERM_TABLE, null, values);
        Log.d("Database",Integer.toString(Term.id));
        Log.d("Database",Term.name+"-"+Term.start.toString()+"-"+Term.end.toString());
		CREATE_TABLE =
				"CREATE TABLE " + "[" + Term.id + "]" + "(" + CLASS_ID +" INTEGER,";
		//Create a column for every day between
		Calendar start = Calendar.getInstance();
		start.set(Term.start.year,Term.start.month,Term.start.day);
		Calendar stop = Calendar.getInstance();
		stop.set(Term.end.year,Term.end.month,Term.end.day);

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		for(;start.getTimeInMillis()<stop.getTimeInMillis();start.add(Calendar.DAY_OF_MONTH,1)){
			CREATE_TABLE=CREATE_TABLE + "[" + /*Date in string*/ df.format(start.getTime()) + "]" + " INTEGER,";
		}
        CREATE_TABLE = CREATE_TABLE.substring(0, CREATE_TABLE.length() - 1);
		CREATE_TABLE = CREATE_TABLE + ")";

		db.execSQL(CREATE_TABLE);

	}

	public void deleteTerm(term Term){
		SQLiteDatabase db = this.getWritableDatabase();
		//TODO show dialog
		String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE "+TERM_ID+"="+Term.id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				Class tmp = new Class();
				tmp.id=cursor.getInt(0);
				tmp.s_name=cursor.getString(1);
				tmp.name=cursor.getString(2);
				tmp.classLocation=cursor.getString(3);
				tmp.teacher=cursor.getString(4);
				tmp.code=cursor.getString(5);
				tmp.notes=cursor.getString(6);
				tmp.geofenceId=cursor.getString(7);
				tmp.geoFence.Lat=cursor.getDouble(8);
				tmp.geoFence.Long=cursor.getDouble(9);
				tmp.geoFence.fenceRadius=cursor.getDouble(10);
				tmp.socialId=cursor.getInt(11);
				tmp.Term.id = cursor.getInt(12);
				tmp.color=cursor.getInt(13);
				int notify=cursor.getInt(14);
				if(notify==0){
					tmp.notify=false;
				}
				else{
					tmp.notify=true;
				}
				tmp.setClassTimes(getClassTimes(tmp));
				tmp.Term.id=-1;
				editClass(tmp);
			} while (cursor.moveToNext());
		}

		db.execSQL("DROP TABLE IF EXISTS " + "[" + Term.id + "]");
		db.delete(TERM_TABLE, TERM_ID + " = ?",
				new String[] { String.valueOf(Term.id) });

	}

	public  ArrayList<term> terms(){
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + TERM_TABLE ;
		Cursor cursor = db.rawQuery(selectQuery, null);
		ArrayList<term> termList = new ArrayList<term>();
		if (cursor.moveToFirst()) {
			do {
				term Term = new term();
				Term.id=cursor.getInt(0);
				Term.name=cursor.getString(1);
				Term.start.day=cursor.getInt(2);
				Term.start.month=cursor.getInt(3);
				Term.start.year=cursor.getInt(4);
				Term.end.day=cursor.getInt(5);
				Term.end.month=cursor.getInt(6);
				Term.end.year=cursor.getInt(7);
				termList.add(Term);
			} while (cursor.moveToNext());
		}
		//
		// return kitap liste
		return termList;
	}

	public  term getTerm(int id){
		if(id<0) {
			return null;
			//TODO add snackbar
		}
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + TERM_TABLE + " WHERE "+TERM_ID+"="+id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		term t=new term();
		if (cursor.getCount() > 0) {
				t.id=id;
				t.name=cursor.getString(1);
				t.start.day=cursor.getInt(2);
				t.start.month=cursor.getInt(3);
				t.start.year=cursor.getInt(4);
				t.end.day=cursor.getInt(5);
				t.end.month=cursor.getInt(6);
				t.end.year=cursor.getInt(7);
		}

		// return kitap liste
		return t;
	}

	public HashMap<Integer,Class.classTime> getClassTimes(Class c){
		HashMap<Integer,Class.classTime> t = new HashMap<>();
		Class.classTime classtime ;
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + TIME_TABLE + " WHERE " + CLASS_ID + "=" + c.id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				Log.d("Class Time","Creating new class for getClassTimes");
				classtime= c.new classTime();
				/*+ TIME_ID + " INTEGER PRIMARY KEY,"//0
				+ CLASS_ID + " INTEGER," //1
				+ START_TIME_H + " INTEGER,"//2
				+ START_TIME_M + " INTEGER,"//3
				+ END_TIME_H + " INTEGER,"//4
				+ END_TIME_M + " INTEGER,"//5
				+ DAY + " INTEGER"+")"//6*/
				classtime.startTime.hour=cursor.getInt(2);
				classtime.startTime.minute=cursor.getInt(3);
				classtime.endTime.hour=cursor.getInt(4);
				classtime.endTime.minute=cursor.getInt(5);
				classtime.day=cursor.getInt(6);
				t.put(cursor.getInt(0),classtime);
			} while (cursor.moveToNext());
		}

		return t;
	}

	public HashMap<Integer,Class.classTime> setClassTimes(Class c){

		SQLiteDatabase db = this.getWritableDatabase();

		Log.d("Class Times","Deleting times of class from db:"+c.id+"-"+c.s_name);

		db.delete(TIME_TABLE, CLASS_ID + " = ?",
				new String[] { String.valueOf(c.id) });

		HashMap<Integer,Class.classTime> classTimes = new HashMap<>();
		ContentValues values = new ContentValues();

		for(Map.Entry<Integer, Class.classTime> entry : c.classTimes.entrySet()) {
			Class.classTime t = entry.getValue();
			/*+ TIME_ID + " INTEGER PRIMARY KEY,"//0
				+ CLASS_ID + " INTEGER," //1
				+ START_TIME_H + " INTEGER,"//2
				+ START_TIME_M + " INTEGER,"//3
				+ END_TIME_H + " INTEGER,"//4
				+ END_TIME_M + " INTEGER,"//5
				+ DAY + " INTEGER"+")"//6
			 */
			Log.d("Class Times","Adding new time to db: "+t.getString()+"in:"+c.id+"-"+c.s_name);
			values.clear();
			values.put(CLASS_ID, c.id);
			values.put(START_TIME_H, t.startTime.hour);
			values.put(START_TIME_M, t.startTime.minute);
			values.put(END_TIME_H, t.endTime.hour);
			values.put(END_TIME_M, t.endTime.minute);
			values.put(DAY, t.day);
			int id=(int)db.insert(TIME_TABLE, null, values);
			classTimes.put(id,t);
		}

		return classTimes;
	}

	public HashMap<Date,Integer> getAtt (Class c){
		HashMap<Date,Integer> attendance=new HashMap<>();
		if(c.Term==null || c.Term.id<0)
			return attendance;
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + "[" + c.Term.id + "] WHERE " + CLASS_ID + "=" + c.id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		if(cursor.getCount()<1){
			return attendance;
		}
		cursor.moveToFirst();
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date d = new Date();
		Log.d("Database",Integer.toString(cursor.getColumnCount()));
		for(int i=1;i<cursor.getColumnCount();i++){
			try{
			    d=df.parse(cursor.getColumnName(i));
			}
            catch (java.text.ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			Log.d("getAtt",d.toString()+"-"+i);
            attendance.put(d,cursor.getInt(i));
		}

        return attendance;
	}

	public int getAtt (Class c, Date date){
		if(c.Term==null || c.Term.id<0)
			return -3;
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat attdf = new SimpleDateFormat("dd/MM/yyyy");
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + "[" + c.Term.id + "] WHERE " + CLASS_ID + "=" + c.id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		String attDate = attdf.format(date);
		int i=-2;
		try{
		i=cursor.getInt(cursor.getColumnIndex(attDate));
		}
		catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		catch (java.lang.RuntimeException e){
			e.printStackTrace();
		}

		return i;
	}

	public void uptAtt (HashMap<Date,Integer> att, Class c){
		if(c.Term!=null && c.Term.id>=0) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			Date d;
			for (Map.Entry<Date, Integer> entry : att.entrySet()) {
				d = entry.getKey();
				Log.d("updAtt", "Updating: " + d.toString());
				values.put("[" + df.format(d) + "]", entry.getValue());
			}
			if(values.size()>0)
				db.update("[" + c.Term.id + "]", values, CLASS_ID + " = ?",
					new String[]{String.valueOf(c.id)});

		}
	}
}

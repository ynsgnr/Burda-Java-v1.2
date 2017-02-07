package opus.auctor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yunus on 11.11.2016.
 */

public class term implements Serializable{
    int id;
    String name;
    date start=new date();
    date end=new date();

    public class date implements Serializable{
        int day;
        int month;
        int year;

        public String toString(){
            return Integer.toString(day)+"/"+Integer.toString(month)+"/"+Integer.toString(year);
        }

        public long getTime (){
            Calendar cal=Calendar.getInstance();
            cal.set(year,month,day);
            return cal.getTime().getTime()/1000;
        }

        public Calendar getCal(){
            Calendar cal=Calendar.getInstance();
            cal.set(year,month,day,0,0,0);
            return cal;
        }
    }

    public Boolean isDateInTerm(Calendar cal){
        if(this.start.getCal().compareTo(cal)<=0 && this.end.getCal().compareTo(cal)>=0)
            return true;
        else
            return false;
    }

    public String toString() {
        return name;
    }
}

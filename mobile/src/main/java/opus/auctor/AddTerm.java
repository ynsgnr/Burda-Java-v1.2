package opus.auctor;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddTerm extends Activity {

    EditText name;
    Button dateButton0;
    Button dateButton1;
    Calendar cal;
    int mYear;
    int mMonth;
    int mDay;
    term Term;
    FloatingActionButton done;
    Database db;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_term);

        done = (FloatingActionButton) findViewById(R.id.addTerm_done);
        dateButton0=(Button) findViewById(R.id.dateButton0);
        dateButton1=(Button) findViewById(R.id.dateButton1);
        name = (EditText) findViewById(R.id.name);

        Term = new term();

        cal= Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Term.start.day=mDay;
        Term.start.month=mMonth+1;
        Term.start.year=mYear;

        Term.end.day=mDay;
        Term.end.month=mMonth+1;
        Term.end.year=mYear+1;

        dateButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(AddTerm.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                dateButton0.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);
                                Term.start.day=dayOfMonth;
                                Term.start.month=monthOfYear;
                                Term.start.year=year;
                                //Toast.makeText(getApplicationContext(),"Day:"+dayOfMonth+"-"+monthOfYear+"-"+year,Toast.LENGTH_LONG).show();
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
            }
        });

        dateButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(AddTerm.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // Display Selected date in textbox
                                dateButton1.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);
                                Term.end.day=dayOfMonth;
                                Term.end.month=monthOfYear;
                                Term.end.year=year;
                                //Toast.makeText(getApplicationContext(),"Day:"+dayOfMonth+"-"+monthOfYear+"-"+year,Toast.LENGTH_LONG).show();
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
            }
        });

        ListView list = (ListView) findViewById(R.id.termList);
        list.setVisibility(View.VISIBLE);
        db = new Database(getApplicationContext());
        final TermAdapter times = new TermAdapter(this,db.terms());
        list.setAdapter(times);
        list.setStackFromBottom(true);

        done.bringToFront();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Term.name = name.getText().toString();
                String name = Term.name;
                name.replace(" ","");
                if (name.length()==0){
                    Snackbar.make(v, getResources().getString(R.string.chooseName) , Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else{
                    db.addTerm(Term);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"AddTermActivity");
                    mFirebaseAnalytics.logEvent("added_term", bundle);

                    finish();
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    public class TermAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<term> objects;

        private class ViewHolder {
            TextView name;
            TextView dates;
            Button del;
        }

        public TermAdapter(Context context, ArrayList<term> objects) {
            inflater = LayoutInflater.from(context);
            this.objects = objects;
        }

        public int getCount() {
            return objects.size();
        }

        public term getItem(int position) {
            return objects.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.add_term_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.dates = (TextView) convertView.findViewById(R.id.date);
                holder.del = (Button) convertView.findViewById(R.id.delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            term tmp = objects.get(position);
            holder.name.setText(tmp.name);
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
            holder.dates.setText(sdf1.format(tmp.start.getCal().getTimeInMillis())+"-"+sdf2.format(tmp.end.getCal().getTimeInMillis()));
            holder.del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(AddTerm.this);
                    builder.setTitle(getResources().getString(R.string.losingAttData));
                    builder.setMessage(getResources().getString(R.string.delAttLostData));
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });


                    builder.setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.deleteTerm(objects.get(position));
                            objects.remove(position);

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID,"AddTermActivity");
                            mFirebaseAnalytics.logEvent("deleted_term", bundle);

                            notifyDataSetChanged();
                        }
                    });
                    builder.show();
                }
            });
            return convertView;
        }
    }
}

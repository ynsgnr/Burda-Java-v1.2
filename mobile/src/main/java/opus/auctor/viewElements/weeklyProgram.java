package opus.auctor.viewElements;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import opus.auctor.weekview.MonthLoader;
import opus.auctor.weekview.WeekView;
import opus.auctor.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opus.auctor.Class;
import opus.auctor.Database;
import opus.auctor.R;
import opus.auctor.classDetails;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link weeklyProgram.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link weeklyProgram#newInstance} factory method to
 * create an instance of this fragment.
 */
public class weeklyProgram extends Fragment implements WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {
    WeekView mWeekView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public weeklyProgram() {
        // Required empty public constructor
    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment weeklyProgram.
     */
    // TODO: Rename and change types and number of parameters
    public static weeklyProgram newInstance(String param1, String param2) {
        weeklyProgram fragment = new weeklyProgram();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Fragment","oncreate");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("Fragment","oncreateview");
        return inflater.inflate(R.layout.fragment_weekly_program, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) getActivity().findViewById(R.id.weekView);
        if(mWeekView==null){
            Log.d("Fragmet","Incoming crash, no weekview");
        }

        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        mWeekView.setEmptyViewLongPressListener(this);

        mWeekView.goToHour(8);

        mWeekView.setDrawFinishedListener(new WeekView.DrawFinishedListener() {
            @Override
            public void drawingFinished() {
               mWeekView.invalidate();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        Log.d("Fragment","onattach");
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Fragment","onResume");
        mWeekView.notifyDatasetChanged();
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        //Get class and go to classDetails
        Database db = new Database(getActivity());
        Class tmp = db.GetClassById((int)event.getId());
        if(tmp!=null) {
            Intent intent = new Intent(getActivity(), classDetails.class);
            //send data to addClass activity
            intent.putExtra("class", tmp);
            startActivity(intent);
        }
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        //TODO get to addclass with hours
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with some events.
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        Database db = new Database(getActivity());
        List<Class> classes = db.Classes();

        Class c;
        for(int i=0;i<classes.size();i++){
            c=classes.get(i);
            HashMap<Integer,Class.classTime> classTimes=c.classTimes;
            for(Map.Entry<Integer, Class.classTime> entry : classTimes.entrySet()){
                c.setTime(entry.getKey());

                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.MONTH, newMonth-1);
                startTime.set(Calendar.YEAR, newYear);
                startTime.set(Calendar.DAY_OF_MONTH,0);
                Calendar endTime = Calendar.getInstance();
                endTime.set(Calendar.MONTH, newMonth-1);
                endTime.set(Calendar.YEAR, newYear);
                endTime.set(Calendar.DAY_OF_MONTH,0);

                startTime.set(Calendar.DAY_OF_WEEK,c.day);
                startTime.set(Calendar.HOUR_OF_DAY,c.time0.hour);
                startTime.set(Calendar.MINUTE,c.time0.minute);
                startTime.set(Calendar.SECOND,0);

                endTime.set(Calendar.DAY_OF_WEEK,c.day);
                endTime.set(Calendar.HOUR_OF_DAY,c.time1.hour);
                endTime.set(Calendar.MINUTE,c.time1.minute);
                endTime.set(Calendar.SECOND,0);

                WeekViewEvent event = new WeekViewEvent(c.id, c.s_name,c.classLocation, c.name, c.teacher ,startTime, endTime);
                event.setColor(c.color);
                events.add(event);

                Calendar startTime1= (Calendar) startTime.clone();
                startTime1.add(Calendar.DAY_OF_YEAR,7);

                Calendar endTime1= (Calendar) endTime.clone();
                endTime1.add(Calendar.DAY_OF_YEAR,7);

                event = new WeekViewEvent(c.id, c.s_name, c.classLocation, c.name, c.teacher , startTime1, endTime1);
                event.setColor(c.color);
                events.add(event);


                Calendar startTime2= (Calendar) startTime1.clone();
                startTime2.add(Calendar.DAY_OF_YEAR,7);

                Calendar endTime2= (Calendar) endTime1.clone();
                endTime2.add(Calendar.DAY_OF_YEAR,7);

                event = new WeekViewEvent(c.id, c.s_name, c.classLocation, c.name, c.teacher , startTime2, endTime2);
                event.setColor(c.color);
                events.add(event);


                Calendar startTime3= (Calendar) startTime2.clone();
                startTime3.add(Calendar.DAY_OF_YEAR,7);

                Calendar endTime3= (Calendar) endTime2.clone();
                endTime3.add(Calendar.DAY_OF_YEAR,7);

                event = new WeekViewEvent(c.id, c.s_name, c.classLocation, c.name, c.teacher , startTime3, endTime3);
                event.setColor(c.color);
                events.add(event);
            }
        }
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.MONTH, newMonth-1);
        startTime.set(Calendar.YEAR, newYear);
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.MONTH, newMonth-1);
        endTime.set(Calendar.YEAR, newYear);
        for(int i=0;i<events.size();i++){
            startTime=events.get(i).getStartTime();
            endTime=events.get(i).getEndTime();
            Log.d("Fragment",i+":"+events.get(i).getId()+"-"+events.get(i).getName()+"-" +
                    startTime.get(Calendar.DAY_OF_MONTH)+"-"+startTime.get(Calendar.MONTH)+"-"+startTime.get(Calendar.YEAR)+"/"+
                    endTime.get(Calendar.DAY_OF_MONTH)+"-"+endTime.get(Calendar.MONTH)+"-"+endTime.get(Calendar.YEAR));
        }
        return events;
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}

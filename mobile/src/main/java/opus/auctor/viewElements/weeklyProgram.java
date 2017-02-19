package opus.auctor.viewElements;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import opus.auctor.Class;
import opus.auctor.Database;
import opus.auctor.MainActivity;
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
public class weeklyProgram extends Fragment {

    ArrayList<Class> classes;
    ListView list;

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
        Log.d("Fragment","Starting fragment");
        Database db = new Database(getActivity());
        classes=db.Classes();
        if (classes.size()==0){
            //Database is empty
            ArrayList <Class> l = new ArrayList<>();
            ClassAdapter c = new ClassAdapter(getActivity(),l);
            list = (ListView)getActivity().findViewById(R.id.classesMonday);
            list.setAdapter(c);

            list = (ListView) getActivity().findViewById(R.id.classesTuesday);
            list.setAdapter(c);

            list = (ListView) getActivity().findViewById(R.id.classesWednesday);
            list.setAdapter(c);

            list = (ListView) getActivity().findViewById(R.id.classesThursday);
            list.setAdapter(c);

            list = (ListView) getActivity().findViewById(R.id.classesFriday);
            list.setAdapter(c);
        }
        else{
            //crate weekly table
            ArrayList<Class> mondayList = new ArrayList<Class>();
            ArrayList<Class> tuesdayList = new ArrayList<Class>();
            ArrayList<Class> wednesdayList = new ArrayList<Class>();
            ArrayList<Class> thursdayList = new ArrayList<Class>();
            ArrayList<Class> fridayList = new ArrayList<Class>();
            ArrayList<Class> saturdayList = new ArrayList<Class>();
            ArrayList<Class> sundayList = new ArrayList<Class>();


            Class c;

            for(int i=0;i<classes.size();i++)
            {
                c=classes.get(i);
                //c.setAlarms(getApplicationContext());
                for(Map.Entry<Integer, Class.classTime> entry : c.classTimes.entrySet()) {
                    c.primaryTimeId=entry.getKey();
                    c.setTime();
                    Log.d("Main Table","Found time: "+c.primaryTimeId+"-"+c.day+"-"+c.s_name+" Time:"+c.time0.getString()+"-"+c.time1.getString());
                    Class tmp = new Class(c);
                    switch (c.day) {
                        case Calendar.MONDAY:
                            mondayList.add(tmp);
                            break;
                        case Calendar.TUESDAY:
                            tuesdayList.add(tmp);
                            break;
                        case Calendar.WEDNESDAY:
                            wednesdayList.add(tmp);
                            break;
                        case Calendar.THURSDAY:
                            thursdayList.add(tmp);
                            break;
                        case Calendar.FRIDAY:
                            fridayList.add(tmp);
                            break;
                        case Calendar.SATURDAY:
                            saturdayList.add(tmp);
                            break;
                        case Calendar.SUNDAY:
                            sundayList.add(tmp);
                            break;
                    }
                }
            }

            //Order by time
            sort(mondayList);
            sort(tuesdayList);
            sort(wednesdayList);
            sort(thursdayList);
            sort(fridayList);
            sort(saturdayList);
            sort(sundayList);

            ClassAdapter tmp0 = new ClassAdapter (getActivity(), mondayList);
            list = (ListView) getActivity().findViewById(R.id.classesMonday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp0);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(getActivity(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    getActivity().startActivity(intent);
                }
            });

            ClassAdapter tmp1 = new ClassAdapter (getActivity(), tuesdayList);
            list = (ListView) getActivity().findViewById(R.id.classesTuesday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp1);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(getActivity(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    getActivity().startActivity(intent);
                }
            });

            ClassAdapter tmp2 = new ClassAdapter (getActivity(), wednesdayList);
            list = (ListView) getActivity().findViewById(R.id.classesWednesday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp2);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(getActivity(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    getActivity().startActivity(intent);
                }
            });

            ClassAdapter tmp3 = new ClassAdapter (getActivity(), thursdayList);
            list = (ListView) getActivity().findViewById(R.id.classesThursday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp3);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(getActivity().getApplicationContext(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    getActivity().startActivity(intent);
                }
            });

            ClassAdapter tmp4 = new ClassAdapter (getActivity(), fridayList);
            list = (ListView) getActivity().findViewById(R.id.classesFriday);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(tmp4);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Class tmp = (Class) parent.getItemAtPosition(position);
                    Intent intent = new Intent(getActivity(),classDetails.class);
                    //send data to addClass activity intent.putExtra("key", value);
                    intent.putExtra("class",tmp);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.d("Fragment","onattach");
        super.onAttach(context);
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

    public void sort(ArrayList<Class> classList){
        if (classList.size() == 0) {
            return;
        }
        quickSort(classList, 0, classList.size() - 1);
    }

    private void quickSort(ArrayList<Class> classList,int lowerIndex, int higherIndex) {

        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number, I am taking pivot as middle index number
        Class c = classList.get(lowerIndex+(higherIndex-lowerIndex)/2);
        int pivot = c.classTimes.get(c.primaryTimeId).startTime.getStamp();
        // Divide into two arrays
        while (i <= j) {
            /**
             * In each iteration, we will identify a number from left side which
             * is greater then the pivot value, and also we will identify a number
             * from right side which is less then the pivot value. Once the search
             * is done, then we exchange both numbers.
             */
            c=classList.get(i);
            while (c.classTimes.get(c.primaryTimeId).startTime.getStamp() < pivot) {
                Log.d("Sort",c.classTimes.get(c.primaryTimeId).startTime.getStamp()+"<"+pivot);
                i++;
                c=classList.get(i);
            }
            c=classList.get(j);
            while (c.classTimes.get(c.primaryTimeId).startTime.getStamp() > pivot) {
                Log.d("Sort",c.classTimes.get(c.primaryTimeId).startTime.getStamp()+">"+pivot);
                j--;
                c=classList.get(j);
            }
            if (i <= j) {
                Collections.swap(classList,i,j);
                Log.d("Sort","swaping:"+i+" with "+j);
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSort(classList,lowerIndex, j);
        if (i < higherIndex)
            quickSort(classList,i, higherIndex);
    }

    public class ClassAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<Class> objects;

        private class ViewHolder {
            TextView textView1;
            TextView textView2;
            TextView textView3;
            TextView textView4;
            TextView textView5;
            TextView textView6;
        }

        public ClassAdapter(Context context, ArrayList<Class> objects) {
            inflater = LayoutInflater.from(context);
            this.objects = objects;
        }

        public int getCount() {
            return objects.size();
        }

        public Class getItem(int position) {
            return objects.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item, null);
                holder.textView1 = (TextView) convertView.findViewById(R.id.shortName);
                holder.textView2 = (TextView) convertView.findViewById(R.id.className);
                holder.textView3 = (TextView) convertView.findViewById(R.id.teacher);
                holder.textView4 = (TextView) convertView.findViewById(R.id.classLocation);
                holder.textView5 = (TextView) convertView.findViewById(R.id.time1);
                holder.textView6 = (TextView) convertView.findViewById(R.id.time2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Class tmp = objects.get(position);
            holder.textView1.setText(tmp.s_name);
            holder.textView2.setText(tmp.name);
            holder.textView3.setText(tmp.teacher);
            holder.textView4.setText(tmp.classLocation);
            holder.textView5.setText(tmp.time0.getString());
            holder.textView6.setText(tmp.time1.getString());
            Log.d("Main Table","Primary time of "+tmp.s_name+" is:"+tmp.primaryTimeId+"-"+tmp.classTimes.get(tmp.primaryTimeId).startTime.getString());
            return convertView;
        }
    }

}

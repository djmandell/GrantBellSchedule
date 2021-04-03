package com.grantcompsci.ghsbellschedule;

import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;
//import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by djmandell on 10/28/16.
 */
public class PeriodAdapter extends RecyclerView.Adapter<PeriodAdapter.PeriodViewHolder> {
    private Period[] mPeriods;
    private int mDateNumber;
    private int mTodayDateNumber;
    public static final String TAG = MainActivity.class.getSimpleName();


    public int getTodayDateNumber() {
        return mTodayDateNumber;
    }

    public void setTodayDateNumber(int todayDateNumber) {
        mTodayDateNumber = todayDateNumber;
    }


    public PeriodAdapter(Period[] periods){
        mPeriods=periods;
    }
    @Override
    public PeriodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.period_list_item, parent, false);
        PeriodViewHolder viewHolder = new PeriodViewHolder(view);
        return viewHolder;
    }

    public int getDateNumber() {
        return mDateNumber;
    }

    public void setDateNumber(int dateNumber) {
        mDateNumber = dateNumber;
    }

    @Override
    public void onBindViewHolder(PeriodViewHolder holder, int position) {
        holder.bindPeriod(mPeriods[position]);
    }
    //
    @Override
    public int getItemCount() {
        return mPeriods.length;
    }

    public class PeriodViewHolder extends RecyclerView.ViewHolder{
        private TextView mNameLabel;
        private TextView mStartLabel;
        private TextView mEndLabel;
        private RelativeLayout mPeriodListLayout;

        public PeriodViewHolder(View itemView) {
            super(itemView);
            mNameLabel = (TextView) itemView.findViewById(R.id.nameLabel);
            mStartLabel = (TextView) itemView.findViewById(R.id.startLabel);
            mEndLabel = (TextView) itemView.findViewById(R.id.endLabel);
            mPeriodListLayout = (RelativeLayout) itemView.findViewById(R.id.periodListLayout);


        }

        public void bindPeriod(Period period){
            //SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma");
            Calendar selectedDateCalendar = Calendar.getInstance();
            Calendar calendarPeriodStart = Calendar.getInstance();
            Calendar calendarPeriodEnd = Calendar.getInstance();
            Date periodStartTimeDate = new Date();
            Date periodEndTimeDate = new Date();
            Date selectedDate = new Date();
            String selectedDateString = new SimpleDateFormat("hh:mma").format(selectedDateCalendar.getTime());
            //!! Log.i(TAG,"Selected date =====================>" + selectedDateString);
            /* Highlight the current period in the period schedule display
               We check the current time (selectedDateString) and make sure it lays somewhere between current periodStart and periodEnd.

               The date stuff was admittedly a bit confusing.  SimpleDateFormat converts the time we pull from the periodStart,
               periodEnd and the selectedDateString and converts them to Date objects so that they can be compared.

             */
            if (!period.getPeriodStart().equals(" ") && !period.getPeriodStart().equals("")) {


                try {
                    // Sometimes we don't have a period start or end time (PSAT day has weird schedules, late start has "check PPS" message)
                    // Only try to parse period start/end if there is a period start/end


                    periodStartTimeDate = new SimpleDateFormat("hh:mma").parse(period.getPeriodStart());

                    if (!period.getPeriodName().equals("LUNCH")) {
                        calendarPeriodStart.setTime(periodStartTimeDate);
                        calendarPeriodStart.add(Calendar.MINUTE, -5);
                        periodStartTimeDate = calendarPeriodStart.getTime();
                    }

                    periodEndTimeDate = new SimpleDateFormat("hh:mma").parse(period.getPeriodEnd());
                    selectedDate = new SimpleDateFormat("hh:mma").parse(selectedDateString);


                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            // make the TextViews views "GONE" (not take up space in the layout) if they don't contain text.
            else{
                mStartLabel.setVisibility(View.GONE);
                mEndLabel.setVisibility(View.GONE);
                mNameLabel.setGravity(Gravity.LEFT);
            }

            /*Check to see if the current time is before the end of the period and after the start (-5 minutes).
              If so, the relativeLayout's background color will be blue and all the text will be white.
             */
            if((mTodayDateNumber == mDateNumber && selectedDate.before(periodEndTimeDate) ) && ((selectedDate.after(periodStartTimeDate) || selectedDate.equals(periodStartTimeDate)) ) ){
                //!! Log.i(TAG,"Period: " + period.getPeriodName() + "  periodStart: "+ periodStartTimeDate.toString() + "  Period End: " + periodEndTimeDate.toString() + "  Current: " + selectedDate.toString()+ " ");
                mPeriodListLayout.setBackgroundColor(Color.parseColor("#22509b"));
                mNameLabel.setTextColor(Color.WHITE);
                mStartLabel.setTextColor(Color.WHITE);
                mEndLabel.setTextColor(Color.WHITE);

            }


            mNameLabel.setText(period.getPeriodName());
            mStartLabel.setText(period.getPeriodStart());
            mEndLabel.setText(period.getPeriodEnd());

//

        }
    }
}

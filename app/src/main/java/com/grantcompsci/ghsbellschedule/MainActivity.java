package com.grantcompsci.ghsbellschedule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import hirondelle.date4j.DateTime;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static final String TAG = MainActivity.class.getSimpleName();

    /*

     *
     * Current Date = todayDate
     *        Anything that depends on today's date and time gets stored here
     *        (int, String, DateTime, Date, Calendar (?))
     *
     * Date user selects = mSelectedDate
     *        Any time user goes forward one day/backward one day or selects a date from drop-down calendar it should set mSelectedDate
     *        (int, String, DateTime, Date, Calendar (?))
     *

     */

    private static final String PREFS_FILE = "com.grantcompsci.ghsbellschedule";



    private static final String KEY_JSONSCHEDULEDATA = "key_jsonscheduldata";
    private static final String KEY_JSONPERIODSCHEDULEDATA = "key_jsonperiodscheduldata";
    private static final String KEY_DATEVERSIONCHECK = "key_dateversioncheck";
    private static final String KEY_DATEVERSION = "key_dateversion";
    private static final String KEY_VERSION = "key_version";
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mSharedPreferences;
    private ScheduleType mScheduleType;
    private TextView mSummaryLabel;
    private Period [] mPeriods;
    private DividerItemDecoration mDividerItemDecoration;
    private String mJsonScheduleData;
    //private String mJsonPeriodScheduleData;
    //Calendar mCalendar = Calendar.getInstance();
    private Date mTodayDate;
    private Date mSavedDate;
    private DateTime mCurrentDateTime;
    //private String mTodayDateString;
    private int mTodayDateInt;
    private CompactCalendarView mCompactCalendarView;
    private boolean isExpanded = false;
    private AppBarLayout mAppBarLayout;
    private String mOldDateStringPreScroll = "";
    private boolean mScrolled;
    private Date mSelectedDate;
    private String mSelectedDateString;
    private int mSelectedDateInt;
    private DateTime mSelectedDateTime;
    private SimpleDateFormat mYearMonthDayFormatter =  new SimpleDateFormat("yyyyMMdd");
    private OnSwipeTouchListener onSwipeTouchListener;
    private RecyclerView mRecyclerView;



    static final String SAVED_DATE_STRING = "saved_date_string";
    static final String SELECTED_DATE_INT_KEY = "selected_date_int_key";
    static final String SELECTED_DATE_STRING_KEY = "selected_date_string_key";

    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);



    // Track date when we save the instance.  If it's been less than... 3 minutes we'll assume they want same date
    // Dump rotation detection, it doesn't work perfectly and this covers rotation

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save custom values into the bundle

        savedInstanceState.putString(SAVED_DATE_STRING, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        savedInstanceState.putInt(SELECTED_DATE_INT_KEY, mSelectedDateInt);
        savedInstanceState.putString(SELECTED_DATE_STRING_KEY, mSelectedDateString);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        System.out.println();



    }




    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        // Restore state members from saved instance

        mSelectedDateInt = savedInstanceState.getInt(SELECTED_DATE_INT_KEY);
        mSelectedDateString = savedInstanceState.getString(SELECTED_DATE_STRING_KEY);
        Date savedDate = new Date();
        // convert from String to Date
        try {
            mSavedDate = new SimpleDateFormat("yyyyMMddHHmmss").parse(savedInstanceState.getString(SAVED_DATE_STRING));
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        mSavedDate = new Date();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //!! Log.i(TAG,"APP STARTED ===========> ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ImageView arrow = (ImageView) findViewById(R.id.date_picker_arrow);
        setTitle("Grant Bell Schedule");
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        // Set up the CompactCalendarView

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();



        mCompactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        // Force English and set first day of week to Sunday
        mCompactCalendarView.setLocale(TimeZone.getDefault(), Locale.US);
        mCompactCalendarView.setFirstDayOfWeek(Calendar.SUNDAY);
        // mCompactCalendarView.setShouldDrawDaysHeader(true);

        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                //!! Log.i(TAG,"CLICKED A DATE ===========> dateClicked = " + dateClicked);
                setSelectedDate(dateClicked);
                updateDisplay();
                ViewCompat.animate(arrow).rotation(0).start();
                mAppBarLayout.setExpanded(false, true);
                isExpanded = false;
                //mCompactCalendarView.addEvent(new Event(Color.GREEN,1488186000000L));
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                /* -When the month scrolls we want to select the same day in the next/past month
                   -e.g. February 12th scrolls to March 12th, January 12th, etc.
                   -If we're scrolling from a month that has 31 days to a month that has 30 days we automatically select the last day of the month
                   -if we're scrolling to February when the date is the 29th or 30th we'll automatically select the 28th (or 29th on a leap year)
                */
                DateTime dateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd").format(firstDayOfNewMonth));

                int dateOffset = ((int) mSelectedDateTime.getDay())-1;
                //Log.i(TAG,"SCROLLED ===========> dateTime = " + dateTime.getMonth());

                if (mSelectedDateTime.getDay() == 31){
                    dateTime = dateTime.getEndOfMonth();
                }
                else if (dateTime.getMonth()==2){
                    //Log.i(TAG,"SCROLLED ===========> dateTime = " + dateTime.getDay());
                    if (mSelectedDateTime.getDay() > 28){
                        dateTime = dateTime.getEndOfMonth();
                    }
                    else{
                        dateTime = dateTime.plusDays(dateOffset);
                        //dateTime = dateTime.getEndOfMonth();
                    }
                }
                else{
                    dateTime = dateTime.plusDays(dateOffset);
                }
                //!! Log.i(TAG,"SCROLLED AND SET DATE ===========>  new date: "+ dateTime.format("D MMMM YYYY", Locale.US));
                setSubtitle(dateTime.format("WWWW, D MMMM YYYY", Locale.US));
                mScrolled = true;
            }
        });


        // Set current date/time to today/now
        setSelectedDate(new Date());



        RelativeLayout datePickerButton = (RelativeLayout) findViewById(R.id.date_picker_button);

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView datePickerTextViewGetter = (TextView) findViewById(R.id.date_picker_text_view);
                if (isExpanded) {
                    ViewCompat.animate(arrow).rotation(0).start();
                    if (mScrolled==true){
                        if (mSelectedDate != null){
                            setSelectedDate(mSelectedDate);
                            setSubtitle(mOldDateStringPreScroll);
                        }
                        else{
                            setSelectedDate(new Date());
                        }
                        mScrolled = false;
                    }
                    mAppBarLayout.setExpanded(false, true);
                    isExpanded = false;
                } else {
                    mOldDateStringPreScroll = (String) datePickerTextViewGetter.getText();
                    ViewCompat.animate(arrow).rotation(180).start();
                    mAppBarLayout.setExpanded(true, true);
                    isExpanded = true;
                }
            }
        });



        // the TextViews (and other visible stuff) aren't created until setContentView is called.  If you you try to do stuff like below before setContentView you end up with a null object reference
        mSummaryLabel = (TextView)findViewById(R.id.scheduleTypeLabel);
        mSummaryLabel.setText(" LOADING ");

        // START THE PREFERENCES ACTIVITY
/*        Intent i = new Intent(this, MyPreferencesActivity.class);
        startActivity(i);*/



    }


    /* I put all of this code in onResume because there's no guarantee that the app will get destroyed.
    If app stays in memory overnight I want it to check for a new version file the next day, which means all of this needs to be in onResume (right?)

    onResume we now:
        1) Get current date and time (should be ints?)
        2) Check to see if we've ever done a version check, our version check was before today, or it's before 8:15am
            I assume snow days/late start announced before 8:15, so we want to check for new versions whenever app is loaded before that time
        3) Check for network availability
        4) If network available, download version JSON
        5) If version JSON newer than one stored in SharedPrefs

     */
    @Override
    protected void onResume() {
        isExpanded = false;
        mAppBarLayout.setExpanded(false, true);
        super.onResume();



        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        setTodayDate(new Date());

        // check to see if the user has switched back to the app in the last 3 minutes.
        // if they have, load the date they were previously viewing.  If they haven't, load today's date onResume

        if (mSavedDate != null) {
            //Log.i(TAG, "mSavedDate wasn't null ===========> " + mSavedDate.toString());
            DateTime savedDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mSavedDate));
            mCurrentDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mTodayDate));
            //Log.i(TAG,"WHAT DAY IS IT.... getDay: " + mCurrentDateTime.getDay() + " getWeekDay: " + mCurrentDateTime.getWeekDay());
            if (savedDateTime.numSecondsFrom(mCurrentDateTime) < 150) {
                //Log.i(TAG, "THIS WAS RESTORED RECENTLY ===========> " + savedDateTime.numSecondsFrom(currentDateTime));
                try {
                    mSelectedDate = new SimpleDateFormat("yyyyMMdd").parse(mSelectedDateString);
                    setSelectedDate(mSelectedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }// compare current date to saved date... probably


            else {
                setSelectedDate(new Date());
            }
        }

        else {
            setSelectedDate(new Date());
        }


        checkCalendarUpdates();
        updateDisplay();

    }


    public void setTodayDate(Date date) {
        mTodayDate = date;
        mTodayDateInt = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(mTodayDate));
    }

    public void setSelectedDate(Date date) {
        setSubtitle(new SimpleDateFormat("EEEE, d MMMM yyyy", /*Locale.getDefault()*/Locale.ENGLISH).format(date));
        if (mCompactCalendarView != null) {
            mCompactCalendarView.setCurrentDate(date);

            mSelectedDate = date;
            mSelectedDateTime = new DateTime (new SimpleDateFormat("yyyy-MM-dd").format(mSelectedDate));
            mSelectedDateString = new SimpleDateFormat("yyyyMMdd").format(mSelectedDate);
            mSelectedDateInt = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(mSelectedDate));
        }

    }


    @Override
    public void setTitle(CharSequence title) {
        TextView tvTitle = (TextView) findViewById(R.id.title);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public void setSubtitle(String subtitle) {
        TextView datePickerTextView = (TextView) findViewById(R.id.date_picker_text_view);

        if (datePickerTextView != null) {
            datePickerTextView.setText(subtitle);
        }
    }

    private void checkCalendarUpdates(){
        /*  Schedule information exists in three files:
            - scheduleFile - An imported Google Calendar ics file converted to json.  Contains a list of dates and a SUMMARY with schedule type (A, B, A-FLEX, EARLY RELEASE, etc)
            - periodScheduleFile - Manually created json file that maps the schedule type to a list of period NAMEs with their corresponding START/END times
            - versionFile holds DATE of the most recent version and the VERSION of the update (so that multiple revisions can be published in a single day)

            This method checks to see if we have an up-to-date version of the schedule.. It does so as follows:

            1) Checks the version file on the server against the version we have in SharedPrefs if we've never done a download, haven't done a download since yesterday, or it's before 8:15am
            2) If the DATE/VERSION on the server are greater than the one we've got in SharedPrefs we download the SchedulePeriod data and store it in shared Prefs

             AB Calendar grabbed from here: https://calendar.google.com/calendar/embed?src=u2r5154prrjr5uhukp71857g70%40group.calendar.google.com&ctz=America/Los_Angeles%22
                 2017-18 Calendar grabbed from here: https://www.pps.net/site/handlers/icalfeed.ashx?MIID=13161
             used ical2json app to convert the ical (ics) file to a JSON file (syntax: ical2json calendarName.ics)

         */
        String siteName = "http://www.grantcompsci.com/bellapp/";
        String scheduleFile = "schoolYearSchedule.json";
        String periodScheduleFile = "periodSchedule.json";
        String versionFile = "versionNumber.json";
        final String scheduleUrl= siteName + scheduleFile;
        final String periodUrl = siteName+periodScheduleFile;
        final String versionUrl = siteName+versionFile;
        mSharedPreferences = getSharedPreferences(PREFS_FILE,Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        Calendar calendar = Calendar.getInstance();
        int currentTime = Integer.parseInt(new SimpleDateFormat("Hmm").format(calendar.getTime()));
        mCurrentDateTime = new DateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mTodayDate));


        //!! Log.i(TAG,"Our time right now is ===========> " + currentTime);
        //!! Log.i(TAG,"The last time we checked the version was ===========> " + mSharedPreferences.getInt(KEY_DATEVERSIONCHECK,0) + " AND today's date = " + mTodayDateInt);


        // mTodayDateInt (int) used to check to see if we've checked for schedule version updates today. If not, check for updates.
        // currentTime (int) used to see if the time is before 8:30 or after 4:30, if it is
        // we check for version updates even if we've checked already today (in case of weather-related late
        // change)
        // If today's schedule is a late start we check for schedule changes all day, just in case they change from late start to no school.
        // If we've never downloaded the schedule
        // The program also checks the version file every time the app is opened on a weekend, regardless of the time.
        if (mScheduleType != null){
            //Log.i(TAG,"YOOOOOOOOO! " + mScheduleType.getScheduleType());
        }

        if ((mScheduleType != null && (mScheduleType.getScheduleType().equals("B-LATE START") || mScheduleType.getScheduleType().equals("A-LATE START")))
                || mSharedPreferences.getInt(KEY_DATEVERSIONCHECK,0)==0 || mTodayDateInt > mSharedPreferences.getInt(KEY_DATEVERSIONCHECK,0)  ||
                currentTime < 830 || currentTime > 1630 || mCurrentDateTime.getWeekDay() == 7 || mCurrentDateTime.getWeekDay()== 1 )  {
            //Log.i(TAG,"Looks like we either haven't checked the version at all or haven't checked it since yesterday ===========> ");
            // if we have a working network connection we run our code, if not we pop up a toast message saying we don't have one right now.
            // TODO: What to do if no network? Add a refresh button? Automatically attempt every 5 minutes until we get a network connection (battery issues).
            if (isNetworkAvailable()) {
                // Doesn't appear to do what I want.  Revisit?
                //mSummaryLabel.setText(" LOADING UPDATE");


                //Create OkHttp client and build the request
                final  OkHttpClient client = new OkHttpClient();


                Request versionRequest = new Request.Builder()
                        .url(versionUrl) //the location for the file that holds most recent version date/number
                        .build();


                Call versionCall = client.newCall(versionRequest);
                versionCall.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // I imagine we want to be doing something here.  What are best practices for failure?
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String versionJsonData = response.body().string();
                        if (response.isSuccessful()){
                            boolean latestVersion = false;
                            try {
                                // Check to see if we have an older version than the most recent published version
                                latestVersion = checkVersionUpdate(versionJsonData);
                                if (!latestVersion) {

                                    Request request = new Request.Builder()
                                            .url(scheduleUrl)
                                            .build();

                                    // Creates a call based on the built request
                                    Call schedCall = client.newCall(request);
                                    // Execute asynchronous call in order received in queue (don't interfere with main (UI) thread
                                    // Callback pings us when async task is running in background to display stuff
                                    schedCall.enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {

                                        }
                                        // when we get a response save it to a String called jsonData,
                                        // if response is successful run the getCurrentDetails method and save the result to mScheduleType

                                        // TODO: I'm not doing anything with mScheduleType right now.  Need a refresher on how to deal with it.


                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            try {
                                                String jsonData = response.body().string();
                                                mJsonScheduleData = jsonData; // Store in shared prefs, then pull it back out of shared prefs and use it in getCurrentDetails
                                                mEditor.putString(KEY_JSONSCHEDULEDATA, mJsonScheduleData);
                                                mEditor.apply();

                                                if (response.isSuccessful()) {
                                                    //mScheduleType = getCurrentDetails(jsonData);
                                                    Request periodRequest = new Request.Builder()
                                                            .url(periodUrl)
                                                            .build();
                                                    Call periodCall = client.newCall(periodRequest);
                                                    periodCall.enqueue(new Callback() {

                                                        @Override
                                                        public void onFailure(Call call, IOException e) {


                                                        }

                                                        @Override
                                                        public void onResponse(Call call, Response response) throws IOException {
                                                            String periodScheduleData = response.body().string();
                                                            //I never use this.
                                                            //mJsonPeriodScheduleData = periodScheduleData;
                                                            mEditor.putString(KEY_JSONPERIODSCHEDULEDATA, periodScheduleData);
                                                            mEditor.apply();

                                                            // re-runs updateDisplay if there's a change so that today's info immediately reflects the change.
                                                            // You have to update the UI on the UI thread, otherwise you get a "CalledFromWrongThreadException"
                                                            if (response.isSuccessful()){
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        updateDisplay();

                                                                    }
                                                                });

                                                            }


                                                        }
                                                    });


                                                } else {
                                                    // Log.i(TAG,"FAILURE 999999999999999999999999");

                                                }
                                            }
                                            catch (IOException e) {
                                                //!! Log.e(TAG, "Exception caught: ", e);
                                            }
/*                                            catch (JSONException e){
                                                //!! Log.e(TAG, "Exception caught: ", e);
                                            }*/
                                        }
                                    });



                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


            }

            else {
                // Sometimes seems to result in a "NOT CONNECTED" error when device wakes up and app already open.
                // Commenting this out for now, as there's no way to manually refresh and this message would
                // probably just be confusing.

                //potentially can make the toast persist for more than 3 seconds by using "The best solution" on http://stackoverflow.com/questions/2220560/can-an-android-toast-be-longer-than-toast-length-long
                //TODO: Figure out how long to make this message.  Don't know that students even need to see it.
                //Toast.makeText(this, R.string.network_unavailable_message,
                //Toast.LENGTH_LONG).show();

            }


            // We execute the call, and log to Logcat (presumably?) if successful, catch if exception and print to console

        }

        else{
            //Log.i(TAG,"Looks like we've already checked today and it's after 8:15a ===========> ");
        }

        //!! Log.i(TAG,"WE'RE RUNNING AFTER SHAREDPREFS GET LOADED ===========> ");
        //updateDisplay();


    }

    private void updateDisplay(){

        //!! Log.i(TAG,"BEGINNING TO UPDATE DISPLAY ===========> " + mSelectedDate + " " + mTodayDateInt);
        if (mSharedPreferences != null){
            mSharedPreferences = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
            if (!mSharedPreferences.getString(KEY_JSONSCHEDULEDATA,"").equals("") ){
                mCompactCalendarView.removeAllEvents();
                String jsonData = mSharedPreferences.getString(KEY_JSONSCHEDULEDATA, "");
                try {
                    mScheduleType = getCurrentDetails(jsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (!mSharedPreferences.getString(KEY_JSONPERIODSCHEDULEDATA,"").equals("") ){
                String periodScheduleData = mSharedPreferences.getString(KEY_JSONPERIODSCHEDULEDATA, "");
                try {
                    mPeriods = getPeriodSchedule(periodScheduleData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
        if (mPeriods!=null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSummaryLabel.setText(mScheduleType.getScheduleType());
                    PeriodAdapter adapter = new PeriodAdapter(mPeriods);
                    adapter.setDateNumber(mSelectedDateInt);
                    adapter.setTodayDateNumber(mTodayDateInt);
                    //!! Log.i(TAG,"UPDATED DISPLAY IN UPDATEDISPLAY METHOD ----------->" );
                    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                    onSwipeTouchListener = new OnSwipeTouchListener(MainActivity.this){
                        public void onSwipeTop() {
                            //Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();
                        }
                        public void onSwipeRight() {

                            mRecyclerView.startAnimation(AnimationUtils.loadAnimation(
                                    MainActivity.this,R.anim.push_right_in
                            ));
                            String yesterdayDateString = ((mSelectedDateInt -1) + "");
                            Date yesterdayDate = mSelectedDate;
                            try {
                                yesterdayDate = mYearMonthDayFormatter.parse(yesterdayDateString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            setSelectedDate(yesterdayDate);
                            mOldDateStringPreScroll = mSelectedDateTime.format("D MMMM YYYY", Locale.US);
                            //!! Log.i(TAG,"CLICKED LEFT ARROW ===========> ");

                            updateDisplay();
                        }
                        public void onSwipeLeft() {
                            //!! Log.i(TAG,"CLICKED RIGHT ARROW ===========> ");
                            mRecyclerView.startAnimation(AnimationUtils.loadAnimation(
                                    MainActivity.this,R.anim.push_left_in
                            ));
                            String tomorrowDateString = ((mSelectedDateInt + 1) + "");
                            Date tomorrowDate = mSelectedDate;
                            try {
                                tomorrowDate = mYearMonthDayFormatter.parse(tomorrowDateString);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            setSelectedDate(tomorrowDate);
                            mOldDateStringPreScroll = mSelectedDateTime.format("D MMMM YYYY", Locale.US);


                            updateDisplay();
                        }
                        public void onSwipeBottom() {
                            //Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT).show();
                        }
                    };
                    mRecyclerView.setOnTouchListener(onSwipeTouchListener);


                    // Weird error (probably my fault) that seems to cause relativelayout in recyclerview to increase in size by 1 pixel every time we refresh.
                    //mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),1);
                    //mRecyclerView.addItemDecoration(mDividerItemDecoration);


                    //mRecyclerView.setNestedScrollingEnabled(false);

                    // the Custom divider works great.  Built-in divider expanded for some reason.  Probably a bug but didn't want to look at it.
                    mRecyclerView.addItemDecoration(new CustomDividerItemDecoration(mRecyclerView.getContext()));
                    mRecyclerView.setAdapter(adapter);
                    mRecyclerView.setLayoutManager(layoutManager);


                }
            });
        }
    }



    private ScheduleType getCurrentDetails(String jsonData) throws JSONException {
        JSONObject calendarSchedule = new JSONObject(jsonData);
        ScheduleType scheduleType = new ScheduleType();
        boolean matchFound = false;

        /*
        First we get the JSONArray using schedule
        Then we iterate through the array using a for loop looking for today's date
                if we match date matchFound = true and setScheduleType to SUMMARY value for match
                if matchFound is false (after loop's end) setScheduleType to "NO SCHOOL"
        When we find it we use scheduleType to set scheduleType to our schedule type ("B-FLEX", etc).

        TODO:  I manually edited calendar data.  It might be better if I just use the data exactly as it comes from Google.  If so, changes will have to be made to the parser
         */
        //String scheduleSummary;

        // THE JSON ARRAY CONTAINING THE SCHEDULE (name in JSON is "VEVENT")
        JSONArray calendarScheduleArray = calendarSchedule.getJSONArray("VEVENT");

        // LOOP THROUGH THE ARRAY LOOKING FOR THE DATE WE WANT TO DISPLAY
        for (int i = 0; i < calendarScheduleArray.length(); i++) {

            // MAKE A JSONOBJECT FOR EACH DAY SO THAT WE CAN LOOK AT THE BELL SCHEDULE FOR THAT DAY
            JSONObject calendarScheduleDayObject = calendarScheduleArray.getJSONObject(i);

            // IF WE FIND THE DATE THAT WE'RE LOOKING FOR THEN GET THE "SUMMARY" WHICH IS THE SCHEDULE TYPE (A-FLEX, B, ETC)
            if (calendarScheduleDayObject.getString("DTSTART;VALUE=DATE").equals(mSelectedDateString)){
                scheduleType.setScheduleType(calendarScheduleDayObject.getString("SUMMARY"));
                matchFound = true;
            }

            // if there's a day off (SUMMARY = NO SCHOOL, CONFERENCES, SPRING BREAK, OR WINTER BREAK) draw a white circle around the date in the date picker

            if (calendarScheduleDayObject.getString("SUMMARY").equals("NO SCHOOL")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("CONFERENCES")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("SPRING BREAK")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("WINTER BREAK")){
                Date eventDate = new Date();
                String eventDateString = calendarScheduleDayObject.getString("DTSTART;VALUE=DATE");
                try {
                    eventDate = new SimpleDateFormat("yyyyMMdd").parse(eventDateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                mCompactCalendarView.addEvent(new Event(Color.WHITE,eventDate.getTime()), true);


            }

            // if there's an unusual schedule type (ACT, PSAT, RACEFORWARD, FINALS, EARLY DISMISSAL, LATE START, SPECIAL, etc)
            // draw a green circle around the date in the date picker
            if (calendarScheduleDayObject.getString("SUMMARY").equals("A-ACT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-ACT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("ACT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-ASSEMBLY")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-ASSEMBLY")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("ASSEMBLY")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("FIRST DAY")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("LAST DAY")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-RACEFORWARD")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-RACEFORWARD-2")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-RACEFORWARD-3")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-RACEFORWARD")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-RACEFORWARD-2")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-RACEFORWARD-3")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("RACEFORWARD")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("RACEFORWARD-1")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("RACEFORWARD-2")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("RACEFORWARD-3")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("RACEFORWARD-4")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-PSAT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-PSAT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("PSAT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-SAT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-SAT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("SAT")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-EARLY DISMISSAL")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-EARLY DISMISSAL")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-FLEX-EARLY DISMISSAL")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-FLEX-EARLY DISMISSAL")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("EARLY DISMISSAL")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-LATE START")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-LATE START")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-FLEX-LATE START")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-FLEX-LATE START")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("LATE START")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("FINALS-1")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("FINALS-2")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("FINALS-3")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("FINALS-4")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("ALL PERIODS")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("SPECIAL")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("SPECIAL-1")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("SPECIAL-2")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("SPECIAL-3")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("SPECIAL-4")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-SPECIAL-1")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-SPECIAL-2")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-SPECIAL-3")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("A-SPECIAL-4")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-SPECIAL-4")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-SPECIAL-1")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-SPECIAL-2")
                    || calendarScheduleDayObject.getString("SUMMARY").equals("B-SPECIAL-3")){
                Date eventDate = new Date();
                String eventDateString = calendarScheduleDayObject.getString("DTSTART;VALUE=DATE");
                try {
                    eventDate = new SimpleDateFormat("yyyyMMdd").parse(eventDateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                mCompactCalendarView.addEvent(new Event(Color.parseColor("#46f900"),eventDate.getTime()), true);


            }

        }

        // if there's no match with the schedule type the schedule type should be set to either:
        // NO SCHOOL (if it's a weekday) or
        // WEEKEND (if it's a Saturday or Sunday)

        if(!matchFound){
            if (mSelectedDateTime.getWeekDay() == 7 || mSelectedDateTime.getWeekDay() == 1 ){
                scheduleType.setScheduleType("WEEKEND");
            }
            else{
                scheduleType.setScheduleType(getString(R.string.no_school));
            }

        }



        return scheduleType;
    }
    /*
        We load the A/B schedule and the period schedule into shared prefs. We want to be able to change those schedules without an app update,
        so we check a version file to see if we need to update.

        The version file contains two fields, one for date and one for version. It looks something like this:

        DATE: 20161117
        VERSION: 01

        We do an update (return false) under the following conditions:

        1) The versionDate  in SharedPrefs is less than the date in the version file
        2) The versionVersion in SharedPrefs is less than the version in the version file

        If either one of the above conditions is true we update sharedPrefs with the latest versionDate and versionVersion before downloading the new A/B and period schedules.
        Then we return false.

        If neither are true we return true and no update is performed.
     */





    private boolean checkVersionUpdate(String jsonData) throws JSONException{
        boolean isMostRecent;
        JSONObject versionObject = new JSONObject(jsonData);
        int versionDateObject = Integer.parseInt(versionObject.getString("DATE"));  // date of the most recent version update
        int versionVersionObject = Integer.parseInt(versionObject.getString("VERSION")); // version number of most recent update
        //!! Log.i(TAG,"WHAT ARE WE PUTTING IN DATEVERSIONCHECK?  SHOULD BE TODAY'S DATE ===========> " + mTodayDateInt);
        mEditor.putInt(KEY_DATEVERSIONCHECK, mTodayDateInt);
        mEditor.apply();

        if (mSharedPreferences.getInt(KEY_DATEVERSION,0)==0 || versionDateObject > mSharedPreferences.getInt(KEY_DATEVERSION,0)){

            mEditor.putInt(KEY_DATEVERSION, versionDateObject);
            mEditor.putInt(KEY_VERSION, versionVersionObject);
            mEditor.apply();
            isMostRecent = false;
        }

        else if (versionDateObject == mSharedPreferences.getInt(KEY_DATEVERSION,0)){
            if (mSharedPreferences.getInt(KEY_VERSION,0)==0 || versionVersionObject > mSharedPreferences.getInt(KEY_VERSION,0)){
                mEditor.putInt(KEY_DATEVERSION, versionDateObject);
                mEditor.putInt(KEY_VERSION, versionVersionObject);
                mEditor.apply();
                //!! Log.i(TAG,"SERVER VERSION DATE SAME AS LOCAL, VERSION NUMBER GREATER ===========> "+ mSharedPreferences.getInt(KEY_DATEVERSION,0));
                isMostRecent = false;
            }
            //Server version date and version number both match
            else{
                isMostRecent=true;
            }
        }
        //server version date is less than the date in SharedPrefs (this shouldn't happen)
        else{
            isMostRecent=true;
        }
        return isMostRecent;

    }





    private Period[] getPeriodSchedule(String jsonData) throws JSONException {
        /* scheduleNames is a list of all valid schedule types (any type which appears (or could appear) in schoolYearSchedule.json and periodSchedule.json.
         *
         * I whitelist ScheduleTypes because the data is messy and hunting down all the outliers is more of a pain than it's worth. It also lets me declare any non-weekend
         * day without a schedule a "NO SCHOOL" day, which means I can gracefully handle summer break and any other vacation day that are not in the Google Calendar I import.
         *
         * Edit scheduleNames to match whatever schedule types you have in your calendar.  Anything not in scheduleNames will be considered "NO SCHOOL"
         * I've added a bunch of "SPECIAL" options so that I have flexibility in the case of one-off schedules without having to publish a new version of the app.
         *
         * I really should just loop through the periodSchedule file to populate the scheduleNames array.  I wish I'd thought of that 5 years ago.
         *
         */

/*        String[] scheduleNames = {"A","B","C","A-FLEX","B-FLEX","C-FLEX","A-RACEFORWARD","B-RACEFORWARD", "A-RACEFORWARD-2", "A-RACEFORWARD-3", "B-RACEFORWARD-2", "B-RACEFORWARD-3",
                "A-LATE START","B-LATE START","A-EARLY DISMISSAL", "B-EARLY DISMISSAL", "ACT","A-ACT","B-ACT","A-PSAT","B-PSAT","PSAT","FINALS-1","FINALS-2","FINALS-3","FINALS-4",
                "SPECIAL", "SPECIAL-1","SPECIAL-2", "SPECIAL-3","SPECIAL-4","A-SPECIAL-1","A-SPECIAL-2","A-SPECIAL-3","A-SPECIAL-4","B-SPECIAL-1","B-SPECIAL-2","B-SPECIAL-3","B-SPECIAL-4",
                "SKINNY", "ALL PERIODS", "LATE START", "EARLY DISMISSAL","A-FLEX-ASSEMBLY","B-FLEX-ASSEMBLY","ASSEMBLY","A-ASSEMBLY", "FIRST DAY","ASYNC", "A-RACEFORWARD-4","B-RACEFORWARD-4",
                "LAST DAY", "B-ASSEMBLY", "A-FLEX-LATE START", "B-FLEX-LATE START", "SAT", "A-SAT", "B-SAT","WHITE","BLUE","GRAY","WEDNESDAY","TUESDAY","MONDAY","THURSDAY","FRIDAY",
                "A-FLEX-EARLY DISMISSAL", "B-FLEX-EARLY-DISMISSAL", "RACEFORWARD","RACEFORWARD-1","RACEFORWARD-2","RACEFORWARD-3","RACEFORWARD-4"};*/
        JSONObject periodBells = new JSONObject(jsonData);

        // Loop through all of the schedule types defined in periodSchedule.json and store them in an array
        String[] scheduleNames = new String[periodBells.length()];
        Iterator<?> keys = periodBells.keys();
        int counter = 0;
        while(keys.hasNext()){
            scheduleNames[counter] = (String)keys.next();
            counter++;
        }

        boolean foundScheduleName = false;
        for (int i = 0; i < scheduleNames.length ; i++) {
            // If today's schedule type exists in periodSchedule.json and the type isn't "NO SCHOOL" then set foundSchedule to true so that we use it to create today's schedule
            // NO SCHOOL exists periodSchedule.json because the iPhone app crashed without it.  I'm not using it here.
            if (mScheduleType.getScheduleType().equals(scheduleNames[i]) && !scheduleNames[i].equals("NO SCHOOL")){
                foundScheduleName=true;
            }
        }
        Period[] periods;

        if (foundScheduleName){
            JSONArray periodScheduleArray = periodBells.getJSONArray(mScheduleType.getScheduleType());
            periods = new Period[periodScheduleArray.length()];


            for (int i = 0; i < periodScheduleArray.length(); i++) {
                JSONObject jsonPeriod = periodScheduleArray.getJSONObject((i));
                Period period = new Period();
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                if (!sp.getString("p1ClassName","NA").equals("NA") && !sp.getString("p1ClassName","NA").equals("") && jsonPeriod.getString("NAME").equals("P1")){
                    String p1Name = sp.getString("p1ClassName", "NA");
                    period.setPeriodName(p1Name);
                }

                else if (!sp.getString("p2ClassName","NA").equals("NA") && !sp.getString("p2ClassName","NA").equals("") && jsonPeriod.getString("NAME").equals("P2")){
                    String p2Name = sp.getString("p2ClassName", "NA");
                    period.setPeriodName(p2Name);
                }

                else if (!sp.getString("p3ClassName","NA").equals("NA") && !sp.getString("p3ClassName","NA").equals("") && jsonPeriod.getString("NAME").equals("P3")){
                    String p3Name = sp.getString("p3ClassName", "NA");
                    period.setPeriodName(p3Name);
                }

                else if (!sp.getString("p4ClassName","NA").equals("NA") && !sp.getString("p4ClassName","NA").equals("") && jsonPeriod.getString("NAME").equals("P4")){
                    String p4Name = sp.getString("p4ClassName", "NA");
                    period.setPeriodName(p4Name);
                }

                else if (!sp.getString("p5ClassName","NA").equals("NA") && !sp.getString("p5ClassName","NA").equals("") && jsonPeriod.getString("NAME").equals("P5")){
                    String p5Name = sp.getString("p5ClassName", "NA");
                    period.setPeriodName(p5Name);
                }

                else if (!sp.getString("p6ClassName","NA").equals("NA") && !sp.getString("p6ClassName","NA").equals("") && jsonPeriod.getString("NAME").equals("P6")){
                    String p6Name = sp.getString("p6ClassName", "NA");
                    period.setPeriodName(p6Name);
                }

                else if (!sp.getString("p7ClassName","NA").equals("NA") && !sp.getString("p7ClassName","NA").equals("") && jsonPeriod.getString("NAME").equals("P7")){
                    String p7Name = sp.getString("p7ClassName", "NA");
                    period.setPeriodName(p7Name);
                }

                else if (!sp.getString("p8ClassName","NA").equals("NA") && !sp.getString("p8ClassName","NA").equals("") && jsonPeriod.getString("NAME").equals("P8")){
                    String p8Name = sp.getString("p8ClassName", "NA");
                    period.setPeriodName(p8Name);
                }

                else{
                    period.setPeriodName(jsonPeriod.getString("NAME"));
                }

                period.setPeriodStart(jsonPeriod.getString("START"));
                period.setPeriodEnd(jsonPeriod.getString("END"));

                periods[i]=period;

            }
        }

        else{
            JSONArray periodScheduleArray = periodBells.getJSONArray("NO-SCHOOL");
            periods = new Period[periodScheduleArray.length()];

            for (int i = 0; i < periodScheduleArray.length(); i++) {
                JSONObject jsonPeriod = periodScheduleArray.getJSONObject((i));
                Period period = new Period();
                period.setPeriodName(jsonPeriod.getString("NAME"));
                period.setPeriodStart(jsonPeriod.getString("START"));
                period.setPeriodEnd(jsonPeriod.getString("END"));

                periods[i] = period;
            }

        }


        return periods;

    }






    // Check for internet connection before we try to get today's schedule
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isAvailable=false;
        // NetworkInfo has been deprectated in API 29
/*        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        //Log.d("GRRRR",networkInfo.isConnectedOrConnecting() + " connecting?: " + networkInfo.isConnected() + " is connected");
        if (networkInfo !=null && networkInfo.isConnected()){
            isAvailable = true;
        }

        ;*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = manager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = manager.getNetworkCapabilities(nw);
            if (actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))){
                isAvailable = true;
            }
        } else {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo !=null && networkInfo.isConnected()){
                isAvailable = true;
            }
        }
        return isAvailable;

    }

    // This method gets called when an item in the drawer (settings) gets selected.

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.period_settings) {
            Intent i = new Intent(this, MyPreferencesActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}

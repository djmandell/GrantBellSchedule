package com.grantcompsci.ghsbellschedule;

/**
 * Created by djmandell on 10/14/16.
 */
public class ScheduleType {
    private String mScheduleType;
    private Period[] mPeriodSchedule;


    public String getScheduleType() {
        return mScheduleType;
    }

    public void setScheduleType(String scheduleType) {
        mScheduleType = scheduleType;
    }

    public Period[] getPeriodSchedule() {
        return mPeriodSchedule;
    }

    public void setPeriodSchedule(Period[] periodSchedule) {
        mPeriodSchedule = periodSchedule;
    }


}

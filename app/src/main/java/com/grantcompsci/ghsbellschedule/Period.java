package com.grantcompsci.ghsbellschedule;

/**
 * Created by djmandell on 10/17/16.
 *
 *     Used to set the name of each period (as listed in periodSchedule.json on the server) and their start and end times.
 *     Periods can have any name.  Times are just strings and can therefore be in any format as well. I use (for grantcompsci):
 *
 *     01:38pm
 *
 *     TODO: Maybe want to be able to add assemblies somehow, assuming they're part of the data we receive.
 *
 */
public class Period {


    private String mPeriodName;
    private String mPeriodStart;
    private String mPeriodEnd;


    public String getPeriodName() {

        return mPeriodName;
    }

    public void setPeriodName(String periodName) {
        mPeriodName = periodName;
    }

    public String getPeriodStart() {
        return mPeriodStart;
    }

    public void setPeriodStart(String periodStart) {
        mPeriodStart = periodStart;
    }

    public String getPeriodEnd() {
        return mPeriodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        mPeriodEnd = periodEnd;
    }



}

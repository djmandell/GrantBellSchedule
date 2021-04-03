package com.grantcompsci.ghsbellschedule;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

/*
 *
 * Used for replacing the generic period number with the actual class names for each period (if desired).
 *
 */

public class PeriodNameSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        setEditTextFields();
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setEditTextFields();
    }

    public void setEditTextFields(){
        SharedPreferences sp =  getPreferenceScreen().getSharedPreferences();
        String[] savedStrings = {sp.getString("p1ClassName",null),sp.getString("p2ClassName",null),sp.getString("p3ClassName",null),sp.getString("p4ClassName",null),
                sp.getString("p5ClassName",null), sp.getString("p6ClassName",null),sp.getString("p7ClassName",null),sp.getString("p8ClassName",null)};


        EditTextPreference[] etp = {(EditTextPreference)findPreference("p1ClassName"), (EditTextPreference) findPreference("p2ClassName"),(EditTextPreference) findPreference("p3ClassName"),
                (EditTextPreference) findPreference("p4ClassName"),(EditTextPreference) findPreference("p5ClassName"),(EditTextPreference) findPreference("p6ClassName"),
                (EditTextPreference) findPreference("p7ClassName"),(EditTextPreference) findPreference("p8ClassName")};
        for (int i = 0; i < savedStrings.length; i++) {
            // If the class name has preveiously been saved, display the class name.
            if(savedStrings[i]!=null && !savedStrings[i].isEmpty()){
                etp[i].setSummary(etp[i].getText());
            }
            // If there's nothing saved to the prefernce, display a message that lets students know they can enter a class name here.
            else{
                etp[i].setSummary("Enter your class name for period " + (i+1));
            }
        }

    }

}

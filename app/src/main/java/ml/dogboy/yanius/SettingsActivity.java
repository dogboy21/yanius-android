package ml.dogboy.yanius;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("pref_darkmode", false)) {
            this.getTheme().applyStyle(R.style.AppTheme_Dark, true);
        }

        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
    }

}

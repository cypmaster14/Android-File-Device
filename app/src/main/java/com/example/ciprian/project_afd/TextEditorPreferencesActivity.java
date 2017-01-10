package com.example.ciprian.project_afd;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Ciprian on 10/01/2017.
 */

public class TextEditorPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}

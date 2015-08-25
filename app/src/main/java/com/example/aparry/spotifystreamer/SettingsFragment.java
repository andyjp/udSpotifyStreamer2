package com.example.aparry.spotifystreamer;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // http://stackoverflow.com/questions/9760341/retrieve-a-list-of-countries-from-the-android-os
        // http://stackoverflow.com/questions/7063831/android-how-to-populate-a-charsequence-array-dynamically-not-initializing

        List<Locale> countries = new ArrayList<Locale>();
        List<String> countryEntries = new ArrayList<>();
        List<String> countryValues = new ArrayList<>();

        // Get available locales
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            countries.add(locale);
        }

        // Alphabetize by display country
        Collections.sort(countries, new Comparator<Locale>() {
            @Override
            public int compare(Locale lhs, Locale rhs) {
                return lhs.getDisplayCountry().compareTo(rhs.getDisplayCountry());
            }
        });

        // Set country entries and country values
        for (Locale locale : countries) {
            String country = locale.getDisplayCountry();
            if (country.trim().length() > 0 && !countryEntries.contains(country)) {
                countryEntries.add(country);
                countryValues.add(locale.getCountry());
            }
        }

        ListPreference countryPref = (ListPreference) findPreference("pref_country");
        countryPref.setEntries(countryEntries.toArray(new CharSequence[countryEntries.size()]));
        countryPref.setEntryValues(countryValues.toArray(new CharSequence[countryValues.size()]));
        countryPref.setDefaultValue(Locale.getDefault().getCountry());
    }
}

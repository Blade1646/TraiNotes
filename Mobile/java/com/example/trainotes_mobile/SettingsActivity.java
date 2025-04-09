package com.example.trainotes_mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.trainotes_mobile.other.ActivityUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private ListView generalListView;
    private ListView otherListView;

    private ArrayList<HashMap<String,String>> generalArrayList;
    private ArrayList<HashMap<String,String>> otherArrayList;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.setAppLocale(this);
        setContentView(R.layout.activity_settings);

        preferences = PreferenceManager
                .getDefaultSharedPreferences(SettingsActivity.this);
        SharedPreferences.Editor editor = preferences.edit();

        generalListView = findViewById(R.id.generalListView);
        otherListView = findViewById(R.id.otherListView);

        generalArrayList = new ArrayList<HashMap<String,String>>();
        otherArrayList = new ArrayList<HashMap<String,String>>();

        loadArrayLists();

        SimpleAdapter generalAdapter = new SimpleAdapter(this, generalArrayList,
                R.layout.settings_point,
                new String[] { "generalLine1","generalLine2" },
                new int[] {R.id.itemTextView, R.id.subItemTextView});
        SimpleAdapter otherAdapter = new SimpleAdapter(this, otherArrayList,
                R.layout.settings_point,
                new String[] { "otherLine1","otherLine2" },
                new int[] {R.id.itemTextView, R.id.subItemTextView});
        generalListView.setAdapter(generalAdapter);
        otherListView.setAdapter(otherAdapter);

        generalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selectedItem = generalArrayList.get(position);
                String selectedLine1 = selectedItem.get("generalLine1");
                if (selectedLine1 == getString(R.string.language))
                {
                    final Dialog dialog = new Dialog(SettingsActivity.this);
                    dialog.setContentView(R.layout.dialog_language);
                    Button englishLanguageButton = dialog.findViewById(R.id.englishLanguageButton);
                    Button ukrainianLanguageButton = dialog.findViewById(R.id.ukrainianLanguageButton);
                    Button cancelLanguageButton = dialog.findViewById(R.id.cancelLanguageButton);
                    boolean isDefaultLanguage = preferences.getBoolean("isDefaultLanguage", true);
                    ( isDefaultLanguage ? englishLanguageButton : ukrainianLanguageButton )
                            .setText(getString(R.string.circle_checked));

                    englishLanguageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isDefaultLanguage) {
                                editor.putBoolean("isDefaultLanguage", true);
                                editor.apply();
                                ActivityUtil.restartActivity(SettingsActivity.this);
                            }
                            dialog.dismiss();
                        }
                    });

                    ukrainianLanguageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isDefaultLanguage) {
                                editor.putBoolean("isDefaultLanguage", false);
                                editor.apply();
                                ActivityUtil.restartActivity(SettingsActivity.this);
                            }
                            dialog.dismiss();
                        }
                    });

                    cancelLanguageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
                else if (selectedLine1 == getString(R.string.unit_system))
                {
                    final Dialog dialog = new Dialog(SettingsActivity.this);
                    dialog.setContentView(R.layout.dialog_unit_system);
                    Button imperialUnitSystemButton = dialog.findViewById(R.id.imperialUnitSystemButton);
                    Button metricUnitSystemButton = dialog.findViewById(R.id.metricUnitSystemButton);
                    Button cancelUnitSystemButton = dialog.findViewById(R.id.cancelUnitSystemButton);
                    boolean isDefaultUnitSystem = preferences.getBoolean("isDefaultUnitSystem", true);
                    ( isDefaultUnitSystem ? imperialUnitSystemButton : metricUnitSystemButton )
                            .setText(getString(R.string.circle_checked));

                    imperialUnitSystemButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isDefaultUnitSystem) {
                                editor.putBoolean("isDefaultUnitSystem", true);
                                editor.apply();
                                loadArrayLists();
                                generalAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        }
                    });

                    metricUnitSystemButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isDefaultUnitSystem) {
                                editor.putBoolean("isDefaultUnitSystem", false);
                                editor.apply();
                                loadArrayLists();
                                generalAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        }
                    });

                    cancelUnitSystemButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
                else if (selectedLine1 == getString(R.string.date_format))
                {
                    final Dialog dialog = new Dialog(SettingsActivity.this);
                    dialog.setContentView(R.layout.dialog_date_format);
                    Button americanDateFormatButton = dialog.findViewById(R.id.americanDateFormatButton);
                    Button europeanDateFormatButton = dialog.findViewById(R.id.europeanDateFormatButton);
                    Button cancelDateFormatButton = dialog.findViewById(R.id.cancelDateFormatButton);
                    boolean isDefaultDateFormat = preferences.getBoolean("isDefaultDateFormat", true);
                    ( isDefaultDateFormat ? americanDateFormatButton : europeanDateFormatButton )
                            .setText(getString(R.string.circle_checked));

                    americanDateFormatButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isDefaultDateFormat) {
                                editor.putBoolean("isDefaultDateFormat", true);
                                editor.apply();
                                loadArrayLists();
                                generalAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        }
                    });

                    europeanDateFormatButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isDefaultDateFormat) {
                                editor.putBoolean("isDefaultDateFormat", false);
                                editor.apply();
                                loadArrayLists();
                                generalAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        }
                    });

                    cancelDateFormatButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
                else if (selectedLine1 == getString(R.string.week_start))
                {
                    final Dialog dialog = new Dialog(SettingsActivity.this);
                    dialog.setContentView(R.layout.dialog_week_start);
                    Button sundayWeekStartButton = dialog.findViewById(R.id.sundayWeekStartButton);
                    Button mondayWeekStartButton = dialog.findViewById(R.id.mondayWeekStartButton);
                    Button cancelWeekStartButton = dialog.findViewById(R.id.cancelWeekStartButton);
                    boolean isDefaultWeekStart = preferences.getBoolean("isDefaultWeekStart", true);
                    ( isDefaultWeekStart ? sundayWeekStartButton : mondayWeekStartButton )
                            .setText(getString(R.string.circle_checked));

                    sundayWeekStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!isDefaultWeekStart) {
                                editor.putBoolean("isDefaultWeekStart", true);
                                editor.apply();
                                loadArrayLists();
                                generalAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        }
                    });

                    mondayWeekStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isDefaultWeekStart) {
                                editor.putBoolean("isDefaultWeekStart", false);
                                editor.apply();
                                loadArrayLists();
                                generalAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        }
                    });

                    cancelWeekStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            }
        });

        otherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> selectedItem = otherArrayList.get(position);
                String selectedLine1 = selectedItem.get("otherLine1");
                if (selectedLine1 == getString(R.string.help))
                {
                    String url = "https://www.trainotes.com/help";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
                else if (selectedLine1 == getString(R.string.feedback))
                {
                    String url = "https://www.trainotes.com/feedback";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
                else if (selectedLine1 == getString(R.string.privacy_policy))
                {
                    String url = "https://www.trainotes.com/privacy";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
        });
    }

    private void loadArrayLists() {
        String[][] generalListElements =
                {{getString(R.string.language), ( preferences.getBoolean("isDefaultLanguage", true) ) ?
                        getString(R.string.english) : getString(R.string.ukrainian)},
                        {getString(R.string.unit_system), ( preferences.getBoolean("isDefaultUnitSystem", true) ) ?
                                getString(R.string.imperial) : getString(R.string.metric)},
                        {getString(R.string.date_format), ( preferences.getBoolean("isDefaultDateFormat", true) ) ?
                                getString(R.string.mm_dd_yyyy) : getString(R.string.dd_mm_yyyy)},
                        {getString(R.string.week_start), ( preferences.getBoolean("isDefaultWeekStart", true) ) ?
                                getString(R.string.sunday_full) : getString(R.string.monday_full)}};
        String[][] otherListElements = {{getString(R.string.help), getString(R.string.help_value)},
                {getString(R.string.feedback), getString(R.string.feedback_value)},
                {getString(R.string.privacy_policy), getString(R.string.privacy_policy_value)}};

        generalArrayList.clear();
        otherArrayList.clear();

        for(int i = 0; i < generalListElements.length; i++){
            HashMap<String,String> el = new HashMap<String,String>();
            el.put( "generalLine1", generalListElements[i][0]);
            el.put( "generalLine2", generalListElements[i][1]);
            generalArrayList.add( el );
        }
        for(int i = 0; i < otherListElements.length; i++){
            HashMap<String,String> el = new HashMap<String,String>();
            el.put( "otherLine1", otherListElements[i][0]);
            el.put( "otherLine2", otherListElements[i][1]);
            otherArrayList.add( el );
        }
    }

    public void onSettingsButtonClicked(View view) { ActivityUtil.restartActivities(this); }
}
package com.example.trainotes_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.trainotes_mobile.other.ActivityUtil;
import com.example.trainotes_mobile.other.MySingleton;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        CalendarAdapter.OnItemListener, TrainingDayAdapter.OnItemListener {

    private ViewSwitcher viewSwitcher;
    private ViewSwitcher daysOfWeekSwitcher;
    private ViewSwitcher iconSwitcher;
    private ImageButton plusButton;
    private TextView dateTextView;
    private RecyclerView dayRecyclerView;
    private RecyclerView monthRecyclerView;
    private TextView emptyTextView;

    private ArrayList<Integer> exercisesIDsArray;

    public static LocalDate currentDate;
    public static LocalDate selectedDate;

    private SharedPreferences preferences;
    private boolean isDefaultUnitSystem;
    private static boolean isDailyMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.setAppLocale(this);
        setContentView(R.layout.activity_main);

        viewSwitcher = findViewById(R.id.viewSwitcher);
        daysOfWeekSwitcher = findViewById(R.id.daysOfWeekSwitcher);
        iconSwitcher = findViewById(R.id.iconSwitcher);
        plusButton = findViewById(R.id.plusButton);
        dateTextView = findViewById(R.id.dateTextView);
        dayRecyclerView = findViewById(R.id.dayRecyclerView);
        monthRecyclerView = findViewById(R.id.monthRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);

        selectedDate = (getIntent().getStringExtra("selectedDate") == null ? LocalDate.now() :
                LocalDate.parse(getIntent().getStringExtra("selectedDate"), DateTimeFormatter.ISO_LOCAL_DATE));
        currentDate = selectedDate;

        preferences = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);
        isDefaultUnitSystem = preferences.getBoolean("isDefaultUnitSystem", true);

        load();
    }

    public void onCalendarButtonClicked(View view) {
        toggleToMonthMode();
    }

    public void onHandButtonClicked(View view) {
        toggleToDayMode();
    }

    public void onThreeDotsButtonClicked(View view) {
        showPopupMenu(view);
    }

    public void onPlusButtonClicked(View view) {
        Intent exercisesListIntent = new Intent(MainActivity.this, ExercisesListActivity.class);
        startActivity(exercisesListIntent);
    }

    public void onPreviousButtonClicked(View view) {
        if (isDailyMode) {
            selectedDate = selectedDate.minusDays(1);
            load();
        }
        else {
            currentDate = currentDate.minusMonths(1);
            setMonthView();
        }
    }

    public void onNextButtonClicked(View view) {
        if (isDailyMode) {
            selectedDate = selectedDate.plusDays(1);
            load();
        }
        else {
            currentDate = currentDate.plusMonths(1);
            setMonthView();
        }
    }

    private void load() {
        String url = getResources().getString(R.string.api_route) + "approaches/getByTrainingDay?trainingDay=" + selectedDate +
                "&userId=" + preferences.getInt("userID", -1);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        setDayView(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;

                            if (statusCode == 404) {
                                Toast.makeText(MainActivity.this,
                                        getResources().getString(R.string.error_user), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                            }
                        }
                        else Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                    }
                });

        MySingleton.getInstance(MainActivity.this).addToRequestQueue(request);
    }

    private void toggleToMonthMode() {
        isDailyMode = false;
        iconSwitcher.showNext();
        viewSwitcher.showNext();
        plusButton.setVisibility(View.GONE);
        currentDate = selectedDate;
        setMonthView();
    }

    private void toggleToDayMode() {
        isDailyMode = true;
        iconSwitcher.showPrevious();
        viewSwitcher.showPrevious();
        plusButton.setVisibility(View.VISIBLE);
        load();
    }

    private void setMonthView() {
        dateTextView.setText(getMonthYear(currentDate));
        if (preferences.getBoolean("isDefaultWeekStart", true)) daysOfWeekSwitcher.setDisplayedChild(0);
        else daysOfWeekSwitcher.setDisplayedChild(1);
        ArrayList<LocalDate> daysArray = getDaysArray(currentDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysArray, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        monthRecyclerView.setLayoutManager(layoutManager);
        monthRecyclerView.setAdapter(calendarAdapter);
    }

    private void setDayView(JSONArray exercisesJSONArray) {
        dateTextView.setText(getFullDate(selectedDate));

        if (exercisesJSONArray.length() == 0) emptyTextView.setVisibility(View.VISIBLE);
        else emptyTextView.setVisibility(View.GONE);

        exercisesIDsArray = new ArrayList<>();
        ArrayList<String> exercisesNamesArray = new ArrayList<>();
        HashMap<Integer, ArrayList<HashMap<String, String>>> exercisesHashMap = new HashMap<>();

        for (int outerI = 0; outerI < exercisesJSONArray.length(); outerI++) {
            try {
                exercisesIDsArray.add(exercisesJSONArray.getJSONObject(outerI).getInt("id"));
                exercisesNamesArray.add(exercisesJSONArray.getJSONObject(outerI).getString("name"));

                ArrayList<HashMap<String, String>> approachArrayList = new ArrayList<>();
                JSONArray approachJSONArray = exercisesJSONArray.getJSONObject(outerI).getJSONArray("approaches");
                for (int innerI = 0; innerI < approachJSONArray.length(); innerI++) {
                    int finalInnerI = innerI;
                    HashMap<String, String> el = new HashMap<String, String>() {{
                        put( "weight", (isDefaultUnitSystem ? approachJSONArray.getJSONObject(finalInnerI).getString("weight") :
                                ActivityUtil.imperialToMetric(approachJSONArray.getJSONObject(finalInnerI).getString("weight"))));
                        put("reps", approachJSONArray.getJSONObject(finalInnerI).getString("reps"));
                    }};
                    approachArrayList.add(el);
                }

                exercisesHashMap.put(exercisesIDsArray.get(outerI), approachArrayList);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        TrainingDayAdapter trainingDayAdapter = new TrainingDayAdapter(isDefaultUnitSystem,
                exercisesIDsArray, exercisesNamesArray, exercisesHashMap, this);
        dayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dayRecyclerView.setAdapter(trainingDayAdapter);
    }

    private void showPopupMenu (View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.settings_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_profile:
                        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(profileIntent);
                        return true;
                    case R.id.menu_settings:
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        return true;
                    case R.id.menu_details:
                        Intent detailsIntent = new Intent(MainActivity.this, DetailsActivity.class);
                        startActivity(detailsIntent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    private String getMonthYear(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy",
                (preferences.getBoolean("isDefaultLanguage", true) ? Locale.ENGLISH : new Locale("uk", "UA")));
        return date.format(formatter);
    }

    private String getFullDate(LocalDate date) {
        String fullDate;
        String day;
        String month;

        if (date.equals(LocalDate.now())) day = getString(R.string.today);
        else if (date.equals(LocalDate.now().minusDays(1))) day = getString(R.string.yesterday);
        else if (date.equals(LocalDate.now().plusDays(1))) day = getString(R.string.tomorrow);
        else day = date.format(DateTimeFormatter.ofPattern("E",
        (preferences.getBoolean("isDefaultLanguage", true) ? Locale.ENGLISH : new Locale("uk", "UA"))));

        month = date.format(DateTimeFormatter.ofPattern("MMMM",
                (preferences.getBoolean("isDefaultLanguage", true) ? Locale.ENGLISH : new Locale("uk", "UA"))));

        fullDate = day + ", " + (preferences.getBoolean("isDefaultDateFormat", true) ?
                month + " " + date.getDayOfMonth() : date.getDayOfMonth() + " " + month)
                + (date.getYear() == LocalDate.now().getYear() ? "" : " " + date.getYear());
        return fullDate;
    }

    private ArrayList<LocalDate> getDaysArray(LocalDate date) {
        ArrayList<LocalDate> daysArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysAmount = yearMonth.lengthOfMonth();
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);

        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() -
                (preferences.getBoolean("isDefaultWeekStart", true) ? 0 : 1);
        LocalDate prevMonth = date.minusMonths(1);
        LocalDate nextMonth = date.plusMonths(1);
        YearMonth prevYearMonth = YearMonth.from(prevMonth);
        int prevDaysInMonth = prevYearMonth.lengthOfMonth();

        for(int i = 1; i <= 42; i++)
        {
            if(i <= dayOfWeek)
                daysArray.add(LocalDate.of(prevMonth.getYear(),prevMonth.getMonth(), prevDaysInMonth + i - dayOfWeek));
            else if(i > daysAmount + dayOfWeek)
                daysArray.add(LocalDate.of(nextMonth.getYear(),nextMonth.getMonth(),i - dayOfWeek - daysAmount));
            else
                daysArray.add(LocalDate.of(date.getYear(),date.getMonth(),i - dayOfWeek));
        }

        return daysArray;
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if (date.equals(selectedDate)) toggleToDayMode();
        else {
            selectedDate = currentDate = date;
            setMonthView();
        }
    }

    @Override
    public void onItemClick(int position) {
        Intent exerciseIntent = new Intent(MainActivity.this, ExerciseActivity.class);
        exerciseIntent.putExtra("exerciseID", exercisesIDsArray.get(position).toString());
        startActivity(exerciseIntent);
    }
}
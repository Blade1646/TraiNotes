package com.example.trainotes_mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.trainotes_mobile.other.ActivityUtil;
import com.example.trainotes_mobile.other.MySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class ExerciseActivity extends AppCompatActivity {

    private TextView exerciseTextView;
    private TextView weightTextView;
    private EditText weightEditText;
    private EditText repsEditText;
    private Button saveExerciseButton;
    private Button clearExerciseButton;
    private ListView exerciseListView;
    private ViewSwitcher favouriteButtonSwitcher;

    private ArrayList<HashMap<String,String>> exerciseArrayList;

    private boolean firstLoad;
    private String exerciseID;
    private boolean isDefaultUnitSystem;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.setAppLocale(this);
        setContentView(R.layout.activity_exercise);

        exerciseTextView = findViewById(R.id.exerciseTextView);
        favouriteButtonSwitcher = findViewById(R.id.favouriteButtonSwitcher);
        weightTextView = findViewById(R.id.weightTextView);
        weightEditText = findViewById(R.id.weightEditText);
        repsEditText = findViewById(R.id.repsEditText);
        saveExerciseButton = findViewById(R.id.saveExerciseButton);
        clearExerciseButton = findViewById(R.id.clearExerciseButton);
        exerciseListView = findViewById(R.id.exerciseListView);

        exerciseID = getIntent().getStringExtra("exerciseID");
        firstLoad = true;

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(ExerciseActivity.this);
        isDefaultUnitSystem = preferences.getBoolean("isDefaultUnitSystem", true);
        weightTextView.setText((isDefaultUnitSystem ? getString(R.string.imperial_weight) : getString(R.string.metric_weight)));

        load();
    }

    public void onExerciseButtonClicked(View view) { ActivityUtil.restartActivities(this); }

    public void onNotFavouriteButtonClicked(View view) {
        setFavourite(true);
    }

    public void onFavouriteButtonClicked(View view) {
        setFavourite(false);
    }

    private void setFavourite (boolean favourite) {
        String url = getResources().getString(R.string.api_route) + "exercises/edit/favourite?id=" +
                exerciseID + "&value=" + favourite;
        StringRequest request = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        load();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;

                            if (statusCode == 404) {
                                Toast.makeText(ExerciseActivity.this,
                                        getResources().getString(R.string.no_exercise), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ExerciseActivity.this,
                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        MySingleton.getInstance(ExerciseActivity.this).addToRequestQueue(request);
    }

    public void onMinusWeightButtonClicked(View view) {
        if (weightEditText.getText().toString().equals("")) weightEditText.setText("0.0");
        float weightEditTextValue = Float.parseFloat(weightEditText.getText().toString());
        if (weightEditTextValue < 5) weightEditText.setText("0.0");
        else weightEditText.setText(Float.toString(weightEditTextValue - 5));
    }

    public void onPlusWeightButtonClicked(View view) {
        if (weightEditText.getText().toString().equals("")) weightEditText.setText("5.0");
        else weightEditText.setText(Float.toString(Float.parseFloat(weightEditText.getText().toString()) + 5));
    }

    public void onMinusRepsButtonClicked(View view) {
        if (repsEditText.getText().toString().equals("")) repsEditText.setText("0");
        int repsEditTextValue = Integer.parseInt(repsEditText.getText().toString());
        if (repsEditTextValue < 1) repsEditText.setText("0");
        else repsEditText.setText(Integer.toString(repsEditTextValue - 1));
    }

    public void onPlusRepsButtonClicked(View view) {
        if (repsEditText.getText().toString().equals("")) repsEditText.setText("1");
        else repsEditText.setText(Integer.toString(Integer.parseInt(repsEditText.getText().toString()) + 1));
    }

    public void onSaveExerciseButtonClicked(View view) {
        if (selectedPosition == -1) {
            if (repsEditText.getText().toString().equals("") || weightEditText.getText().toString().equals(""))
                Toast.makeText(this, getResources().getString(R.string.enter_values), Toast.LENGTH_SHORT).show();
            else {
                String url = getResources().getString(R.string.api_route) + "approaches/add?weight=" +
                        (isDefaultUnitSystem ? weightEditText.getText().toString() :
                                ActivityUtil.metricToImperial(weightEditText.getText().toString()))
                        + "&reps=" + repsEditText.getText().toString() + "&trainingDayDay=" +
                        MainActivity.selectedDate + "&exerciseId=" + exerciseID;

                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                load();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null) {
                                    Toast.makeText(ExerciseActivity.this,
                                            getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                MySingleton.getInstance(ExerciseActivity.this).addToRequestQueue(request);
            }
        }
        else {
            String url = getResources().getString(R.string.api_route) + "approaches/edit?id=" +
                    exerciseArrayList.get(selectedPosition).get("id") + "&weight=" +
                    (isDefaultUnitSystem ? weightEditText.getText().toString() :
                            ActivityUtil.metricToImperial(weightEditText.getText().toString()))
                    + "&reps=" + repsEditText.getText().toString();

            StringRequest request = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            onItemUnselected();
                            load();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;

                                if (statusCode == 409) {
                                    Toast.makeText(ExerciseActivity.this,
                                            getResources().getString(R.string.no_approach), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ExerciseActivity.this,
                                            getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

            MySingleton.getInstance(ExerciseActivity.this).addToRequestQueue(request);
        }
    }

    public void onClearExerciseButtonClicked(View view) {
        if (selectedPosition == -1) {
            weightEditText.setText("");
            repsEditText.setText("");
        }
        else openCategoryExerciseDeleteDialog();
    }

    private void load() {
        String url = getResources().getString(R.string.api_route) + "exercises/getByTrainingDay?id=" + exerciseID +
                "&trainingDay=" + MainActivity.selectedDate;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String exerciseName = response.getString("name");
                            String exerciseNotes = (response.getString("notes") == "null" ?
                                    "" : response.getString("notes"));
                            boolean exerciseFavourite = response.getBoolean("favourite");
                            handleExerciseList(response.getJSONArray("approaches"));

                            exerciseTextView.setText(exerciseName);
                            exerciseTextView.setTooltipText(exerciseNotes);
                            favouriteButtonSwitcher.setDisplayedChild(exerciseFavourite ? 1 : 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;

                            if (statusCode == 404) {
                                Toast.makeText(ExerciseActivity.this,
                                        getResources().getString(R.string.no_exercise), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ExerciseActivity.this,
                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        MySingleton.getInstance(ExerciseActivity.this).addToRequestQueue(request);
    }

    private void handleExerciseList(JSONArray approachesJSONArray) {
        if (approachesJSONArray.length() == 0) return;

        exerciseArrayList = new ArrayList<HashMap<String,String>>();
        for (int i = 0; i < approachesJSONArray.length(); i++) {
            HashMap<String,String> el = new HashMap<String,String>();
            try {
                el.put("id", approachesJSONArray.getJSONObject(i).getString("id"));
                el.put( "weight", (isDefaultUnitSystem ? approachesJSONArray.getJSONObject(i).getString("weight") :
                        ActivityUtil.imperialToMetric(approachesJSONArray.getJSONObject(i).getString("weight"))));
                el.put( "reps", approachesJSONArray.getJSONObject(i).getString("reps"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            exerciseArrayList.add( el );
        }

        if (firstLoad == true) {
            weightEditText.setText(exerciseArrayList.get(exerciseArrayList.size() - 1).get("weight"));
            repsEditText.setText(exerciseArrayList.get(exerciseArrayList.size() - 1).get("reps"));
            firstLoad = false;
        }

        SimpleAdapter exerciseAdapter = new SimpleAdapter(this, exerciseArrayList,
                R.layout.exercise_approach,
                new String[] { "number", "weight", "reps" },
                new int[] {R.id.exerciseNumberTextView , R.id.exerciseWeightTextView, R.id.exerciseRepsTextView}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView exerciseWeightHintTextView = view.findViewById(R.id.exerciseWeightHintTextView);
                TextView exerciseNumberTextView = view.findViewById(R.id.exerciseNumberTextView);

                exerciseWeightHintTextView.setText(isDefaultUnitSystem ? getString(R.string.lbs_append) : getString(R.string.kgs_append));
                exerciseNumberTextView.setText(Integer.toString(position + 1));

                if (position == selectedPosition)
                    view.setBackgroundColor(ContextCompat.getColor(ExerciseActivity.this, R.color.blue));
                else view.setBackgroundColor(ContextCompat.getColor(ExerciseActivity.this, android.R.color.transparent));

                return view;
            }
        };

        exerciseListView.setAdapter(exerciseAdapter);

        exerciseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemSelected(view, position);
                exerciseAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onItemSelected(View view, int position) {
        TextView exerciseWeightTextView = view.findViewById(R.id.exerciseWeightTextView);
        TextView exerciseRepsTextView = view.findViewById(R.id.exerciseRepsTextView);

        selectedPosition = position;
        saveExerciseButton.setText(getString(R.string.edit));
        clearExerciseButton.setText(getString(R.string.delete));
        weightEditText.setText(exerciseWeightTextView.getText());
        repsEditText.setText(exerciseRepsTextView.getText());
    }

    private void onItemUnselected () {
        selectedPosition = -1;
        saveExerciseButton.setText(getString(R.string.save));
        clearExerciseButton.setText(getString(R.string.clear));
        weightEditText.setText("");
        repsEditText.setText("");
    }

    private void openCategoryExerciseDeleteDialog() {
        final Dialog dialog = new Dialog(ExerciseActivity.this);
        dialog.setContentView(R.layout.dialog_delete);

        TextView deleteCategoryExerciseTextView = dialog.findViewById(R.id.deleteCategoryExerciseTextView);
        TextView deleteCategoryExerciseNameTextView = dialog.findViewById(R.id.deleteCategoryExerciseNameTextView);
        Button acceptCategoryExerciseDelete = dialog.findViewById(R.id.acceptCategoryExerciseDelete);
        Button cancelCategoryExerciseDelete = dialog.findViewById(R.id.cancelCategoryExerciseDelete);

        deleteCategoryExerciseTextView.setText(getString(R.string.delete_approach));

        SpannableStringBuilder sb = new SpannableStringBuilder(getString(R.string.delete_selected_approach));
        int startIndex = sb.toString().indexOf(getString(R.string.selected));
        int endIndex = startIndex + getString(R.string.selected).length();
        sb.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        startIndex = sb.toString().indexOf(getString(R.string.permanently));
        endIndex = startIndex + getString(R.string.permanently).length();
        sb.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        deleteCategoryExerciseNameTextView.setText(sb);

        acceptCategoryExerciseDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getResources().getString(R.string.api_route) + "approaches/delete?id=" +
                        exerciseArrayList.get(selectedPosition).get("id");
                StringRequest request = new StringRequest(Request.Method.DELETE, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                onItemUnselected();
                                load();
                                dialog.dismiss();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null) {
                                    int statusCode = error.networkResponse.statusCode;

                                    if (statusCode == 404) {
                                        Toast.makeText(ExerciseActivity.this,
                                                getResources().getString(R.string.no_approach), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ExerciseActivity.this,
                                                getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                MySingleton.getInstance(ExerciseActivity.this).addToRequestQueue(request);
            }
        });

        cancelCategoryExerciseDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
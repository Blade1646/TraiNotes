package com.example.trainotes_mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.trainotes_mobile.other.ActivityUtil;
import com.example.trainotes_mobile.other.MySingleton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class ExercisesListActivity extends AppCompatActivity {

    private TextView categoriesExercisesTextView;
    private TextView emptyCategoriesTextView;
    private TextView emptyExercisesTextView;

    private ViewSwitcher exercisesViewSwitcher;
    private ListView categoriesListView;
    private ListView exercisesListView;

    private ArrayList<HashMap<String,String>> categoriesArrayList;
    private ArrayList<HashMap<String,String>> exercisesArrayList;
    private SimpleAdapter categoriesAdapter;
    private SimpleAdapter exercisesAdapter;

    private SharedPreferences preferences;
    private boolean isCategoryMode = true;
    private int selectedCategoryPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.setAppLocale(this);
        setContentView(R.layout.activity_exercises_list);

        categoriesExercisesTextView = findViewById(R.id.categoriesExercisesTextView);
        emptyCategoriesTextView = findViewById(R.id.emptyCategoriesTextView);
        emptyExercisesTextView = findViewById(R.id.emptyExercisesTextView);
        exercisesViewSwitcher = findViewById(R.id.exercisesViewSwitcher);
        categoriesListView = findViewById(R.id.categoriesListView);
        exercisesListView = findViewById(R.id.exercisesListView);

        preferences = PreferenceManager
                .getDefaultSharedPreferences(ExercisesListActivity.this);

        handleCategoriesList();
    }

    public void onExercisesButtonClicked(View view) {
        if (isCategoryMode) {
            ActivityUtil.restartActivities(this);
        } else {
            toggleToCategoryMode();
        }
    }

    public void onPlusExerciseCategoriesButtonClicked(View view) {
        if (isCategoryMode) {
            openCategoryDialog(true, -1);
        } else {
            openExerciseDialog(true, -1);
        }
    }

    private void toggleToCategoryMode() {
        isCategoryMode = true;
        exercisesViewSwitcher.showPrevious();
        categoriesExercisesTextView.setText(getString(R.string.categories));
        handleCategoriesList();
    }

    private void toggleToExerciseMode(int position) {
        isCategoryMode = false;
        exercisesViewSwitcher.showNext();
        categoriesExercisesTextView.setText(getString(R.string.exercises));
        selectedCategoryPosition = position;
        handleExercisesList();
    }

    private void handleCategoriesList() {
        String url = getResources().getString(R.string.api_route) + "categories/getByUserId?userId=" +
                preferences.getInt("userID", -1);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) emptyCategoriesTextView.setVisibility(View.VISIBLE);
                        else emptyCategoriesTextView.setVisibility(View.GONE);

                        categoriesArrayList = new ArrayList<HashMap<String, String>>();
                        for (int i = 0; i < response.length(); i++) {
                            HashMap<String, String> el = new HashMap<String, String>();
                            try {
                                el.put("id", response.getJSONObject(i).getString("id"));
                                el.put("name", response.getJSONObject(i).getString("name"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            categoriesArrayList.add(el);
                        }

                        categoriesAdapter = new SimpleAdapter(ExercisesListActivity.this, categoriesArrayList,
                                R.layout.exercises_universal,
                                new String[]{"name"},
                                new int[]{R.id.exercisesTextView}) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                ImageButton threeDotsExercisesButton = view.findViewById(R.id.threeDotsExercisesButton);

                                threeDotsExercisesButton.setTag(position);

                                threeDotsExercisesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int clickedPosition = (int) v.getTag();
                                        showCategoryPopUpMenu(v, clickedPosition);
                                    }
                                });

                                return view;
                            }
                        };

                        categoriesListView.setAdapter(categoriesAdapter);

                        categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                toggleToExerciseMode(position);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;

                            if (statusCode == 404) {
                                Toast.makeText(ExercisesListActivity.this,
                                        getResources().getString(R.string.error_user), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ExercisesListActivity.this,
                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        MySingleton.getInstance(ExercisesListActivity.this).addToRequestQueue(request);
    }

    private void handleExercisesList() {
        String url = getResources().getString(R.string.api_route) + "exercises/getByCategoryId?categoryId=" +
                categoriesArrayList.get(selectedCategoryPosition).get("id");
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() == 0) emptyExercisesTextView.setVisibility(View.VISIBLE);
                        else emptyExercisesTextView.setVisibility(View.GONE);

                        exercisesArrayList = new ArrayList<HashMap<String, String>>();
                        for (int i = 0; i < response.length(); i++) {
                            HashMap<String, String> el = new HashMap<String, String>();
                            try {
                                el.put("id", response.getJSONObject(i).getString("id"));
                                el.put("name", response.getJSONObject(i).getString("name"));
                                el.put("notes", (response.getJSONObject(i).getString("notes") == "null" ?
                                        "" : response.getJSONObject(i).getString("notes")));
                                el.put("favourite", response.getJSONObject(i).getString("favourite"));
                                el.put("categoryID", response.getJSONObject(i).getString("categoryId"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            exercisesArrayList.add(el);
                        }

                        exercisesAdapter = new SimpleAdapter(ExercisesListActivity.this, exercisesArrayList,
                                R.layout.exercises_universal,
                                new String[]{"name"},
                                new int[]{R.id.exercisesTextView}) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                ImageButton threeDotsExercisesButton = view.findViewById(R.id.threeDotsExercisesButton);
                                ImageView favouriteButton = view.findViewById(R.id.favouriteButton);

                                if (exercisesArrayList.get(position).get("favourite") == "true")
                                    favouriteButton.setVisibility(View.VISIBLE);

                                threeDotsExercisesButton.setTag(position);
                                threeDotsExercisesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int clickedPosition = (int) v.getTag();
                                        showExercisePopUpMenu(v, clickedPosition);
                                    }
                                });

                                return view;
                            }
                        };

                        exercisesListView.setAdapter(exercisesAdapter);

                        exercisesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                finish();
                                Intent exerciseIntent = new Intent(ExercisesListActivity.this, ExerciseActivity.class);
                                exerciseIntent.putExtra("exerciseID", exercisesArrayList.get(position).get("id"));
                                startActivity(exerciseIntent);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;

                            if (statusCode == 404) {
                                Toast.makeText(ExercisesListActivity.this,
                                        getResources().getString(R.string.error_user), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ExercisesListActivity.this,
                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        MySingleton.getInstance(ExercisesListActivity.this).addToRequestQueue(request);
    }

    private void showCategoryPopUpMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.edit_delete_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_edit:
                        openCategoryDialog(false, position);
                        return true;
                    case R.id.menu_delete:
                        openCategoryExerciseDeleteDialog(true, position);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    private void showExercisePopUpMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.edit_delete_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_edit:
                        openExerciseDialog(false, position);
                        return true;
                    case R.id.menu_delete:
                        openCategoryExerciseDeleteDialog(false, position);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }

    private void openCategoryDialog(boolean add, int position) {
        final Dialog dialog = new Dialog(ExercisesListActivity.this);
        dialog.setContentView(R.layout.dialog_category_edit);

        TextView editCategoryTextView = dialog.findViewById(R.id.editCategoryTextView);
        Button acceptCategoryChange = dialog.findViewById(R.id.acceptCategoryChange);
        Button cancelCategoryChange = dialog.findViewById(R.id.cancelCategoryChange);
        EditText editCategoryNameEditText = dialog.findViewById(R.id.editCategoryNameEditText);

        editCategoryTextView.setText(add ? getString(R.string.new_category) : getString(R.string.edit_category_wide));
        if (!add) editCategoryNameEditText.setText(categoriesArrayList.get(position).get("name"));

        acceptCategoryChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (add) {
                    String url = getResources().getString(R.string.api_route) + "categories/add?name=" +
                            editCategoryNameEditText.getText().toString() + "&userId=" +
                            preferences.getInt("userID", -1);
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(ExercisesListActivity.this,
                                            getResources().getString(R.string.add_category), Toast.LENGTH_SHORT).show();
                                    handleCategoriesList();
                                    dialog.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse != null) {
                                        int statusCode = error.networkResponse.statusCode;

                                        if (statusCode == 409) {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.wrong_category), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                    MySingleton.getInstance(ExercisesListActivity.this).addToRequestQueue(request);
                }
                else {
                    String url = getResources().getString(R.string.api_route) + "categories/edit?id=" +
                            categoriesArrayList.get(position).get("id") + "&name=" +
                            editCategoryNameEditText.getText().toString() + "&userId=" +
                            preferences.getInt("userID", -1);
                    StringRequest request = new StringRequest(Request.Method.PUT, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(ExercisesListActivity.this,
                                            getResources().getString(R.string.edit_category), Toast.LENGTH_SHORT).show();
                                    handleCategoriesList();
                                    dialog.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse != null) {
                                        int statusCode = error.networkResponse.statusCode;

                                        if (statusCode == 404) {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.no_category), Toast.LENGTH_SHORT).show();
                                        } else if (statusCode == 409) {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.wrong_category), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                    MySingleton.getInstance(ExercisesListActivity.this).addToRequestQueue(request);
                }
            }
        });

        cancelCategoryChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                editCategoryNameEditText.setSelection(editCategoryNameEditText.getText().length());
                editCategoryNameEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editCategoryNameEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        dialog.show();
    }

    private void openExerciseDialog(boolean add, int position) {
        final Dialog dialog = new Dialog(ExercisesListActivity.this);
        dialog.setContentView(R.layout.dialog_exercise_edit);

        TextView editExerciseTextView = dialog.findViewById(R.id.editExerciseTextView);
        Button acceptExerciseChange = dialog.findViewById(R.id.acceptExerciseChange);
        Button cancelExerciseChange = dialog.findViewById(R.id.cancelExerciseChange);
        EditText editExerciseNameEditText = dialog.findViewById(R.id.editExerciseNameEditText);
        EditText editExerciseNotesEditText = dialog.findViewById(R.id.editExerciseNotesEditText);
        EditText categoryNameEditText = dialog.findViewById(R.id.categoryNameEditText);

        editExerciseTextView.setText(add ? getString(R.string.new_exercise) : getString(R.string.edit_exercise_wide));
        if (!add) {
            editExerciseNameEditText.setText(exercisesArrayList.get(position).get("name"));
            editExerciseNotesEditText.setText(exercisesArrayList.get(position).get("notes"));
        }
        categoryNameEditText.setText(categoriesArrayList.get(selectedCategoryPosition).get("name"));

        acceptExerciseChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (add) {
                    String url = getResources().getString(R.string.api_route) + "exercises/add?name=" +
                            editExerciseNameEditText.getText().toString() + "&notes=" +
                            editExerciseNotesEditText.getText().toString() + "&userId=" +
                            preferences.getInt("userID", -1) + "&categoryId=" +
                            categoriesArrayList.get(selectedCategoryPosition).get("id");
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(ExercisesListActivity.this,
                                            getResources().getString(R.string.add_exercise), Toast.LENGTH_SHORT).show();
                                    handleExercisesList();
                                    dialog.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse != null) {
                                        int statusCode = error.networkResponse.statusCode;

                                        if (statusCode == 409) {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.wrong_exercise), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                    MySingleton.getInstance(ExercisesListActivity.this).addToRequestQueue(request);
                }
                else {
                    String url = getResources().getString(R.string.api_route) + "exercises/edit?id=" +
                            exercisesArrayList.get(position).get("id") + "&name=" +
                            editExerciseNameEditText.getText().toString() + "&notes=" +
                            editExerciseNotesEditText.getText().toString() + "&userId=" +
                            preferences.getInt("userID", -1);
                    StringRequest request = new StringRequest(Request.Method.PUT, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(ExercisesListActivity.this,
                                            getResources().getString(R.string.edit_exercise), Toast.LENGTH_SHORT).show();
                                    handleExercisesList();
                                    dialog.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error.networkResponse != null) {
                                        int statusCode = error.networkResponse.statusCode;

                                        if (statusCode == 404) {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.no_exercise), Toast.LENGTH_SHORT).show();
                                        } else if (statusCode == 409) {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.wrong_exercise), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ExercisesListActivity.this,
                                                    getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });

                    MySingleton.getInstance(ExercisesListActivity.this).addToRequestQueue(request);
                }
            }
        });

        cancelExerciseChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                editExerciseNameEditText.setSelection(editExerciseNameEditText.getText().length());
                editExerciseNameEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editExerciseNameEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        dialog.show();
    }

    private void openCategoryExerciseDeleteDialog(boolean category, int position) {
        final Dialog dialog = new Dialog(ExercisesListActivity.this);
        dialog.setContentView(R.layout.dialog_delete);

        TextView deleteCategoryExerciseTextView = dialog.findViewById(R.id.deleteCategoryExerciseTextView);
        TextView deleteCategoryExerciseNameTextView = dialog.findViewById(R.id.deleteCategoryExerciseNameTextView);
        Button acceptCategoryExerciseDelete = dialog.findViewById(R.id.acceptCategoryExerciseDelete);
        Button cancelCategoryExerciseDelete = dialog.findViewById(R.id.cancelCategoryExerciseDelete);

        deleteCategoryExerciseTextView.setText(category ?
                getString(R.string.delete_category_wide) : getString(R.string.delete_exercise_wide));

        String name = (category ? categoriesArrayList : exercisesArrayList).get(position).get("name");

        SpannableStringBuilder sb = new SpannableStringBuilder(getString(R.string.delete) + " " + name +
                "?\n" + getString(R.string.related_data));
        int startIndex = sb.toString().indexOf(name);
        int endIndex = startIndex + name.length();
        sb.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        startIndex = sb.toString().indexOf(getString(R.string.permanently));
        endIndex = startIndex + getString(R.string.permanently).length();
        sb.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        deleteCategoryExerciseNameTextView.setText(sb);

        acceptCategoryExerciseDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getResources().getString(R.string.api_route) +
                        (category ? "categories" : "exercises") + "/delete?id=" +
                        (category ? categoriesArrayList : exercisesArrayList).get(position).get("id");
                StringRequest request = new StringRequest(Request.Method.DELETE, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(ExercisesListActivity.this, getResources().getString(
                                        (category ? R.string.delete_category : R.string.delete_exercise)),
                                        Toast.LENGTH_SHORT).show();
                                if (category) handleCategoriesList();
                                else handleExercisesList();
                                dialog.dismiss();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null) {
                                    int statusCode = error.networkResponse.statusCode;

                                    if (statusCode == 404) {
                                        Toast.makeText(ExercisesListActivity.this, getResources().getString(
                                                (category ? R.string.no_category : R.string.no_exercise)),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ExercisesListActivity.this,
                                                getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                MySingleton.getInstance(ExercisesListActivity.this).addToRequestQueue(request);
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
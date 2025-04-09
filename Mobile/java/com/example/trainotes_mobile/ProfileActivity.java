package com.example.trainotes_mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.trainotes_mobile.other.ActivityUtil;
import com.example.trainotes_mobile.other.ImageLoadTask;
import com.example.trainotes_mobile.other.MySingleton;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNicknameTextView;
    private ImageButton infoEditButton;
    private ShapeableImageView userImage;
    private TextView userEmailTextView;
    private TextView userPasswordTextView;
    private Switch newsSwitch;
    private TextView passwordTextView;
    private LinearLayout passwordLinearLayout;
    private EditText firstPasswordEditText;
    private EditText secondPasswordEditText;

    private SharedPreferences preferences;

    private String userNickname;
    private String userImageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.setAppLocale(this);
        setContentView(R.layout.activity_profile);

        userNicknameTextView = findViewById(R.id.userNicknameTextView);
        infoEditButton = findViewById(R.id.infoEditButton);
        userImage = findViewById(R.id.userImage);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        userPasswordTextView = findViewById(R.id.userPasswordTextView);
        newsSwitch = findViewById(R.id.newsSwitch);
        passwordTextView = findViewById(R.id.passwordTextView);
        firstPasswordEditText = findViewById(R.id.firstPassword);
        secondPasswordEditText = findViewById(R.id.secondPassword);

        preferences = PreferenceManager
                .getDefaultSharedPreferences(ProfileActivity.this);
        SharedPreferences.Editor editor = preferences.edit();

        load();

        View.OnClickListener editListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(ProfileActivity.this);
                dialog.setContentView(R.layout.dialog_info_edit);

                EditText nicknameEditText = dialog.findViewById(R.id.nicknameEditText);
                EditText imageURLEditText = dialog.findViewById(R.id.imageURLEditText);
                Button acceptDataChange = dialog.findViewById(R.id.acceptDataChange);
                Button cancelDataChange = dialog.findViewById(R.id.cancelDataChange);

                nicknameEditText.setText(userNickname);
                imageURLEditText.setText(userImageURL);

                acceptDataChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = getResources().getString(R.string.api_route) + "users/update/profile?id=" +
                                preferences.getInt("userID", -1) + "&nickname=" +
                                nicknameEditText.getText().toString() + "&imageURL=" +
                                imageURLEditText.getText().toString();
                        StringRequest request = new StringRequest(Request.Method.PUT, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Toast.makeText(ProfileActivity.this,
                                                getResources().getString(R.string.successful_profile), Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(ProfileActivity.this,
                                                        getResources().getString(R.string.error_user), Toast.LENGTH_SHORT).show();
                                            }
                                            else if (statusCode == 409) {
                                                Toast.makeText(ProfileActivity.this,
                                                        getResources().getString(R.string.wrong_profile), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ProfileActivity.this,
                                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });

                        MySingleton.getInstance(ProfileActivity.this).addToRequestQueue(request);
                    }
                });

                cancelDataChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        };

        infoEditButton.setOnClickListener(editListener);
        userImage.setOnClickListener(editListener);

        newsSwitch.setChecked(preferences.getBoolean("isNewsAllowed", false));
        newsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("isNewsAllowed", true);
                    editor.apply();
                } else {
                    editor.putBoolean("isNewsAllowed", false);
                    editor.apply();
                }
            }
        });

        passwordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordLinearLayout = findViewById(R.id.passwordLinearLayout);

                if (passwordLinearLayout.getVisibility()==View.GONE)
                    passwordLinearLayout.setVisibility(View.VISIBLE);

                else if (passwordLinearLayout.getVisibility()==View.VISIBLE)
                {
                    if (passwordTextView.getCurrentTextColor() ==
                            ContextCompat.getColor(ProfileActivity.this, R.color.green))
                    {
                        String url = getResources().getString(R.string.api_route) + "users/update/password?id=" +
                                preferences.getInt("userID", -1) + "&password=" +
                                secondPasswordEditText.getText().toString();
                        StringRequest request = new StringRequest(Request.Method.PUT, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Toast.makeText(ProfileActivity.this,
                                                getResources().getString(R.string.successful_password), Toast.LENGTH_SHORT).show();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (error.networkResponse != null) {
                                            int statusCode = error.networkResponse.statusCode;

                                            if (statusCode == 404) {
                                                Toast.makeText(ProfileActivity.this,
                                                        getResources().getString(R.string.error_user), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ProfileActivity.this,
                                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });

                        MySingleton.getInstance(ProfileActivity.this).addToRequestQueue(request);
                    }

                    else if (passwordTextView.getCurrentTextColor() ==
                            ContextCompat.getColor(ProfileActivity.this, R.color.red))
                        Toast.makeText(ProfileActivity.this,
                                getString(R.string.password_isnt_changed), Toast.LENGTH_SHORT).show();

                    firstPasswordEditText.setText(null);
                    secondPasswordEditText.setText(null);
                    firstPasswordEditText.setError(null);
                    secondPasswordEditText.setError(null);
                    passwordTextView.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.blue));
                    passwordLinearLayout.setVisibility(View.GONE);
                }
            }
        });

        firstPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateFirstPassword();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        secondPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateSecondPassword();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });
    }

    public void onLogOutButtonClicked(View view) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        finish();
        Intent detailsIntent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(detailsIntent);
    }

    public void onProfileButtonClicked(View view) { ActivityUtil.restartActivities(this); }

    private void load() {
        String url = getResources().getString(R.string.api_route) + "users/get?id=" + preferences.getInt("userID", -1);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            userNickname = response.getString("nickname");
                            userImageURL = response.getString("imageURL");
                            int gender = response.getInt("gender");
                            String email = response.getString("email");
                            String password = response.getString("password");

                            userNicknameTextView.setText(userNickname);

                            new ImageLoadTask(userImageURL, userImage).execute();
                            int strokeColor = ContextCompat.getColor(ProfileActivity.this,
                                    (gender == 0 ? R.color.blue : (gender == 1 ? R.color.red : R.color.green)));
                            ColorStateList colorStateList = ColorStateList.valueOf(strokeColor);
                            userImage.setStrokeColor(colorStateList);

                            userEmailTextView.setText(email);
                            String hiddenPassword = "";
                            for (int i = 0; i < password.length(); i++) hiddenPassword += "*";
                            userPasswordTextView.setText(hiddenPassword);

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
                                Toast.makeText(ProfileActivity.this,
                                        getResources().getString(R.string.error_user), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this,
                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        MySingleton.getInstance(ProfileActivity.this).addToRequestQueue(request);
    }

    private void validateFirstPassword() {
        Pattern patternSymbols = Pattern.compile("^[a-zA-Z0-9.-]+$");
        Pattern patternLength = Pattern.compile("^.{8,12}$");
        boolean isSymbolsCorrect = patternSymbols.matcher(firstPasswordEditText.getText()).matches();
        boolean isLengthCorrect = patternLength.matcher(firstPasswordEditText.getText()).matches();
        if (isSymbolsCorrect && isLengthCorrect) {
            firstPasswordEditText.setError(null);
        } else {
            firstPasswordEditText.setError((isSymbolsCorrect ? "" :
                    getString(R.string.password_must_contain)) + (isLengthCorrect ? "" :
                    getString(R.string.password_must_be_between)));
        }
        handlePasswordTextViewAvailability();
    }

    private void validateSecondPassword() {
        boolean isPasswordsEqual = firstPasswordEditText.getText().toString().equals(secondPasswordEditText.getText().toString());
        if (isPasswordsEqual) {
            secondPasswordEditText.setError(null);
        }
        else {
            secondPasswordEditText.setError(getString(R.string.passwords_dont_match));
        }
        handlePasswordTextViewAvailability();
    }

    private void handlePasswordTextViewAvailability() {
        Pattern patternSymbols = Pattern.compile("^[a-zA-Z0-9.-]+$");
        Pattern patternLength = Pattern.compile("^.{8,12}$");
        boolean isSymbolsCorrect = patternSymbols.matcher(firstPasswordEditText.getText()).matches();
        boolean isLengthCorrect = patternLength.matcher(firstPasswordEditText.getText()).matches();
        boolean isPasswordsEqual = firstPasswordEditText.getText().toString().equals(secondPasswordEditText.getText().toString());
        if (isSymbolsCorrect && isLengthCorrect && isPasswordsEqual) {
            passwordTextView.setTextColor(ContextCompat.getColor(this, R.color.green));
        }
        else {
            passwordTextView.setTextColor(ContextCompat.getColor(this, R.color.red));
        }
    }
}
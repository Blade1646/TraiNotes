package com.example.trainotes_mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.trainotes_mobile.other.ActivityUtil;
import com.example.trainotes_mobile.other.ImageLoadTask;
import com.example.trainotes_mobile.other.MySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.setAppLocale(this);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);

        preferences = PreferenceManager
                .getDefaultSharedPreferences(LoginActivity.this);

        if (preferences.getInt("userID", -1) != -1) { signIn(); }

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { handleSignInButtonAvailability(); }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { handleSignInButtonAvailability(); }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
    }

    private void handleSignInButtonAvailability() {
        if (!emailEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty())
            signInButton.setEnabled(true);
    }

    public void onSignInButtonClicked(View view) {
        String url = getResources().getString(R.string.api_route) + "users/login?email=" +
                emailEditText.getText().toString() + "&password=" + passwordEditText.getText().toString();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int userID = response.getInt("userId");

                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("userID", userID);
                            editor.apply();

                            signIn();
                            Toast.makeText(LoginActivity.this,
                                    getResources().getString(R.string.successful_login), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(LoginActivity.this,
                                        getResources().getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
                                passwordEditText.setText("");
                            } else if (statusCode == 401) {
                                Toast.makeText(LoginActivity.this,
                                        getResources().getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                                passwordEditText.setText("");
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        MySingleton.getInstance(LoginActivity.this).addToRequestQueue(request);
    }

    public void onForgotButtonClicked(View view) {
        String url = "https://www.trainotes.com/recoverpassword";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    public void onSignUpButtonClicked(View view) {
        String url = "https://www.trainotes.com/signup";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void signIn() {
        finish();
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
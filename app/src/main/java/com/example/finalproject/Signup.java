package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.finalproject.MainActivity.URLADDRESS;

public class Signup extends AppCompatActivity {
    EditText usernameText;
    EditText emailText;
    EditText passwordText;
    EditText reEnterPasswordText;
    TextInputLayout usernameLayout;
    TextInputLayout emailLayout;
    TextInputLayout passwordLayout;
    TextInputLayout rePasswordLayout;
    String userId;
    boolean requestSuccess;
    Button signupButton;
    TextView loginLink;
    String username;
    String email;
    String password;
    String reEnterPassword;
    ProgressDialog progressDialog;
    JSONObject userdata;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        findViewsById();
        loginLink.setOnClickListener(v -> {
            backToLogin();
        });
        signupButton.setOnClickListener(v -> {
            if(validate()) {
                if(isOnline()){
                    Task myTask = new Task();
                    myTask.execute();
                }
                else Toast.makeText(Signup.this, "No connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void backToLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginPage.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
    }

    private void findViewsById() {
        usernameText = findViewById(R.id.input_username);
        emailText = findViewById(R.id.input_email);
        passwordText = findViewById(R.id.input_password);
        reEnterPasswordText = findViewById(R.id.input_reEnterPassword);
        signupButton = findViewById(R.id.btn_signup);
        loginLink = findViewById(R.id.link_login);
        passwordLayout = findViewById(R.id.passwordlayout);
        rePasswordLayout = findViewById(R.id.rePasswordlayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        emailLayout = findViewById(R.id.emaillayout);
    }

    public boolean validate() {
        boolean valid = true;
         username = usernameText.getText().toString();
         email = emailText.getText().toString();
         password = passwordText.getText().toString();
         reEnterPassword = reEnterPasswordText.getText().toString();

        if (username.isEmpty() || username.length() < 3 || username.contains(" ")) {
            usernameLayout.setError("Enter at least 3 characters. Spaces aren't allowed");
            valid = false;
        } else {
            usernameLayout.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address.");
            valid = false;
        } else {
            emailLayout.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordLayout.setError("Password can't be shorter than 6 characters.");
            valid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || !(reEnterPassword.equals(password))) {
            rePasswordLayout.setError("Passwords do not match.");
            valid = false;
        } else {
            rePasswordLayout.setError(null);
        }

        return valid;
    }
    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private String postAuth(String url, String userInfo) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(userInfo.getBytes()))
                .build();
        Response response = client.newCall(request).execute();
        int responseCode = response.code();
        if(responseCode<300 && responseCode>=200) requestSuccess=true;
        Headers headers = response.headers();
        String id = headers.get("userid");
        userId = id;
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.finalproject", Context.MODE_PRIVATE);
        if(id!=null) sharedPreferences.edit().putString("userid", id).apply();
        return response.body().string();
    }

    public class Task extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Signup.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Account...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            userdata = new JSONObject();
            try {
                userdata.put("username", username);
                userdata.put("email", email);
                userdata.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            String jsondata = userdata.toString();
            String response = null;
            try {
                response = postAuth(URLADDRESS+"/users/signUp", jsondata);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String output) {
            progressDialog.dismiss();
            switch (output) {
                case "usernameexists":
                    passwordLayout.setError(null);
                    rePasswordLayout.setError(null);
                    emailLayout.setError(null);
                    usernameLayout.setError("Username Taken!");
                    break;
                case "emailexists":
                    passwordLayout.setError(null);
                    rePasswordLayout.setError(null);
                    usernameLayout.setError(null);
                    emailLayout.setError("This email has already been used!");
                    break;
                default:
                    if(output!=null && requestSuccess){
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.finalproject", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("key", output).apply();
                        sharedPreferences.edit().putInt("logged", 1).apply();
                        Intent intent = new Intent(Signup.this, RemindersController.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    else Toast.makeText(Signup.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginPage.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
    }
}
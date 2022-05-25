package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.finalproject.models.User;
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



public class LoginPage extends AppCompatActivity {


    EditText emailText;
    EditText passwordText;
    AppCompatButton login;
    TextView link_signup;
    ProgressDialog progressDialog;
    Boolean backPressed= false;
    JSONObject userdata;
    String userId;
    String reminders;
    TextInputLayout passwordlayout;
    TextInputLayout emaillayout;
    boolean requestSuccess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        findViewsById();
        link_signup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Signup.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
        });
        login.setOnClickListener(v -> {
            if(validate()) {
                if(isOnline()){
                    Task myTask = new Task();
                    myTask.execute();
                }
                else Toast.makeText(LoginPage.this, "No connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty()) {
            emaillayout.setError("Enter a valid username/email");
            valid = false;
        } else {
            emaillayout.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 ) {
            passwordlayout.setError("Enter at least 6 characters");
            valid = false;
        } else {
            passwordlayout.setError(null);
        }

        return valid;
    }
    private void findViewsById() {
        emailText = findViewById(R.id.input_email);
        passwordText = findViewById(R.id.input_password);
        login = findViewById(R.id.btn_login);
        link_signup = findViewById(R.id.link_signup);
        passwordlayout = findViewById(R.id.passwordlayout);
        emaillayout = findViewById(R.id.emaillayout);
    }
    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
    @Override
    public void onBackPressed() {
        if(backPressed) {
            finish();
        }
        else{
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            backPressed=true;
        }
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
            progressDialog = new ProgressDialog(LoginPage.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging in...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            userdata = new JSONObject();
            try {
                userdata.put("email", emailText.getText().toString());
                userdata.put("password", passwordText.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        @Override
        protected String doInBackground(String... urls) {
            String userInfo = userdata.toString();
            String response = null;
            try {
                response = postAuth(URLADDRESS+"/users/login", userInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String output) {
            switch (output) {
                case "noAccount":
                    passwordlayout.setError(null);
                    emaillayout.setError("Username/Email does not exist!");
                    break;
                case "wrongpassword":
                    emaillayout.setError(null);
                    passwordlayout.setError("Wrong password!");
                    break;
                default:
                    if(output!=null && requestSuccess){
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.finalproject", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("key", output).apply();
                        sharedPreferences.edit().putInt("logged", 1).apply();
                        Intent intent = new Intent(LoginPage.this, RemindersController.class);
                        startActivity(intent);
                        finish();
                    }
                    else Toast.makeText(LoginPage.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }
    }


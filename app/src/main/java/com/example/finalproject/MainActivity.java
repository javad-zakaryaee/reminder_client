package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    ImageView view;
    TextView conFail;
    Button retry;
    ProgressBar progressBar;
    final public static String URLADDRESS ="http:10.0.2.2:8080/api";
    SharedPreferences sharedPreferences;
    private boolean shouldFinish = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewsById();
        retry.setOnClickListener(v -> checkOnlineStatus());
        new Task().execute();
    }

    private void checkOnlineStatus(){
        sharedPreferences = getApplication().getSharedPreferences("com.example.finalproject", Context.MODE_PRIVATE);
        if(isOnline()){
            if(conFail.getVisibility()==View.VISIBLE){
                conFail.setVisibility(View.GONE);
                retry.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
            int i = sharedPreferences.getInt("logged", -1);
            if(i==1){
                Intent intent = new Intent(MainActivity.this, RemindersController.class);
                startActivity(intent);
                shouldFinish = true;
            }
            else {
                Intent intent = new Intent(this, LoginPage.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(this, view, "transit");
                startActivity(intent, options.toBundle());
                shouldFinish=true;
            }
        }
        else {
            progressBar = findViewById(R.id.progress);
            progressBar.setVisibility(View.GONE);
            conFail.setVisibility(View.VISIBLE);
            retry.setVisibility(View.VISIBLE);
        }
    }

    private void findViewsById() {
        view = findViewById(R.id.tw);
        conFail  = findViewById(R.id.connectionFail);
        conFail.setVisibility(View.GONE);
        retry = findViewById(R.id.retrybutton);
        retry.setVisibility(View.GONE);
    }

    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
    public class Task extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            retry.performClick();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(shouldFinish){
            finish();
        }
    }

}
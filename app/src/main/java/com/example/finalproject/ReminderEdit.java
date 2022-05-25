package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.textclassifier.ConversationActions;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.example.finalproject.models.Reminder;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalTime;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.finalproject.MainActivity.URLADDRESS;

import static com.example.finalproject.RemindersController.*;
public class ReminderEdit extends AppCompatActivity {
    EditText time;
    EditText title;
    EditText desc;
    Button save;
    Boolean newReminder;
    ProgressDialog pd;
    String Token;
    TimePickerDialog tmPicker;
    Intent intent;
    JSONObject jso = new JSONObject();
    String userId;
    TextInputLayout titleLayout;
    TextInputLayout timeLayout;
    TextInputLayout descLayout;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);
        findViewsById();
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Edit Note");
        intent = new Intent(ReminderEdit.this, RemindersController.class);
        time.setFocusable(false);
        time.setOnClickListener(v -> {
            tmPicker = new TimePickerDialog(this, R.style.AppTheme_Dark_Dialog, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    time.setText(view.getHour() + ":" + view.getMinute());
                }
            },15, 30, true);
            tmPicker.show();

        });
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.finalproject", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userid", null);
        Token = sharedPreferences.getString("key", null);
        Intent intent = getIntent();
        newReminder = intent.getBooleanExtra("new", false);
        Task mytask = new Task();
        String[] params = new String[2];
        if(newReminder){
            save.setOnClickListener(v -> {
                if (validate()) {
                    try {
                        jso.put("title", title.getText().toString());
                        jso.put("date", time.getText().toString());
                        jso.put("desc", desc.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    params[0] = "/reminders/add/" + userId;
                    params[1] = jso.toString();
                    mytask.execute(params);
                }
            });
        }
        else {
            Reminder rm = data.get(position);
            time.setText(reminder.getTime().toString());
            title.setText(reminder.getTitle());
            desc.setText(reminder.getDesc());
            save.setOnClickListener(v -> {
                if(validate()){
                    try {
                        jso.put("title", title.getText().toString());
                        jso.put("time", time.getText().toString());
                        jso.put("desc", desc.getText().toString());
                        jso.put("id", rm.getId());
                        jso.put("done", rm.getDone().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    params[0] = "/reminders/update/" + reminder.getId();
                    params[1] = jso.toString();
                    mytask.execute(params);
                }
            });
        }
    }

    private boolean validate() {
        boolean valid = true;
        String titleText = title.getText().toString();
        String timeText = time.getText().toString();
        String descText = desc.getText().toString();

        if (titleText.isEmpty()) {
            titleLayout.setError("Title can't be empty");
            valid = false;
        } else {
            titleLayout.setError(null);
        }

        if (timeText.isEmpty()) {
            timeLayout.setError("Please pick a time");
            valid = false;
        } else {
            timeLayout.setError(null);
        }

        if (descText.isEmpty()) {
            descLayout.setError("Description can't be empty");
            valid = false;
        } else {
            descLayout.setError(null);
        }
        return valid;
    }

    private void findViewsById() {
        time = findViewById(R.id.time);
        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);
        save = findViewById(R.id.saveButton);
        titleLayout = findViewById(R.id.titleLayout);
        timeLayout = findViewById(R.id.timeLayout);
        descLayout = findViewById(R.id.desLayout);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class Task extends AsyncTask<String, String, String>{
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(ReminderEdit.this, R.style.AppTheme_Dark_Dialog);
            pd.setIndeterminate(true);
            pd.setMessage("Saving...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(URLADDRESS+strings[0])
                    .post(RequestBody.create(strings[1].getBytes()))
                    .header("Authorization", Token)
                    .build();
            try (Response response = client.newCall(request).execute()) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String s) {
            pd.dismiss();
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
    }
}
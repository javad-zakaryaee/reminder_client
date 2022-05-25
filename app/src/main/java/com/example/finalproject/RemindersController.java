package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.customrecycler.CustomAdapter;
import com.example.finalproject.customrecycler.UIActionClass;
import com.example.finalproject.models.Reminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.finalproject.MainActivity.URLADDRESS;

public class RemindersController extends AppCompatActivity {
    public static RecyclerView recyclerView;
    public static JSONArray jsonArray = null;
    FloatingActionButton fab;
    public static int position;
    public static CustomAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    public static ArrayList<Reminder> data;
    public static View.OnClickListener myOnClickListener;
    public static Reminder reminder;
    String TOKEN;
    String username;
    ArrayList<Long> removedItems;
    SharedPreferences sharedPreferences;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getApplicationContext().getSharedPreferences("com.example.finalproject", Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        findViewsById();
        myOnClickListener = new MyOnClickListener(this);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        data = new ArrayList<>();
        removedItems = new ArrayList<>();
        Task mytask = new Task();
        mytask.execute();
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(RemindersController.this, ReminderEdit.class);
            intent.putExtra("new", true);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
        });
    }

    private void findViewsById() {
        recyclerView = findViewById(R.id.Recycler_List);
        fab = findViewById(R.id.fab);

    }
    private  class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            position = recyclerView.getChildAdapterPosition(v);
            reminder= data.get(recyclerView.getChildAdapterPosition(v));
            Intent intent = new Intent(RemindersController.this, ReminderEdit.class);
            intent.putExtra("new", false);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
        }
    }

    private void enableSwipeToDeleteAndUndo() {
        UIActionClass swipeToDeleteCallback = new UIActionClass(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Reminder item = adapter.getData().get(position);
                removedItems.add(data.get(position).getId());
                adapter.deleteItem(position);
                Snackbar snackbar = Snackbar
                        .make(layoutManager.getChildAt(0), "Item was removed from the list.", Snackbar.LENGTH_SHORT);
                snackbar.setAction("UNDO", view -> {
                    adapter.restoreItem(item, position);
                    removedItems.remove(data.get(position).getId());
                    recyclerView.scrollToPosition(position);
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }
    public String getReminders(String urlString) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .header("Authorization", TOKEN)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String getUsername() throws IOException {
        String id = sharedPreferences.getString("userid", null);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URLADDRESS + "/users/get/" + id)
                .get()
                .header("Authorization", TOKEN)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject userData = new JSONObject(response.body().string());
            return userData.getString("username");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            sharedPreferences.edit().putInt("logged", -1).apply();
            startActivity(new Intent(RemindersController.this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public class Delete extends AsyncTask<Long, String, String> {

        @Override
        protected String doInBackground(Long... longs) {
            OkHttpClient client = new OkHttpClient();
            StringBuilder address = new StringBuilder(URLADDRESS);
            address.append("/reminders/delete/");
            for (Long i : removedItems) {
                address.append(i + ",");
            }
            Request request = new Request.Builder()
                    .url(address.toString())
                    .get()
                    .header("Authorization", TOKEN)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
    public class Task extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            TOKEN = sharedPreferences.getString("key", null);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                username = getUsername();
                String id = sharedPreferences.getString("userid", null);
                String reminders = getReminders(URLADDRESS + "/reminders/get/" + id);
                return reminders;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String output) {
            ActionBar ab = getSupportActionBar();
            ab.setTitle("Welcome " + username + "!");
            try {
                jsonArray = new JSONArray(output);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if(jsonArray!=null){
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        data.add(new Reminder(jsonObject.getLong("id"), jsonObject.getString("title"), jsonObject.getString("desc"), LocalTime.parse(jsonObject.getString("time")), Boolean.valueOf(jsonObject.getString("done"))));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            removedItems = new ArrayList<>();
            adapter = new CustomAdapter(data);
            recyclerView.setAdapter(adapter);
            enableSwipeToDeleteAndUndo();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(removedItems!= null){
            new Delete().execute();
        }
    }

}

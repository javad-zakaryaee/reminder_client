package com.example.finalproject.customrecycler;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.RemindersController;
import com.example.finalproject.models.Reminder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.finalproject.MainActivity.URLADDRESS;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private ArrayList<Reminder> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        TextView contentId;
        CheckBox checkBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.contentId = itemView.findViewById(R.id.contentId);
            this.title = itemView.findViewById(R.id.title);
            this.time = itemView.findViewById(R.id.rmDescription);
            this.checkBox =  itemView.findViewById(R.id.checkButton);
        }
    }

    public CustomAdapter(ArrayList<Reminder> data) {
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        view.setOnClickListener(RemindersController.myOnClickListener);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        TextView title = holder.title;
        TextView time = holder.time;
        CheckBox checkBox = holder.checkBox;
        title.setText(dataSet.get(listPosition).getTitle());
        time.setText(dataSet.get(listPosition).getTime().toString());
        checkBox.setChecked(dataSet.get(listPosition).getDone());
        holder.checkBox.setOnClickListener(v -> {
            if(checkBox.isChecked()) dataSet.get(listPosition).setDone(false);
            else dataSet.get(listPosition).setDone(true);
            JSONObject jso = new JSONObject();
            Boolean isChecked = checkBox.isChecked();
            try {
                jso.put("isChecked", isChecked.toString());
                jso.put("URL", URLADDRESS + "/reminders/check/" + dataSet.get(listPosition).getId());
                new Task().execute(jso);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void deleteItem(int position) {
        dataSet.remove(position);
        notifyItemRemoved(position);
    }
    public void restoreItem(Reminder item, int position) {
        dataSet.add(position, item);
        notifyItemInserted(position);
    }
    public ArrayList<Reminder> getData() {
        return dataSet;
    }

    public class Task extends AsyncTask<JSONObject,String,String>{
        @Override
        protected String doInBackground(JSONObject... jsonObjects) {
            String url = null;
            String isChecked = null;
            try {
                url = jsonObjects[0].getString("URL");
                isChecked = jsonObjects[0].getString("isChecked");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(isChecked.getBytes()))
                    .build();
            try (Response response = client.newCall(request).execute()) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

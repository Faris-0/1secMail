package com.yuuna.onesecmail.util;

import static com.yuuna.onesecmail.util.NotificationService.notification;
import static com.yuuna.onesecmail.util.SharedPreferences.OneSecMail;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_READ;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class NotifikasiBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new Task(goAsync(), intent, context).execute();
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final Context context;

        private Boolean iCheck = false;

        private Task(PendingResult pendingResult, Intent intent, Context context) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            SharedPreferences notify = context.getSharedPreferences(OneSecMail, Context.MODE_PRIVATE);
            ArrayList<Integer> integerArrayList = new Gson().fromJson(notify.getString(TAG_READ, null), new TypeToken<ArrayList<Integer>>() {}.getType());
            if (integerArrayList == null) integerArrayList = new ArrayList<>();
            Integer id = intent.getIntExtra("id", 0);
            if (intent.getAction().equals("READ")) {
                for (int i = 0; i < integerArrayList.size(); i++) if (integerArrayList.get(i).equals(id)) iCheck = true;
                if (iCheck) iCheck = false;
                else integerArrayList.add(id);
                notify.edit().putString(TAG_READ, new Gson().toJson(integerArrayList)).apply();
                notification(context, "", "", "", id, true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pendingResult.finish();
        }
    }
}

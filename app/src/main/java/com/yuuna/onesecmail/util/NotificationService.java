package com.yuuna.onesecmail.util;

import static com.yuuna.onesecmail.util.RetrofitClient.retrofitAPI;
import static com.yuuna.onesecmail.util.SharedPreferences.OneSecMail;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_DOMAIN;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_READ;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_USERNAME;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuuna.onesecmail.R;
import com.yuuna.onesecmail.activity.DetailMessageActivity;
import com.yuuna.onesecmail.model.MessageModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationService extends Service {

    private Handler handler = new Handler();
    private Runnable refresh;
    public static SharedPreferences notify;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // do your jobs here
        refresh = new Runnable() {
            public void run() {
                notify = getSharedPreferences(OneSecMail, Context.MODE_PRIVATE);
                String login = notify.getString(TAG_USERNAME, "");
                String domain = notify.getString(TAG_DOMAIN, "");
                retrofitAPI.getAllMessage("getMessages", login, domain).enqueue(new Callback<ArrayList<MessageModel>>() {
                    @Override
                    public void onResponse(Call<ArrayList<MessageModel>> call, Response<ArrayList<MessageModel>> response) {
                        if (response.isSuccessful()) {
                            ArrayList<Integer> integerArrayList = new Gson().fromJson(notify.getString(TAG_READ, null),
                                    new TypeToken<ArrayList<Integer>>() {}.getType());
                            if (integerArrayList == null) integerArrayList = new ArrayList<>();
                            if (integerArrayList.size() != 0) for (int i = 0; i < integerArrayList.size(); i++) loadCheck(integerArrayList.get(i), response.body());
                            else for (int i = 0; i < response.body().size(); i++) notification(getApplicationContext(),
                                    response.body().get(i).getFrom(),
                                    response.body().get(i).getSubject(),
                                    response.body().get(i).getDate(),
                                    response.body().get(i).getId(),
                                    false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<MessageModel>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
                handler.postDelayed(refresh, 5000); // 1000 == 1sec
            }
        };
        handler.post(refresh);
        return super.onStartCommand(intent, flags, startId);
    }

    private void loadCheck(Integer id, ArrayList<MessageModel> body) {
        for (int i = 0; i < body.size(); i++) {
            if (!body.get(i).getId().equals(id)) {
                notification(getApplicationContext(),
                        body.get(i).getFrom(),
                        body.get(i).getSubject(),
                        body.get(i).getDate(),
                        body.get(i).getId(),
                        false);
            }
        }
    }

    public static void notification(Context context, String from, String subject, String datetime, Integer id, Boolean isRemove) {
        if (isRemove) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            NotificationManagerCompat.from(context).cancel(id);
        } else {
            Intent intent = new Intent(context, DetailMessageActivity.class).putExtra("id", id).putExtra("isOPEN", true)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, 0);

            Intent readIntent = new Intent(context, NotifikasiBroadcastReceiver.class).setAction("READ").putExtra("id", id);
            PendingIntent readPendingIntent = PendingIntent.getBroadcast(context, id, readIntent, 0);

            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id")).parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                    .setSmallIcon(android.R.drawable.ic_dialog_email)
                    .setContentTitle(from)
                    .setContentText(subject)
                    .setWhen(date.getTime())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setColor(Color.parseColor("#FDCB01"))
                    .addAction(0, "Mark as read", readPendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("");
                context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            NotificationManagerCompat.from(context).notify(id, builder.build());
        }
    }
}

package com.yuuna.onesecmail.activity;

import static com.yuuna.onesecmail.util.NotificationService.notification;
import static com.yuuna.onesecmail.util.RetrofitClient.retrofitAPI;
import static com.yuuna.onesecmail.util.SharedPreferences.OneSecMail;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_DOMAIN;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_READ;
import static com.yuuna.onesecmail.util.SharedPreferences.TAG_USERNAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuuna.onesecmail.R;
import com.yuuna.onesecmail.model.MessageModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailMessageActivity extends AppCompatActivity {

    private TextView tvFrom, tvSubject, tvHtml;

    private Boolean iCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);

        tvFrom = findViewById(R.id.mFrom);
        tvSubject = findViewById(R.id.mSubject);
        tvHtml = findViewById(R.id.mHTML);

        loadMessage();
    }

    private void loadMessage() {
        SharedPreferences detail = getSharedPreferences(OneSecMail, Context.MODE_PRIVATE);
        String login = detail.getString(TAG_USERNAME, "");
        String domain = detail.getString(TAG_DOMAIN, "");
        Integer id = getIntent().getIntExtra("id", 0);

        retrofitAPI.getDetailMessage("readMessage", login, domain, id).enqueue(new Callback<MessageModel>() {
            @Override
            public void onResponse(Call<MessageModel> call, Response<MessageModel> response) {
                if (response.isSuccessful()) {
                    SharedPreferences notify = getSharedPreferences(OneSecMail, Context.MODE_PRIVATE);
                    ArrayList<Integer> integerArrayList = new Gson().fromJson(notify.getString(TAG_READ, null), new TypeToken<ArrayList<Integer>>() {}.getType());
                    if (integerArrayList == null) integerArrayList = new ArrayList<>();
                    for (int i = 0; i < integerArrayList.size(); i++) if (integerArrayList.get(i).equals(id)) iCheck = true;
                    if (iCheck) iCheck = false;
                    else integerArrayList.add(id);
                    notify.edit().putString(TAG_READ, new Gson().toJson(integerArrayList)).apply();
                    notification(getApplicationContext(), "", "", "", id, true);

                    tvFrom.setText(response.body().getFrom());
                    tvSubject.setText(response.body().getSubject());

                    String dHtmlBody = response.body().getHtmlBody();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) tvHtml.setText(Html.fromHtml(dHtmlBody, Html.FROM_HTML_MODE_LEGACY));
                    else tvHtml.setText(Html.fromHtml(dHtmlBody));
                }
            }

            @Override
            public void onFailure(Call<MessageModel> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("isOPEN", false)) {
            startActivity(new Intent(this, MainActivity.class).putExtra("isOPEN", true));
            finish();
        } else super.onBackPressed();
    }
}
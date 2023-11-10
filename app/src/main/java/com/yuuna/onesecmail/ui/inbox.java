package com.yuuna.onesecmail.ui;

import static com.yuuna.onesecmail.util.RetrofitClient.retrofitAPI;
import static com.yuuna.onesecmail.util.AppConstants.OneSecMail;
import static com.yuuna.onesecmail.util.AppConstants.TAG_DOMAIN;
import static com.yuuna.onesecmail.util.AppConstants.TAG_USERNAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yuuna.onesecmail.R;
import com.yuuna.onesecmail.adapter.MessageAdapter;
import com.yuuna.onesecmail.model.MessageModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class inbox extends Fragment implements MessageAdapter.ItemClickListener {

    private RecyclerView rvMessages;

    private Handler handler = new Handler();
    private Runnable refresh;

    private String username, domain;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        rvMessages = view.findViewById(R.id.idListMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        return view;
    }

    private void autoRefresh() {
        refresh = () -> {
            loadInbox();
            handler.postDelayed(refresh, 5000);
        };
        handler.post(refresh);
    }

    private void loadInbox() {
        retrofitAPI.getAllMessage("getMessages", username, domain).enqueue(new Callback<ArrayList<MessageModel>>() {
            @Override
            public void onResponse(Call<ArrayList<MessageModel>> call, Response<ArrayList<MessageModel>> response) {
                if (response.isSuccessful()) {
                    MessageAdapter messageAdapter = new MessageAdapter(response.body());
                    rvMessages.setAdapter(messageAdapter);
                    messageAdapter.setClickListener(inbox.this);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<MessageModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onClick(MessageModel messageModel, View view, int position) {
        switch (view.getId()) {
            default:
                startActivity(new Intent(getActivity(), DetailMessageActivity.class).putExtra("id", messageModel.getId()));
                break;
        }
    }

    @Override
    public void onStart() {
        SharedPreferences inbox = getActivity().getSharedPreferences(OneSecMail, Context.MODE_PRIVATE);
        username = inbox.getString(TAG_USERNAME, null);
        domain = inbox.getString(TAG_DOMAIN, null);

        autoRefresh();
        super.onStart();
    }

    @Override
    public void onStop() {
        handler.removeCallbacks(refresh);
        super.onStop();
    }
}
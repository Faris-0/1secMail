package com.yuuna.onesecmail.util;

import com.yuuna.onesecmail.model.MessageModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitAPI {

    @GET("v1/")
    Call<ArrayList<MessageModel>> getAllMessage(
            @Query("action") String action,
            @Query("login") String login,
            @Query("domain") String domain
    );

    @GET("v1/")
    Call<MessageModel> getDetailMessage(
            @Query("action") String action,
            @Query("login") String login,
            @Query("domain") String domain,
            @Query("id") Integer id
    );

    @GET("v1/")
    Call<List<String>> getDomain(@Query("action") String action);
}

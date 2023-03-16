package com.yuuna.onesecmail.util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static RetrofitAPI retrofitAPI = RetrofitClient.getRetrofit().create(RetrofitAPI.class);
    private static Retrofit getRetrofit() {
        return new Retrofit.Builder().baseUrl("https://www.1secmail.com/api/").addConverterFactory(GsonConverterFactory.create()).build();
    }
}

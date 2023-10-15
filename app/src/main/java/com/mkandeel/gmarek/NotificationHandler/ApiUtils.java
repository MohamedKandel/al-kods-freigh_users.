package com.mkandeel.gmarek.NotificationHandler;

import static com.mkandeel.gmarek.NotificationHandler.Constants.BASE_URL;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtils {
    public static Retrofit retrofit = null;
    public static ApiInterface getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiInterface.class);
    }
}

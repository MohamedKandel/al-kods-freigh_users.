package com.mkandeel.gmarek.NotificationHandler;

import static com.mkandeel.gmarek.NotificationHandler.Constants.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {
    @Headers({"Authorization: key=AAAAQE_HRrk:APA91bFvHMNif0Mi8obEoMMH-2wYNx3rKbbCwE79b6lJKgNKVn_OCCSnRLNd_wFrm0H5qdOmvdgVymaSFsP0vOyavB4sUPgg5KTgmwo3ZLfzT1KqV1MPIVYlu-vENvsj1ah-oF3Xst4-"
            ,"Content-Type: application/json"})
    @POST("fcm/send")
    Call<PushNotification> sendNotification(@Body PushNotification notification);
}

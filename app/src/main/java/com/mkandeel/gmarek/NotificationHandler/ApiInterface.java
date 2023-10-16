package com.mkandeel.gmarek.NotificationHandler;

import static com.mkandeel.gmarek.NotificationHandler.Constants.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {
    @Headers({"Authorization: key=AAAAQE_HRrk:APA91bGh0F5__bbjIjFfN3cfMFrGPoBCArZ_Vv7CA6G1l6Vuume3UpXvWOE3R_Sta9iZ8mXUYFRbz_97XWxQYBCLOPW3v_hKZ2dGKG3TVsbb6X-eqEyj67YBOCRByyZtVAqG_OFi-iqd"
            ,"Content-Type: application/json"})
    @POST("fcm/send")
    Call<PushNotification> sendNotification(@Body PushNotification notification);
}

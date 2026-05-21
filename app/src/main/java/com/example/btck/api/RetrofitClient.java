package com.example.btck.api;

import android.content.Context;
import com.example.btck.managers.TokenManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // Change this to your actual backend URL
    public static final String BASE_URL = "http://e1.chiasegpu.vn:24548/api/v1/";
    // For real device on same network, use your PC's IP: "http://192.168.x.x:8000/api/v1/"

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static TokenManager tokenManager = null;

    public static void init(Context context) {
        tokenManager = new TokenManager(context);
        retrofit = null;
        apiService = null;
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(tokenManager))
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }

    public static void reset() {
        retrofit = null;
        apiService = null;
    }
}

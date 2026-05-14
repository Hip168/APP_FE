package com.example.btck;

import android.app.Application;
import com.example.btck.api.RetrofitClient;

public class BTCKApplication extends Application {

    private static BTCKApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Khởi tạo RetrofitClient khi app start
        RetrofitClient.init(this);
    }

    public static BTCKApplication getInstance() {
        return instance;
    }
}

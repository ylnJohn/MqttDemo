package com.yln.mqttdemo;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;


public class MqttApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        MqttAppState.getInstance();
        MqttAppState.setApplication(context);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MqttAppState.getInstance().onTerminate();
    }

}

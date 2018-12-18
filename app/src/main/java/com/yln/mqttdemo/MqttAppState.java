package com.yln.mqttdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

/**
 * 全局app
 */
public class MqttAppState {
    private static boolean debug=true;
    private static Toast mToast;
    private static View mToastView;
    private static Context sContext;
    private static MqttAppState instance = null;
    private static String imei;
    // 语音合成对象
    private static Handler mHandler=new Handler();
    public static Context getApplication() {
        return sContext;
    }
    public static void setApplication(Context application) {
        MqttAppState.sContext = application;
    }

    public void onTerminate() {
        mHandler.removeCallbacksAndMessages(null);
    }
    public static boolean isDebug() {
        return debug;
    }
    public static void setDebug(boolean debug) {
        MqttAppState.debug = debug;
    }
    public void showToast(String content) {
        if (mToast == null) {
            mToast = new Toast(sContext);
        }
        if (mToastView == null) {
            mToastView = Toast.makeText(sContext, "", Toast.LENGTH_SHORT).getView();
        }
//        mToast.cancel();
        mToast.setView(mToastView);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.setText(content);
        mToast.show();
    }
    public void showToast(int resid) {
        if (mToast == null) {
            mToast = new Toast(sContext);
        }
        if (mToastView == null) {
            mToastView = Toast.makeText(sContext, "", Toast.LENGTH_SHORT).getView();
        }
//        mToast.cancel();
        mToast.setView(mToastView);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.setText(resid);
        mToast.show();
    }

    public static MqttAppState getInstance() {
        if (instance == null) {
            synchronized (MqttAppState.class) {
                if (instance == null) {
                    instance = new MqttAppState();
                }
            }
        }
        return instance;
    }

    public String getIMEI() {
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(sContext, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 26) {
                    imei=telephonyManager.getImei();
                    return imei;
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    int count = telephonyManager.getPhoneCount();
                    if (count > 1) {
                        imei = telephonyManager.getDeviceId(0);
                        return imei;
                    } else {
                        imei = telephonyManager.getDeviceId();
                        return imei;
                    }
                } else {
                    imei = telephonyManager.getDeviceId();
                    return imei;
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

}

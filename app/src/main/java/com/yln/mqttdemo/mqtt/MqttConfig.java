package com.yln.mqttdemo.mqtt;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yln.mqttdemo.BuildConfig;
import com.yln.mqttdemo.Constants;
import com.yln.mqttdemo.MqttAppState;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by fc on 2018/11/13.
 */

public class MqttConfig {

    private String TAG = "mqtt";

    /**MQTT配置参数**/
    private static String host = Constants.MQTT_IP;
    private static String port = Constants.MQTT_PORT;
    private static String userID = "";
    private static String passWord = "";
    private static String clientID = MqttAppState.getInstance().getIMEI();
    private int mqttRetryCount=0;


    /**MQTT状态信息**/
    private boolean isConnect = false;

    /**MQTT支持类**/
    private MqttAsyncClient mqttClient=null;

    private MqttListener mMqttListener;

    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.arg1) {
                case MqttTag.MQTT_STATE_CONNECTED:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: connected");
                    mMqttListener.onConnected();
                    mqttRetryCount=0;
                    break;
                case MqttTag.MQTT_STATE_FAIL:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: fail");
                    mMqttListener.onFail();
                    break;
                case MqttTag.MQTT_STATE_LOST:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: lost");
                    mMqttListener.onLost();
                    break;
                case MqttTag.MQTT_STATE_RECEIVE:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: receive");
                    MqttObject object= (MqttObject) message.obj;
                    mMqttListener.onReceive(object.getTopic(),object.getMessage());
                    break;
                case MqttTag.MQTT_STATE_SEND_SUCC:
                    if (BuildConfig.DEBUG) Log.d(TAG, "handleMessage: send");
                    mMqttListener.onSendSucc();
                    break;
            }
            return true;
        }
    });

    /**
     * 自带的监听类，判断Mqtt活动变化
     */
    private IMqttActionListener mIMqttActionListener=new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            isConnect=true;
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_CONNECTED;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            isConnect=false;
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_FAIL;
            mHandler.sendMessage(msg);
        }
    };

    /**
     * 自带的监听回传类
     */
    private MqttCallback mMqttCallback=new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            isConnect=false;
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_LOST;
            mHandler.sendMessage(msg);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_RECEIVE;
            String content=new String(message.getPayload());
            MqttObject object=new MqttObject();
            object.setTopic(topic);
            object.setMessage(content);
            msg.obj=object;
            mHandler.sendMessage(msg);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Message msg=new Message();
            msg.arg1=MqttTag.MQTT_STATE_SEND_SUCC;
            mHandler.sendMessage(msg);
        }
    };

    public MqttConfig(MqttListener lis){
        mMqttListener=lis;
    }

    public static void setMqttSetting(String host, String port, String userID, String passWord, String clientID){
        MqttConfig.host = host;
        MqttConfig.port = port;
        MqttConfig.userID = userID;
        MqttConfig.passWord = passWord;
        MqttConfig.clientID = clientID;
    }

    /**
     * 进行Mqtt连接
     */
    public void connectMqtt(){
        try {
            mqttClient=new MqttAsyncClient("tcp://"+this.host+":"+this.port ,
                    "ClientID"+this.clientID, new MemoryPersistence());
            mqttClient.connect(getOptions(), null, mIMqttActionListener);
            mqttClient.setCallback(mMqttCallback);
        } catch (MqttException e) {
            Log.e("yaolinnan","e:",e);
//            e.printStackTrace();
        }
    }

    /**
     * 断开Mqtt连接重新连接
     */
    public void reStartMqtt(){
        if(mqttRetryCount<5) {
            disConnectMqtt();
            connectMqtt();
            mqttRetryCount++;
        }else {
            Log.i(TAG,"mqtt server reconnect error!");
        }
    }

    /**
     * 断开Mqtt连接
     */
    public void disConnectMqtt(){
        try {
            mqttClient.disconnect();
            mqttClient = null;
            isConnect = false;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向Mqtt服务器发送数据
     */
    public void pubMsg(String Topic, String Msg, int Qos){
        if(!isConnect){
            Log.d(TAG,"Mqtt连接未打开");
            return;
        }
        try {
            /** Topic,Msg,Qos,Retained**/
            mqttClient.publish(Topic,Msg.getBytes(),Qos,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向Mqtt服务器发送数据
     */
    public void pubMsg(String Topic, byte[] Msg, int Qos){
        if(!isConnect){
            Log.d(TAG,"Mqtt连接未打开");
            return;
        }
        try {
            /** Topic,Msg,Qos,Retained**/
            mqttClient.publish(Topic,Msg,Qos,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向Mqtt服务器订阅某一个Topic
     */
    public void subTopic(String Topic, int Qos){
        if(!isConnect){
            Log.d(TAG,"Mqtt连接未打开");
            return;
        }
        try {
            mqttClient.subscribe(Topic,Qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subTopic(String[] Topic, int[] Qos){
        if(!isConnect){
            Log.d(TAG,"Mqtt连接未打开");
            return;
        }
        try {
            mqttClient.subscribe(Topic,Qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Mqtt的连接信息
     */
    private MqttConnectOptions getOptions(){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);//重连不保持状态
        if(this.userID!=null&&this.userID.length()>0&&this.passWord!=null&&this.passWord.length()>0){
            options.setUserName(this.userID);//设置服务器账号密码
            options.setPassword(this.passWord.toCharArray());
        }
        options.setConnectionTimeout(10);//设置连接超时时间
        options.setKeepAliveInterval(20);//设置保持活动时间，超过时间没有消息收发将会触发ping消息确认
//        options.setAutomaticReconnect(true);
//        options.setCleanSession(false);
        return options;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public static String getClientID() {
        return clientID;
    }

    class MqttObject{
        String topic;
        String message;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

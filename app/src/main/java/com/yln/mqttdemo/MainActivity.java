package com.yln.mqttdemo;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.yln.mqttdemo.mqtt.MqttListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttListener{

    private RecyclerView mMessageRV;
    private EditText mMessageET;
    private Button mSendBtn;
    private Gson gson=new Gson();
    private List<MqttMessage> messageList=new ArrayList<>();
    private MqttAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MqttService.addMqttListener(this);
        Intent intent=new Intent(this, MqttService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }else {
            startService(intent);
        }
        mMessageRV=findViewById(R.id.swipe_target);
        mMessageET=findViewById(R.id.send_edit);
        mSendBtn=findViewById(R.id.send_button);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=mMessageET.getText().toString().trim();
                if(!TextUtils.isEmpty(message)){
                    MqttMessage mqttMessage=new MqttMessage();
                    mqttMessage.setUserid(Constants.MQTT_USERID);
                    mqttMessage.setMessage(message);
                    mqttMessage.setUsername(Constants.MQTT_USERNAME);
                    mqttMessage.setSendTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
                    MqttService.getMqttConfig().pubMsg(Constants.MQTT_TOPIC,gson.toJson(mqttMessage),0);
                }else {
                    MqttAppState.getInstance().showToast("不可发送为空的消息");
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessageRV.setLayoutManager(layoutManager);
        layoutManager.setReverseLayout(true);//倒序显示
        layoutManager.setStackFromEnd(true);
        mAdapter=new MqttAdapter(this,messageList);
        mMessageRV.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MqttService.removeMqttListener(this);
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onFail() {

    }

    @Override
    public void onLost() {

    }

    @Override
    public void onReceive(String topic, String message) {
        if(topic.equals(Constants.MQTT_TOPIC)){
            MqttMessage mqttMessage=gson.fromJson(message,MqttMessage.class);
            if(messageList.size()>0)
                messageList.add(0,mqttMessage);
            else
                messageList.add(mqttMessage);
            mAdapter.setList(messageList);
            mMessageRV.scrollToPosition(0);
        }
    }

    @Override
    public void onSendSucc() {
        MqttAppState.getInstance().showToast("消息发送成功");
        mMessageET.setText("");
        mMessageET.clearFocus();
    }
}

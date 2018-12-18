package com.yln.mqttdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by fc on 2018/12/18.
 */

public class MqttAdapter extends RecyclerView.Adapter<MqttAdapter.ViewHolder>{

    private Context mContext;
    private List<MqttMessage> messageList;
    private int ITEM_MESSAGE_ME=0;
    private int ITEM_MESSAGE_OTHER=1;

    public MqttAdapter(Context context,List<MqttMessage> list){
        mContext=context;
        messageList=list;
    }

    public void setList(List<MqttMessage> list){
        messageList=list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==ITEM_MESSAGE_ME){
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_message_me,null,false);
            return new ViewHolder(view);
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_message_other,null,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MqttMessage message=messageList.get(position);
        holder.timeTV.setText(message.getSendTime());
        holder.userTV.setText(message.getUsername());
        holder.contentTV.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        if(messageList!=null){
            return messageList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        MqttMessage message=messageList.get(position);
        if(Constants.MQTT_USERID.equals(message.getUserid())){
            return ITEM_MESSAGE_ME;
        }
        return ITEM_MESSAGE_OTHER;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View contentView;
        TextView userTV;
        TextView contentTV;
        TextView timeTV;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView=itemView;
            userTV=contentView.findViewById(R.id.message_user);
            contentTV=contentView.findViewById(R.id.message_text);
            timeTV=contentView.findViewById(R.id.message_time);
        }
    }
}

package com.example.samplechatapp;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatRoomAdapter extends ArrayAdapter<ChatRoomInfo> {
    public ChatRoomAdapter(Context context, int resource, List<ChatRoomInfo> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.chat_room_info, parent, false);
        }

        TextView chatRoomName = (TextView) convertView.findViewById(R.id.chatRoomNameView);
        TextView descriptionView = (TextView) convertView.findViewById(R.id.descriptionView);

        ChatRoomInfo chatRoom = getItem(position);

        chatRoomName.setText(chatRoom.getName());
        descriptionView.setText(chatRoom.getDescription());

        return convertView;
    }
}
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
        TextView lastMessageAtView = (TextView) convertView.findViewById(R.id.lastMessageAtView);

        ChatRoomInfo chatRoom = getItem(position);

        chatRoomName.setText(chatRoom.getName());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date lastMessageDate = new Date(chatRoom.getTimestamp());
        String lastMessageAt = DateFormat.getInstance().format(lastMessageDate);
        lastMessageAtView.setText("Last message at: " + lastMessageAt);

        return convertView;
    }
}
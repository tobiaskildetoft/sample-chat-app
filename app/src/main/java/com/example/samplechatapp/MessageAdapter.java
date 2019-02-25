package com.example.samplechatapp;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<ChatMessage> {
    public MessageAdapter(Context context, int resource, List<ChatMessage> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);

        ImageView avatarImageView = convertView.findViewById(R.id.avatarImageView);
        ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = convertView.findViewById(R.id.nameTextView);
        TextView timestampTextView = convertView.findViewById(R.id.timestampTextView);

        ChatMessage message = getItem(position);


        if (message.getPhotoUrl() != null) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());
        // Negative timestamp indicates that the message is a dummy
        if (message.getTimestamp() >= 0) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date timestampDate = new Date(message.getTimestamp());
            String timestampString = DateFormat.getInstance().format(timestampDate);
            timestampTextView.setText(timestampString);
        } else {
            timestampTextView.setVisibility(View.GONE);
        }

        if (message.getAvatarUrl() != null) {
            avatarImageView.setVisibility(View.VISIBLE);
            Glide.with(avatarImageView.getContext())
                    .load(message.getAvatarUrl())
                    .into(avatarImageView);
        } else {
            avatarImageView.setVisibility(View.GONE);
        }

        return convertView;
    }
}

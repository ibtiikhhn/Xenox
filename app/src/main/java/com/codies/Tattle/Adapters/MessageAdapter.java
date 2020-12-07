package com.codies.Tattle.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codies.Tattle.Models.Chat;
import com.codies.Tattle.R;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    public static final int MSGLEFT = 0;
    public static final int MSGRIGHT = 1;
    List<Chat> mChat;
    Context context;
    String userId;

    public MessageAdapter(Context context, String userId) {
        this.userId = userId;
        this.context = context;
        mChat = new ArrayList<>();
    }

    public void setList(List<Chat> chats) {
        this.mChat = chats;
        Log.i("CHAT", "setList: " + chats.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSGRIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (mChat.get(position).getSender().equals(userId)) {
            return MSGRIGHT;
        } else {
            return MSGLEFT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        Log.i("Chat", "onBindViewHolder: " + chat.getMessage());
        holder.showMessage.setText(chat.getMessage());

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView showMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.chatText);
        }
    }
}
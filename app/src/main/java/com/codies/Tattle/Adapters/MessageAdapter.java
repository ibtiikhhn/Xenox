package com.codies.Tattle.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codies.Tattle.Models.Chat;
import com.codies.Tattle.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    public static final int MSGLEFT = 0;
    public static final int MSGRIGHT = 1;
    List<Chat> chatList;
    Context context;
    String userId;

    public MessageAdapter(Context context, String userId, List<Chat> chats) {
        this.userId = userId;
        this.context = context;
        this.chatList = chats;
    }

    /*public void setList(List<Chat> chats) {
        this.mChat = chats;
        Log.i("CHAT", "setList: " + chats.size());
        notifyDataSetChanged();
    }*/

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
        if (chatList.get(position).getSender().equals(userId)) {
            return MSGRIGHT;
        } else {
            return MSGLEFT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        if (chat.isImage()) {
            holder.showMessage.setVisibility(View.GONE);
            Glide.with(context).load(chat.getMessage()).into(holder.chatIMG);
        } else {
            holder.chatIMG.setVisibility(View.GONE);
            holder.showMessage.setText(chat.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView showMessage;
        ImageView chatIMG;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.chatText);
            chatIMG = itemView.findViewById(R.id.chatIV);
        }
    }
}
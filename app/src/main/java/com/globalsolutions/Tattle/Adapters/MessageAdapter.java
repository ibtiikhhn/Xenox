package com.globalsolutions.Tattle.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.globalsolutions.Tattle.Models.Chat;
import com.globalsolutions.Tattle.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int MSGLEFTIMAGE = 0;
    public static final int MSGLEFTTEXT = 1;
    public static final int MSGRIGHTIMAGE = 2;
    public static final int MSGRIGHTTEXT = 3;
    List<Chat> chatList;
    Context context;
    String userId;

    public MessageAdapter(Context context, String userId, List<Chat> chats) {
        this.userId = userId;
        this.context = context;
        this.chatList = chats;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSGRIGHTIMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right_image, parent, false);
            return new MessageImageViewHolder(view);
        }

        else if (viewType == MSGRIGHTTEXT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MessageTextViewHolder(view);
        }

        else if (viewType == MSGLEFTIMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left_image, parent, false);
            return new MessageImageViewHolder(view);
        }

        else  {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MessageTextViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        Chat chat = chatList.get(position);
        if (chat.getSender().equals(userId)) {
            if (chat.isImage()) {
                return MSGRIGHTIMAGE;
            } else {
                return MSGRIGHTTEXT;
            }
        } else {
            if (chat.isImage()) {
                return MSGLEFTIMAGE;
            } else {
                return MSGLEFTTEXT;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {

        Chat chat = chatList.get(position);
        if (getItemViewType(position) == MSGLEFTIMAGE) {
            Glide.with(context).load(chat.getMessage()).into(((MessageImageViewHolder) holder).chatIMG);
        } else if (getItemViewType(position) == MSGRIGHTIMAGE) {
            Glide.with(context).load(chat.getMessage()).into(((MessageImageViewHolder) holder).chatIMG);
        } else if (getItemViewType(position) == MSGLEFTTEXT) {
            ((MessageTextViewHolder) holder).showMessage.setText(chat.getMessage());
        }else {
            ((MessageTextViewHolder) holder).showMessage.setText(chat.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MessageImageViewHolder extends RecyclerView.ViewHolder {
        ImageView chatIMG;

        public MessageImageViewHolder(@NonNull View itemView) {
            super(itemView);
            chatIMG = itemView.findViewById(R.id.chatIV);
        }
    }

    public class MessageTextViewHolder extends RecyclerView.ViewHolder {
        TextView showMessage;

        public MessageTextViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.chatText);
        }
    }
}
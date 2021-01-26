package com.globalsolutions.Tattle.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.globalsolutions.Tattle.Interfaces.ChatClickListener;
import com.globalsolutions.Tattle.Models.ChatList;
import com.globalsolutions.Tattle.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    public static final String TAG = "ChatListAdapter";

    ChatClickListener clickListener;
    Context context;
    List<ChatList> userList;
    String myId;
    boolean sendReceiverName = false;

    public ChatListAdapter(Context context, ChatClickListener clickListener, String myId) {
        this.context = context;
        this.clickListener = clickListener;
        this.myId = myId;
        userList = new ArrayList<>();
    }

    public void setList(List<ChatList> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chats_counsellor, parent, false);
        return new ChatListAdapter.ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatList chatList = userList.get(position);
        holder.message.setText(chatList.getLastMessage());
        holder.user.setText(chatList.getOpponentName());
        if (chatList.getOpponentIMG() != null && !chatList.getOpponentIMG().isEmpty()) {
            Glide.with(context).load(userList.get(position).getOpponentIMG()).into(holder.circularImageView);
        }

        /*if (chatList.getSenderId().equals(myId)) {
            sendReceiverName = true;
            holder.user.setText(chatList.getReceiverName());
            if (chatList.getReceiverIMG() != null && !chatList.getReceiverIMG().isEmpty()) {
                Glide.with(context).load(userList.get(position).getReceiverIMG()).into(holder.circularImageView);
            }
        } else {
            sendReceiverName = false;
            holder.user.setText(chatList.getSenderName());
            if (chatList.getSenderIMG() != null && !chatList.getSenderIMG().isEmpty()) {
                Glide.with(context).load(userList.get(position).getSenderIMG()).into(holder.circularImageView);
            }
        }*/

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        CircularImageView circularImageView;
        TextView user;
        TextView message;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            circularImageView = itemView.findViewById(R.id.userIMMG);
            user = itemView.findViewById(R.id.userNAME);
            message = itemView.findViewById(R.id.userMessageTV);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(userList.get(getAdapterPosition()).getSenderId(), userList.get(getAdapterPosition()).getReceiverId(), userList.get(getAdapterPosition()).getCombinedId(), userList.get(getAdapterPosition()).getOpponentName());

                    /*if (sendReceiverName) {
                        clickListener.onClick(userList.get(getAdapterPosition()).getSenderId(), userList.get(getAdapterPosition()).getReceiverId(), userList.get(getAdapterPosition()).getCombinedId(), userList.get(getAdapterPosition()).getReceiverName());
                    } else {
                        clickListener.onClick(userList.get(getAdapterPosition()).getReceiverId(), userList.get(getAdapterPosition()).getSenderId(), userList.get(getAdapterPosition()).getCombinedId(), userList.get(getAdapterPosition()).getSenderName());
                    }*/
                }
            });
        }
    }
}
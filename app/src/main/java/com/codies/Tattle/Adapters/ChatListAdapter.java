package com.codies.Tattle.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codies.Tattle.Interfaces.ChatClickListener;
import com.codies.Tattle.Models.ChatList;
import com.codies.Tattle.Models.User;
import com.codies.Tattle.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    ChatClickListener clickListener;
    Context context;
    List<ChatList> userList;

    public ChatListAdapter(Context context, ChatClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
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
        holder.message.setText(userList.get(position).getLastMessage());
        holder.user.setText(userList.get(position).getSenderName());
        if (userList.get(position).getSenderIMG() != null && !userList.get(position).getSenderIMG().isEmpty()) {
            Glide.with(context).load(userList.get(position).getSenderIMG()).into(holder.circularImageView);
        }
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
                    clickListener.onClick(userList.get(getAdapterPosition()).getReceiverId(),userList.get(getAdapterPosition()).getReceiverId(), userList.get(getAdapterPosition()).getCombinedId());
                }
            });
        }
    }
}
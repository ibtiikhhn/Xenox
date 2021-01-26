package com.globalsolutions.Tattle.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.globalsolutions.Tattle.Interfaces.ChatClickListener;
import com.globalsolutions.Tattle.Models.User;
import com.globalsolutions.Tattle.R;
import com.globalsolutions.Tattle.UI.Activities.SearchActivity;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    public static final String TAG = "ChatListAdapter";

    Context context;
    List<User> userList;
    String myId;
    ChatClickListener clickListener;
    boolean sendReceiverName = false;

    public SearchAdapter(Context context) {
        this.context = context;
        userList = new ArrayList<>();
    }

    public void setList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cv_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userEmail.setText(user.getEmail());
        if (user.getImageUrl() != null) {
            Glide.with(context.getApplicationContext()).load(user.getImageUrl()).into(holder.userImg);
        }
        holder.chatBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof SearchActivity) {
                    ((SearchActivity)context).initChat(user.getUserId());
                }
            }
        });

        holder.audioCallBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof SearchActivity) {
                    ((SearchActivity)context).initAudioCall(user.getEmail());
                }
            }
        });

        holder.videoCallBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof SearchActivity) {
                    ((SearchActivity)context).initVideoCall(user.getEmail());
                }
            }
        });

        holder.userImg.setClickable(true);
        holder.userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircularImageView userImg;
        TextView userName;
        TextView userEmail;
        ImageButton chatBt;
        ImageButton audioCallBt;
        ImageButton videoCallBt;

        public ViewHolder(@NonNull View includeView) {
            super(includeView);
            userImg = includeView.findViewById(R.id.searchImage);
            userName = includeView.findViewById(R.id.searchNametv);
            userEmail = includeView.findViewById(R.id.searchEmailtv);
            chatBt = includeView.findViewById(R.id.searchChatBT);
            audioCallBt = includeView.findViewById(R.id.searchAudioCallBT);
            videoCallBt = includeView.findViewById(R.id.searchVideoCallBT);
        }
    }
}

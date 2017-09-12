package com.google.firebase.udacity.friendlychat;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.udacity.friendlychat.common.models.FriendlyMessage;

import java.util.List;

import static java.security.AccessController.getContext;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<FriendlyMessage> messages;
    private Context context;

    public MessageAdapter(Context context, List<FriendlyMessage> messages) {
        this.messages = messages;
        this.context = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        boolean isPhoto = this.messages.get(position).getPhotoUrl() != null;
        if (isPhoto) {
            holder.txtMessage.setVisibility(View.GONE);
            holder.imgPhoto.setVisibility(View.VISIBLE);
            Glide.with(holder.imgPhoto.getContext())
                    .load(this.messages.get(position).getPhotoUrl())
                    .into(holder.imgPhoto);
        } else {
            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.imgPhoto.setVisibility(View.GONE);
            holder.txtMessage.setText(this.messages.get(position).getText());
        }
        holder.txtName.setText(this.messages.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPhoto;
        private TextView txtMessage, txtName;

        private ViewHolder(View itemView) {
            super(itemView);
            imgPhoto = (ImageView) itemView.findViewById(R.id.imgPhoto);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
        }
    }
}

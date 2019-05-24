package io.codelabs.digitutor.view.adapter.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.codelabs.digitutor.R;
import io.codelabs.widget.BaselineGridTextView;
import io.codelabs.widget.CircularImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {
    public CircularImageView avatar;
    public BaselineGridTextView username, info;


    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        this.avatar = itemView.findViewById(R.id.user_avatar);
        this.username = itemView.findViewById(R.id.user_name);
        this.info = itemView.findViewById(R.id.user_info);
    }
}

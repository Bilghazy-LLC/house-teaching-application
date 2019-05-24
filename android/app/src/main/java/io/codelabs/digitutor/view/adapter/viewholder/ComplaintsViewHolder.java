package io.codelabs.digitutor.view.adapter.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.codelabs.digitutor.R;
import io.codelabs.widget.BaselineGridTextView;
import io.codelabs.widget.CircularImageView;

public class ComplaintsViewHolder extends RecyclerView.ViewHolder {
    public CircularImageView avatar;
    public BaselineGridTextView username, complaint;


    public ComplaintsViewHolder(@NonNull View itemView) {
        super(itemView);
        this.avatar = itemView.findViewById(R.id.user_avatar);
        this.username = itemView.findViewById(R.id.user_name);
        this.complaint = itemView.findViewById(R.id.complaint);
    }
}

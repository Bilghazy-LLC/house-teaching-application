package io.codelabs.digitutor.view.adapter.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.codelabs.digitutor.R;
import io.codelabs.widget.BaselineGridTextView;

/**
 * ViewHolder subclass for showing all subjects
 */
public class SubjectViewHolder extends RecyclerView.ViewHolder {
    public BaselineGridTextView name;
    public BaselineGridTextView desc;

    public SubjectViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.subject_name);
        desc = itemView.findViewById(R.id.subject_desc);
    }
}

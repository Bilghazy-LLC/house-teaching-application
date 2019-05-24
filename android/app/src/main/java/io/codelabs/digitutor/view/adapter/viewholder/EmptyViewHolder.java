package io.codelabs.digitutor.view.adapter.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import io.codelabs.digitutor.R;
import io.codelabs.widget.ForegroundImageView;

/**
 * ViewHolder subclass for showing empty state of a process
 */
public class EmptyViewHolder extends RecyclerView.ViewHolder {
    public ShimmerFrameLayout shimmer;

    public EmptyViewHolder(@NonNull View itemView) {
        super(itemView);
        shimmer = itemView.findViewById(R.id.empty_shimmer);
    }
}

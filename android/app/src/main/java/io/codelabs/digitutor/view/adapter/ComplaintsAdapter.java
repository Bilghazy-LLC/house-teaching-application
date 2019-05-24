package io.codelabs.digitutor.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.core.util.OnClickListener;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.data.model.Complaint;
import io.codelabs.digitutor.view.adapter.viewholder.ComplaintsViewHolder;
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder;
import io.codelabs.sdk.glide.GlideApp;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ComplaintsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Complaint> complaints = new ArrayList<>(0);
    private static final int TYPE_EMPTY = R.layout.item_empty;
    private static final int TYPE_COMPLAINT = R.layout.item_complaint;

    private final LayoutInflater inflater;
    private final BaseActivity context;
    private final OnClickListener<Complaint> listener;
    private final FirebaseFirestore firestore;

    public ComplaintsAdapter(BaseActivity context, OnClickListener<Complaint> listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.firestore = context.firestore;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_EMPTY:
                return new EmptyViewHolder(inflater.inflate(TYPE_EMPTY, parent, false));
            case TYPE_COMPLAINT:
                return new ComplaintsViewHolder(inflater.inflate(TYPE_COMPLAINT, parent, false));
            default:
                throw new IllegalArgumentException("Please pass in a valid viewholder instance or subclass");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return complaints.isEmpty() ? TYPE_EMPTY : TYPE_COMPLAINT;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ComplaintsViewHolder) bindUserViewHolder((ComplaintsViewHolder) holder, position);
        else bindEmptyViewHolder((EmptyViewHolder) holder);
    }

    private void bindEmptyViewHolder(EmptyViewHolder holder) {
        holder.shimmer.startShimmer();
    }

    private void bindUserViewHolder(ComplaintsViewHolder holder, int position) {
        Complaint complaint = complaints.get(position);
        holder.complaint.setText(complaint.getDescription());

        // Get the parent information from the database
        FirebaseDataSource.getUser(context, firestore, complaint.getParent(), BaseUser.Type.PARENT, new AsyncCallback<BaseUser>() {
            @Override
            public void onError(@Nullable String error) {

            }

            @Override
            public void onSuccess(@Nullable BaseUser response) {
                if (response != null) {
                    // Load profile image
                    GlideApp.with(context)
                            .load(response.getAvatar())
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .circleCrop()
                            .priority(Priority.IMMEDIATE)
                            .placeholder(R.drawable.avatar_placeholder)
                            .error(R.drawable.ic_player)
                            .transition(withCrossFade())
                            .into(holder.avatar);
                    holder.username.setText(response.getName());
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete() {

            }
        });


        holder.itemView.setOnLongClickListener(v -> {
            listener.onClick(complaint, true);
            return true;
        });

        holder.itemView.setOnClickListener(v -> listener.onClick(complaint, false));
    }

    @Override
    public int getItemCount() {
        return complaints.isEmpty() ? 1 : complaints.size();
    }

    public void addData(List<Complaint> complaints) {
        this.complaints.clear();
        this.complaints.addAll(complaints);
        notifyDataSetChanged();
    }
}

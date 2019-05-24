package io.codelabs.digitutor.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.util.OnClickListener;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder;
import io.codelabs.digitutor.view.adapter.viewholder.UserViewHolder;
import io.codelabs.sdk.glide.GlideApp;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<BaseUser> users = new ArrayList<>(0);
    private static final int TYPE_EMPTY = R.layout.item_empty;
    private static final int TYPE_USER = R.layout.item_user;

    private final LayoutInflater inflater;
    private final Context context;
    private final OnClickListener<BaseUser> listener;

    public UsersAdapter(Context context, OnClickListener<BaseUser> listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_EMPTY:
                return new EmptyViewHolder(inflater.inflate(TYPE_EMPTY, parent, false));
            case TYPE_USER:
                return new UserViewHolder(inflater.inflate(TYPE_USER, parent, false));
            default:
                throw new IllegalArgumentException("Please pass in a valid viewholder instance or subclass");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return users.isEmpty() ? TYPE_EMPTY : TYPE_USER;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewHolder) bindUserViewHolder((UserViewHolder) holder, position);
        else bindEmptyViewHolder((EmptyViewHolder) holder);
    }

    private void bindEmptyViewHolder(EmptyViewHolder holder) {
        holder.shimmer.startShimmer();
    }

    private void bindUserViewHolder(UserViewHolder holder, int position) {

        BaseUser user = users.get(position);
        holder.username.setText(user.getName());
        holder.info.setText(user.getEmail()); // TODO: 005 05.05.19 Get additional information about this parent (Like date added and so on)

        // Load profile image
        GlideApp.with(context)
                .load(user.getAvatar())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop()
                .priority(Priority.IMMEDIATE)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.ic_player)
                .transition(withCrossFade())
                .into(holder.avatar);

        holder.itemView.setOnLongClickListener(v -> {
            listener.onClick(user, true);
            return true;
        });

        holder.itemView.setOnClickListener(v -> listener.onClick(user, false));
    }

    @Override
    public int getItemCount() {
        return users.isEmpty() ? 1 : users.size();
    }

    public void addData(List<? extends BaseUser> users) {
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }
}

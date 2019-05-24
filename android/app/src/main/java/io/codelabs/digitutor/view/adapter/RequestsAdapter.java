package io.codelabs.digitutor.view.adapter;

import android.os.Bundle;
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
import io.codelabs.digitutor.data.model.Parent;
import io.codelabs.digitutor.data.model.Request;
import io.codelabs.digitutor.view.UserActivity;
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder;
import io.codelabs.digitutor.view.adapter.viewholder.UserViewHolder;
import io.codelabs.sdk.glide.GlideApp;
import io.codelabs.sdk.util.ExtensionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class RequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Request> requests = new ArrayList<>(0);
    private static final int TYPE_EMPTY = R.layout.item_empty;
    private static final int TYPE_USER = R.layout.item_user;

    private final LayoutInflater inflater;
    private final BaseActivity context;
    private final OnClickListener<Request> listener;
    private final FirebaseFirestore firestore;

    public RequestsAdapter(BaseActivity context, OnClickListener<Request> listener, FirebaseFirestore firestore) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.firestore = firestore;
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
        return requests.isEmpty() ? TYPE_EMPTY : TYPE_USER;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewHolder) bindUserViewHolder((UserViewHolder) holder, position);
        else bindEmptyViewHolder((EmptyViewHolder) holder);
    }

    private void bindEmptyViewHolder(EmptyViewHolder holder) {
        holder.shimmer.startShimmer();
    }

    private void bindUserViewHolder(@NotNull UserViewHolder holder, int position) {
        Request request = requests.get(position);

        FirebaseDataSource.getUser(context, context.firestore, request.getParent(), BaseUser.Type.PARENT, new AsyncCallback<BaseUser>() {
            @Override
            public void onError(@Nullable String error) {
                ExtensionUtils.debugLog(context, error);
            }

            @Override
            public void onSuccess(@Nullable BaseUser response) {
                ExtensionUtils.debugLog(context, "Request User" + response);
                if (response != null) {
                    holder.username.setText(Objects.requireNonNull(response).getName());
                    holder.info.setText(String.format(Locale.getDefault(), "%d ward(s)", ((Parent) response).getWards().size()));

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

                    holder.avatar.setOnClickListener(v -> {
                        Bundle bundle = new Bundle(0);
                        bundle.putString(UserActivity.EXTRA_USER_TYPE, BaseUser.Type.PARENT);
                        bundle.putString(UserActivity.EXTRA_USER_UID, request.getParent());
                        ((BaseActivity) context).intentTo(UserActivity.class, bundle, false);
                    });
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
            listener.onClick(request, true);
            return true;
        });

        holder.itemView.setOnClickListener(v -> listener.onClick(request, false));
    }

    @Override
    public int getItemCount() {
        return requests.isEmpty() ? 1 : requests.size();
    }

    public void addData(List<Request> requests) {
        this.requests.clear();
        this.requests.addAll(requests);
        notifyDataSetChanged();
    }
}

package io.codelabs.digitutor.view.adapter.viewholder;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.data.model.Feedback;
import io.codelabs.sdk.glide.GlideApp;
import io.codelabs.widget.BaselineGridTextView;
import io.codelabs.widget.ForegroundImageView;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

public class FeedbackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_DATA = R.layout.item_feedback;
    private static final int TYPE_EMPTY = R.layout.item_empty;

    private List<Feedback> dataSource = new ArrayList<>(0);
    private final BaseActivity host;
    private final LayoutInflater inflater;

    public FeedbackAdapter(BaseActivity host) {
        this.host = host;
        this.inflater = LayoutInflater.from(host);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_DATA:
                return new FeedbackViewHolder(inflater.inflate(TYPE_DATA, parent, false));
            case TYPE_EMPTY:
                return new EmptyViewHolder(inflater.inflate(TYPE_EMPTY, parent, false));
            default:
                throw new IllegalArgumentException("Cannot find this viewholder instance");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_DATA) {
            bindDataViewHolder((FeedbackViewHolder) holder, dataSource.get(position));
        } else if (getItemViewType(position) == TYPE_EMPTY) {
            ((EmptyViewHolder) holder).shimmer.startShimmer();
        }
    }

    private void bindDataViewHolder(FeedbackViewHolder holder, Feedback feedback) {

        // Get the current tutor's information
        FirebaseDataSource.getUser(host, host.firestore, feedback.getTutor(), BaseUser.Type.TUTOR, new AsyncCallback<BaseUser>() {
            @Override
            public void onError(@Nullable String error) {
                System.out.println("Debugger : Unable to get this tutor's feedback: " + feedback.getTutor());
            }

            @Override
            public void onSuccess(@Nullable BaseUser response) {
                if (response != null) {
                    GlideApp.with(host)
                            .asBitmap()
                            .load(response.getAvatar())
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .placeholder(R.drawable.avatar_placeholder)
                            .fallback(R.drawable.avatar_placeholder)
                            .priority(Priority.IMMEDIATE)
                            .transition(withCrossFade())
                            .into(holder.avatar);

                    holder.username.setText(response.getName());
                    holder.feedback.setText(feedback.getMessage());
                    holder.timestamp.setText(DateUtils.getRelativeTimeSpanString(feedback.getTimestamp(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));

                    holder.itemView.setOnClickListener(v -> {
                        //do nothing
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

    }

    @Override
    public int getItemViewType(int position) {
        return dataSource.isEmpty() ? TYPE_EMPTY : TYPE_DATA;
    }

    @Override
    public int getItemCount() {
        return dataSource.isEmpty() ? 1 : dataSource.size();
    }

    public void addData(List<Feedback> feedbacks) {
        this.dataSource.clear();
        this.dataSource.addAll(feedbacks);
        notifyDataSetChanged();
    }


    public class FeedbackViewHolder extends RecyclerView.ViewHolder {
        ForegroundImageView avatar;
        BaselineGridTextView username, feedback, timestamp;

        FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.user_avatar);
            username = itemView.findViewById(R.id.user_name);
            feedback = itemView.findViewById(R.id.feedback);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}

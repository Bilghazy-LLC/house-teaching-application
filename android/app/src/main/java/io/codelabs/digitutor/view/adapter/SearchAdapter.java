package io.codelabs.digitutor.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.util.OnClickListener;
import io.codelabs.digitutor.data.BaseDataModel;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.data.model.Complaint;
import io.codelabs.digitutor.data.model.Report;
import io.codelabs.digitutor.data.model.Subject;
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder;
import io.codelabs.digitutor.view.adapter.viewholder.SubjectViewHolder;
import io.codelabs.digitutor.view.adapter.viewholder.UserViewHolder;
import io.codelabs.sdk.glide.GlideApp;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EMPTY = R.layout.item_empty;
    private static final int TYPE_OTHER = R.layout.item_subject;
    private static final int TYPE_USER = R.layout.item_user;

    private final OnClickListener<BaseDataModel> listener;
    private final Context context;
    private final LayoutInflater inflater;
    private List<BaseDataModel> models = new ArrayList<>(0);

    public SearchAdapter(OnClickListener<BaseDataModel> listener, Context context) {
        this.listener = listener;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (models.isEmpty()) return TYPE_EMPTY;
        else if (models.get(position) instanceof BaseUser)
            return TYPE_USER;
        else return TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_EMPTY:
                return new EmptyViewHolder(inflater.inflate(TYPE_EMPTY, parent, false));
            case TYPE_OTHER:
                return new SubjectViewHolder(inflater.inflate(TYPE_OTHER, parent, false));
            case TYPE_USER:
                return new UserViewHolder(inflater.inflate(TYPE_USER, parent, false));
            default:
                throw new IllegalArgumentException("Please pass in a valid ");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_EMPTY:
                bindEmptyViewHolder((EmptyViewHolder) holder);
                break;
            case TYPE_OTHER:
                bindOtherViewHolder((SubjectViewHolder) holder, position);
                break;
            case TYPE_USER:
                bindUserViewHolder((UserViewHolder) holder, position);
                break;
        }
    }

    private void bindUserViewHolder(UserViewHolder holder, int position) {
        BaseDataModel dataModel = models.get(position);

        holder.itemView.setOnClickListener(v -> listener.onClick(dataModel, false));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onClick(dataModel, true);
            return true;
        });

        if (dataModel instanceof BaseUser) {
            holder.username.setText(((BaseUser) dataModel).getName());
            holder.info.setText(((BaseUser) dataModel).getEmail());

            GlideApp.with(context)
                    .asDrawable()
                    .load(((BaseUser) dataModel).getAvatar())
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.ic_player)
                    .circleCrop()
                    .into(holder.avatar);
        }
    }

    private void bindOtherViewHolder(SubjectViewHolder holder, int position) {
        BaseDataModel dataModel = models.get(position);

        if (dataModel instanceof Subject) {
            holder.name.setText(((Subject) dataModel).getName());
            holder.desc.setText(((Subject) dataModel).getDescription().isEmpty() ? "N/A" : ((Subject) dataModel).getDescription());
        } else if (dataModel instanceof Complaint) {
            holder.name.setText("Complaint");   // TODO: 006 06.05.19 Add better description for complaints
            holder.desc.setText(((Complaint) dataModel).getDescription());

        } else if (dataModel instanceof Report) {
            // TODO: 006 06.05.19 Bind reports
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(dataModel, false));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onClick(dataModel, true);
            return true;
        });
    }

    private void bindEmptyViewHolder(EmptyViewHolder holder) {
        holder.shimmer.startShimmer();
    }

    @Override
    public int getItemCount() {
        return models.isEmpty() ? 1 : models.size();
    }

    /**
     * Add new data through this method
     *
     * @param models new models to add to the existing one
     */
    public void addData(List<? extends BaseDataModel> models) {
        this.models.clear();
        this.models.addAll(models);
        notifyDataSetChanged();
    }
}

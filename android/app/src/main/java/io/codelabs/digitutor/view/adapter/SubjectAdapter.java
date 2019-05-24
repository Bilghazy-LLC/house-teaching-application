package io.codelabs.digitutor.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.util.OnClickListener;
import io.codelabs.digitutor.data.model.Subject;
import io.codelabs.digitutor.view.adapter.viewholder.EmptyViewHolder;
import io.codelabs.digitutor.view.adapter.viewholder.SubjectViewHolder;
import io.codelabs.sdk.glide.GlideApp;

/**
 * {@link androidx.recyclerview.widget.RecyclerView.Adapter} subclass for all {@linkplain Subject}s
 */
public class SubjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Subject> subjects = new ArrayList<>(0);
    private Context context;
    private final LayoutInflater inflater;
    private final OnClickListener<Subject> listener;

    private static final int TYPE_EMPTY = R.layout.item_empty;
    private static final int TYPE_SUBJECT = R.layout.item_subject;

    // Constructor
    public SubjectAdapter(Context context, OnClickListener<Subject> listener) {
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
            case TYPE_SUBJECT:
                return new SubjectViewHolder(inflater.inflate(TYPE_SUBJECT, parent, false));
            default:
                throw new IllegalArgumentException("Pass in a valid ViewHolder subclass");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_SUBJECT) {
            bindSubjectViewHolder((SubjectViewHolder) holder, position);
        } else if (getItemViewType(position) == TYPE_EMPTY) {
            bindEmptyViewHolder((EmptyViewHolder) holder);
        }
    }

    private void bindEmptyViewHolder(@NotNull EmptyViewHolder holder) {
        holder.shimmer.startShimmer();
    }

    private void bindSubjectViewHolder(@NotNull SubjectViewHolder holder, int position) {
        // Get the subject for each position in the list
        Subject subject = subjects.get(position);

        // Bind props
        holder.name.setText(subject.getName());
        holder.desc.setText(TextUtils.isEmpty(subject.getDescription()) ? "N/A" : subject.getDescription());

        holder.itemView.setOnClickListener(v -> listener.onClick(subject, false));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onClick(subject, true);
            return true;
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (subjects.isEmpty()) return TYPE_EMPTY;
        return TYPE_SUBJECT;
    }

    @Override
    public int getItemCount() {
        if (subjects.isEmpty()) return 1;
        return subjects.size();
    }

    /**
     * Add new subjects here
     *
     * @param newSubjects Subjects to be added
     */
    public void addData(@NotNull List<Subject> newSubjects) {
        if (newSubjects.isEmpty()) return;
        /*boolean add = true;

        for (Subject s : newSubjects) {
            add = !subjects.contains(s);

            if (add) {
                subjects.add(s);
                notifyItemRangeChanged(0, newSubjects.size());
            }
        }*/
        subjects.clear();
        subjects.addAll(newSubjects);
        notifyDataSetChanged();
    }


}

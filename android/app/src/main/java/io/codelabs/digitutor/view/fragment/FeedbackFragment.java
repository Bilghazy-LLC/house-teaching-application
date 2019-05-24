package io.codelabs.digitutor.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.TransitionManager;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.data.model.Feedback;
import io.codelabs.digitutor.databinding.FragmentWithListBinding;
import io.codelabs.digitutor.view.adapter.viewholder.FeedbackAdapter;
import io.codelabs.recyclerview.GridItemDividerDecoration;
import io.codelabs.recyclerview.SlideInItemAnimator;
import io.codelabs.sdk.util.ExtensionUtils;

import java.util.List;

public class FeedbackFragment extends Fragment {
    private FragmentWithListBinding binding;
    private FeedbackAdapter adapter;
    private BaseActivity host;

    public FeedbackFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_with_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        host = (BaseActivity) requireActivity();

        adapter = new FeedbackAdapter(host);
        binding.grid.setAdapter(adapter);
        binding.grid.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.grid.setItemAnimator(new SlideInItemAnimator());
        binding.grid.setHasFixedSize(true);
        binding.grid.addItemDecoration(new GridItemDividerDecoration(requireContext(), R.dimen.divider_height, R.color.divider));
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        try {
            FirebaseDataSource.fetchAllFeedbacks(host, host.firestore, host.prefs, new AsyncCallback<List<Feedback>>() {
                @Override
                public void onError(@Nullable String error) {
                    TransitionManager.beginDelayedTransition(binding.fragmentContainer);
                    binding.grid.setVisibility(View.VISIBLE);
                    binding.loading.setVisibility(View.GONE);
                    ExtensionUtils.toast(requireActivity().getApplicationContext(), error, true);
                }

                @Override
                public void onSuccess(@Nullable List<Feedback> response) {
                    if (response != null) {
                        adapter.addData(response);
                    }
                }

                @Override
                public void onStart() {
                    TransitionManager.beginDelayedTransition(binding.fragmentContainer);
                    binding.loading.setVisibility(View.VISIBLE);
                    binding.grid.setVisibility(View.GONE);
                }

                @Override
                public void onComplete() {
                    TransitionManager.beginDelayedTransition(binding.fragmentContainer);
                    binding.loading.setVisibility(View.GONE);
                    binding.grid.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            ExtensionUtils.debugLog(requireActivity(), e.getLocalizedMessage());
        }
    }
}

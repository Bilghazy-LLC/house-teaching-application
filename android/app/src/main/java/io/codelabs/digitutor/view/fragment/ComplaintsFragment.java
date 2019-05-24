package io.codelabs.digitutor.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.TransitionManager;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.data.model.Complaint;
import io.codelabs.digitutor.databinding.FragmentWithListBinding;
import io.codelabs.digitutor.view.ComplaintActivity;
import io.codelabs.digitutor.view.adapter.ComplaintsAdapter;
import io.codelabs.recyclerview.GridItemDividerDecoration;
import io.codelabs.recyclerview.SlideInItemAnimator;
import io.codelabs.sdk.util.ExtensionUtils;

import java.util.List;

public class ComplaintsFragment extends Fragment {
    private FragmentWithListBinding binding;
    private ComplaintsAdapter adapter;

    public ComplaintsFragment() {
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

        adapter = new ComplaintsAdapter(((BaseActivity) requireActivity()), (complaint, isLongClick) -> {
            Intent intent = new Intent(getContext(), ComplaintActivity.class);
            intent.putExtra(ComplaintActivity.EXTRA_COMPLAINT, complaint);
            intent.putExtra(ComplaintActivity.EXTRA_COMPLAINT_STATE, false);
            startActivity(intent);
        });
        binding.grid.setAdapter(adapter);
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        binding.grid.setLayoutManager(lm);
        binding.grid.setItemAnimator(new SlideInItemAnimator());
        binding.grid.setHasFixedSize(true);
        binding.grid.addItemDecoration(new DividerItemDecoration(requireContext(), lm.getOrientation()));
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        FirebaseDataSource.getComplaints(((BaseActivity) requireActivity()), new AsyncCallback<List<Complaint>>() {
            @Override
            public void onError(@Nullable String error) {
                TransitionManager.beginDelayedTransition(binding.fragmentContainer);
                binding.grid.setVisibility(View.VISIBLE);
                binding.loading.setVisibility(View.GONE);
                ExtensionUtils.toast(requireContext(), error, true);
            }

            @Override
            public void onSuccess(@Nullable List<Complaint> response) {
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
    }
}

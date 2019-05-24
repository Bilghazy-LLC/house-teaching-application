package io.codelabs.digitutor.view.fragment;

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
import io.codelabs.digitutor.data.model.Request;
import io.codelabs.digitutor.databinding.FragmentWithListBinding;
import io.codelabs.digitutor.view.RequestDetailsActivity;
import io.codelabs.digitutor.view.adapter.RequestsAdapter;
import io.codelabs.recyclerview.GridItemDividerDecoration;
import io.codelabs.recyclerview.SlideInItemAnimator;
import io.codelabs.sdk.util.ExtensionUtils;

import java.util.List;
import java.util.Objects;

public class RequestsFragment extends Fragment {

    private FragmentWithListBinding binding;
    private RequestsAdapter adapter;

    public RequestsFragment() {
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

        adapter = new RequestsAdapter((BaseActivity) requireActivity(), (request, isLongClick) -> {

            Bundle bundle = new Bundle(0);
            bundle.putString(RequestDetailsActivity.EXTRA_REQUEST_ID, request.getKey());
            bundle.putString(RequestDetailsActivity.EXTRA_REQUEST_PARENT, request.getParent());
            ((BaseActivity) requireActivity()).intentTo(RequestDetailsActivity.class, bundle, false);

        }, ((BaseActivity) requireActivity()).firestore);
        binding.grid.setAdapter(adapter);
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        binding.grid.setLayoutManager(lm);
        binding.grid.setItemAnimator(new SlideInItemAnimator());
        binding.grid.setHasFixedSize(true);
        binding.grid.addItemDecoration(new DividerItemDecoration(requireContext(), lm.getOrientation()));
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        try {
            FirebaseDataSource.getReceivedRequests(requireActivity(),
                    ((BaseActivity) Objects.requireNonNull(requireActivity())).firestore,
                    ((BaseActivity) Objects.requireNonNull(requireActivity())).prefs,
                    new AsyncCallback<List<Request>>() {
                        @Override
                        public void onError(@Nullable String error) {
                            TransitionManager.beginDelayedTransition(binding.fragmentContainer);
                            binding.grid.setVisibility(View.VISIBLE);
                            binding.loading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onSuccess(@Nullable List<Request> response) {
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
            ExtensionUtils.debugLog(requireContext(), e.getLocalizedMessage());
        }
    }
}

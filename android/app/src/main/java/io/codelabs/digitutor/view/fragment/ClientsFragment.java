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
import io.codelabs.digitutor.data.model.Parent;
import io.codelabs.digitutor.databinding.FragmentWithListBinding;
import io.codelabs.digitutor.view.UserActivity;
import io.codelabs.digitutor.view.adapter.UsersAdapter;
import io.codelabs.recyclerview.SlideInItemAnimator;
import io.codelabs.sdk.util.ExtensionUtils;

import java.util.List;
import java.util.Objects;

public class ClientsFragment extends Fragment {
    private FragmentWithListBinding binding;
    private UsersAdapter adapter;

    public ClientsFragment() {
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

        adapter = new UsersAdapter(requireContext(), (parent, isLongClick) -> {
            if (!isLongClick) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(UserActivity.EXTRA_USER, parent);
                bundle.putString(UserActivity.EXTRA_USER_UID, parent.getKey());
                bundle.putString(UserActivity.EXTRA_USER_TYPE, parent.getType());
                ((BaseActivity) requireActivity()).intentTo(UserActivity.class, bundle, false);
            }
        });
        binding.grid.setAdapter(adapter);
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        binding.grid.setLayoutManager(lm);
        binding.grid.addItemDecoration(new DividerItemDecoration(requireContext(), lm.getOrientation()));
        binding.grid.setItemAnimator(new SlideInItemAnimator());
        binding.grid.setHasFixedSize(true);
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        try {
            FirebaseDataSource.getAllClients(requireActivity(),
                    ((BaseActivity) Objects.requireNonNull(requireActivity())).firestore,
                    ((BaseActivity) Objects.requireNonNull(requireActivity())).prefs,
                    new AsyncCallback<List<Parent>>() {
                        @Override
                        public void onError(@Nullable String error) {
                            TransitionManager.beginDelayedTransition(binding.fragmentContainer);
                            binding.grid.setVisibility(View.VISIBLE);
                            binding.loading.setVisibility(View.GONE);
                            ExtensionUtils.toast(requireContext(), error, true);
                        }

                        @Override
                        public void onSuccess(@Nullable List<Parent> response) {
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

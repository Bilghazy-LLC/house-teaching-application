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
import com.google.android.material.snackbar.Snackbar;
import io.codelabs.digitutor.R;
import io.codelabs.digitutor.core.base.BaseActivity;
import io.codelabs.digitutor.core.datasource.remote.FirebaseDataSource;
import io.codelabs.digitutor.core.util.AsyncCallback;
import io.codelabs.digitutor.data.BaseUser;
import io.codelabs.digitutor.data.model.Parent;
import io.codelabs.digitutor.data.model.Timetable;
import io.codelabs.digitutor.data.model.Ward;
import io.codelabs.digitutor.databinding.FragmentTimetableBinding;
import io.codelabs.digitutor.view.adapter.TimetableAdapter;
import io.codelabs.recyclerview.SlideInItemAnimator;
import io.codelabs.sdk.util.ExtensionUtils;

import java.util.List;

public class TimeTableFragment extends Fragment {
    private FragmentTimetableBinding binding;
    private String ward = "";

    private TimetableAdapter adapter;

    public TimeTableFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_timetable, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        adapter = new TimetableAdapter((BaseActivity) requireActivity());
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
            Snackbar snackbar = Snackbar.make(binding.getRoot(), "Error loading your wards", Snackbar.LENGTH_INDEFINITE);
            FirebaseDataSource.getUser(requireActivity(), ((BaseActivity) requireActivity()).firestore, ((BaseActivity) requireActivity()).prefs.getKey(),
                    BaseUser.Type.PARENT, new AsyncCallback<BaseUser>() {
                        @Override
                        public void onError(@Nullable String error) {
                            snackbar.show();
                        }

                        @Override
                        public void onSuccess(@Nullable BaseUser response) {
                            if (response instanceof Parent) {
                                List<String> wards = ((Parent) response).getWards();
                                if (wards.isEmpty()) {
                                    snackbar.show();
                                } else {
                                    ward = wards.get(0);
                                    loadWardInfo(ward);
                                    loadWardTimetable(ward);
                                }
                            }
                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (Exception ex) {
            ExtensionUtils.debugLog(requireContext(), ex.getLocalizedMessage());
        }
    }

    private void loadWardInfo(String ward) {
        try {
            BaseActivity host = (BaseActivity) requireActivity();
            FirebaseDataSource.getWard(host, ward, new AsyncCallback<Ward>() {
                @Override
                public void onError(@Nullable String error) {
                    ExtensionUtils.debugLog(host.getApplicationContext(), error);
                }

                @Override
                public void onSuccess(@Nullable Ward response) {
                    if (response != null) binding.setWard(response);
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onComplete() {

                }
            });
        } catch (Exception ex) {
            ExtensionUtils.debugLog(requireContext(), ex.getLocalizedMessage());
        }
    }

    private void loadWardTimetable(String ward) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), "Error loading your wards", Snackbar.LENGTH_INDEFINITE);

        try {
            FirebaseDataSource.getTimetableForUser((BaseActivity) requireActivity(), ward, new AsyncCallback<List<Timetable>>() {
                @Override
                public void onError(@Nullable String error) {
                    TransitionManager.beginDelayedTransition(binding.fragmentContainer);
                    binding.grid.setVisibility(View.VISIBLE);
                    binding.loading.setVisibility(View.GONE);
                    snackbar.show();
                }

                @Override
                public void onSuccess(@Nullable List<Timetable> response) {
                    if (response != null) {
                        // adapter.addData(response);
                    } else snackbar.show();
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
        } catch (Exception ex) {
            ExtensionUtils.debugLog(requireContext(), ex.getLocalizedMessage());
        }
    }
}

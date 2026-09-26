package com.termux.app.profiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.termux.R;

/**
 * Profile列表底部弹窗
 * 显示所有可用的profiles，支持创建、编辑、删除操作
 */
public class ProfileListBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "ProfileListBottomSheet";

    public interface OnProfileSelectedListener {
        void onProfileSelected(Profile profile);
        void onProfileCreate();
        void onProfileEdit(Profile profile);
        void onProfileDelete(Profile profile);
    }

    private OnProfileSelectedListener listener;
    private RecyclerView recyclerView;
    private ProfileSessionAdapter adapter;
    private FloatingActionButton fabAdd;

    public static ProfileListBottomSheet newInstance() {
        return new ProfileListBottomSheet();
    }

    public void setOnProfileSelectedListener(OnProfileSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_profile_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_profiles);
        fabAdd = view.findViewById(R.id.fab_add_profile);

        setupRecyclerView();
        setupFab();
    }

    private void setupRecyclerView() {
        adapter = new ProfileSessionAdapter(new ProfileSessionAdapter.OnProfileClickListener() {
            @Override
            public void onProfileClick(Profile profile) {
                if (listener != null) {
                    listener.onProfileSelected(profile);
                }
                dismiss();
            }

            @Override
            public void onProfileEditClick(Profile profile) {
                if (listener != null) {
                    listener.onProfileEdit(profile);
                }
            }

            @Override
            public void onProfileDeleteClick(Profile profile) {
                if (listener != null) {
                    listener.onProfileDelete(profile);
                }
                refreshProfiles();
            }

            @Override
            public void onSetDefaultClick(Profile profile) {
                ProfileManager.getInstance(requireContext()).setDefaultProfile(profile.getId());
                refreshProfiles();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        refreshProfiles();
    }

    private void setupFab() {
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onProfileCreate();
                }
            }
        });
    }

    private void refreshProfiles() {
        ProfileManager profileManager = ProfileManager.getInstance(requireContext());
        adapter.setProfiles(profileManager.getProfiles());
        adapter.setDefaultProfileId(profileManager.getDefaultProfile().getId());
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置底部弹窗高度为屏幕的70%
        View view = getView();
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.getLayoutParams().height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);
            }
        }
    }
}

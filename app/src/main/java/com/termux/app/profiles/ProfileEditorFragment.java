package com.termux.app.profiles;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.termux.R;

/**
 * Profile编辑器底部弹窗
 * 支持创建和编辑Profile
 */
public class ProfileEditorFragment extends BottomSheetDialogFragment {

    private static final String TAG = "ProfileEditorFragment";
    private static final String ARG_PROFILE_ID = "profile_id";
    private static final String ARG_IS_EDIT_MODE = "is_edit_mode";

    public interface OnProfileSavedListener {
        void onProfileSaved(Profile profile);
    }

    private OnProfileSavedListener listener;
    private Profile profile;
    private boolean isEditMode;

    // UI组件
    private EditText editName;
    private Spinner spinnerType;
    private EditText editShell;
    private EditText editWorkingDir;

    // Proot相关
    private LinearLayout layoutProot;
    private EditText editProotDistro;
    private EditText editProotArgs;
    private EditText editProotCommand;

    // SSH相关
    private LinearLayout layoutSsh;
    private EditText editSshHost;
    private EditText editSshPort;
    private EditText editSshUser;
    private EditText editSshKeyFile;
    private EditText editSshArgs;

    private com.google.android.material.floatingactionbutton.FloatingActionButton buttonSave;
    private com.google.android.material.button.MaterialButton buttonCancel;

    public static ProfileEditorFragment newInstance() {
        return new ProfileEditorFragment();
    }

    public static ProfileEditorFragment newInstance(String profileId) {
        ProfileEditorFragment fragment = new ProfileEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROFILE_ID, profileId);
        args.putBoolean(ARG_IS_EDIT_MODE, true);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnProfileSavedListener(OnProfileSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String profileId = getArguments().getString(ARG_PROFILE_ID);
            isEditMode = getArguments().getBoolean(ARG_IS_EDIT_MODE, false);

            if (profileId != null) {
                profile = ProfileManager.getInstance(requireContext()).getProfile(profileId);
            }
        }

        if (profile == null) {
            profile = new Profile();
            isEditMode = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupTypeSpinner();
        setupTypeChangeListener();
        setupButtons();

        if (isEditMode && profile != null) {
            populateFields();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置BottomSheet窗口的softInputMode，使布局在键盘弹出时正确调整
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    private void initViews(View view) {
        editName = view.findViewById(R.id.edit_name);
        spinnerType = view.findViewById(R.id.spinner_type);
        editShell = view.findViewById(R.id.edit_shell);
        editWorkingDir = view.findViewById(R.id.edit_working_dir);

        // Proot相关
        layoutProot = view.findViewById(R.id.layout_proot);
        editProotDistro = view.findViewById(R.id.edit_proot_distro);
        editProotArgs = view.findViewById(R.id.edit_proot_args);
        editProotCommand = view.findViewById(R.id.edit_proot_command);

        // SSH相关
        layoutSsh = view.findViewById(R.id.layout_ssh);
        editSshHost = view.findViewById(R.id.edit_ssh_host);
        editSshPort = view.findViewById(R.id.edit_ssh_port);
        editSshUser = view.findViewById(R.id.edit_ssh_user);
        editSshKeyFile = view.findViewById(R.id.edit_ssh_key_file);
        editSshArgs = view.findViewById(R.id.edit_ssh_args);

        buttonSave = view.findViewById(R.id.button_save);
        buttonCancel = view.findViewById(R.id.button_cancel);
    }

    private void setupTypeSpinner() {
        ArrayAdapter<ProfileType> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                ProfileType.values()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // 设置当前类型
        if (profile != null) {
            for (int i = 0; i < ProfileType.values().length; i++) {
                if (ProfileType.values()[i] == profile.getType()) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupTypeChangeListener() {
        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                ProfileType selectedType = (ProfileType) parent.getItemAtPosition(position);
                updateFieldVisibility(selectedType);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // 默认显示Local
                updateFieldVisibility(ProfileType.LOCAL);
            }
        });
    }

    private void updateFieldVisibility(ProfileType type) {
        // 隐藏所有特殊字段
        layoutProot.setVisibility(View.GONE);
        layoutSsh.setVisibility(View.GONE);

        // 根据类型显示相应字段
        switch (type) {
            case LOCAL:
                // 只显示通用字段
                break;
            case PROOT:
                layoutProot.setVisibility(View.VISIBLE);
                break;
            case SSH:
                layoutSsh.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupButtons() {
        buttonSave.setOnClickListener(v -> saveProfile());
        buttonCancel.setOnClickListener(v -> dismiss());
    }

    private void populateFields() {
        if (profile == null) return;

        editName.setText(profile.getName());
        editShell.setText(profile.getShell());
        editWorkingDir.setText(profile.getWorkingDirectory());

        // Proot相关
        editProotDistro.setText(profile.getProotDistro());
        editProotArgs.setText(profile.getProotArgs());
        editProotCommand.setText(profile.getProotCommand());

        // SSH相关
        editSshHost.setText(profile.getSshHost());
        editSshPort.setText(String.valueOf(profile.getSshPort()));
        editSshUser.setText(profile.getSshUser());
        editSshKeyFile.setText(profile.getSshKeyFile());
        editSshArgs.setText(profile.getSshArgs());
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();
        Log.d(TAG, "saveProfile: name='" + name + "'");
        if (name.isEmpty()) {
            editName.setError("Name is required");
            Log.d(TAG, "saveProfile: name is empty");
            return;
        }

        ProfileType type = (ProfileType) spinnerType.getSelectedItem();

        // 创建或更新profile
        if (isEditMode && profile != null) {
            profile.setName(name);
        } else {
            profile = new Profile(name, type);
        }

        profile.setType(type);
        profile.setShell(editShell.getText().toString().trim());
        profile.setWorkingDirectory(editWorkingDir.getText().toString().trim());

        // Proot相关
        if (type == ProfileType.PROOT) {
            profile.setProotDistro(editProotDistro.getText().toString().trim());
            profile.setProotArgs(editProotArgs.getText().toString().trim());
            profile.setProotCommand(editProotCommand.getText().toString().trim());
        }

        // SSH相关
        if (type == ProfileType.SSH) {
            profile.setSshHost(editSshHost.getText().toString().trim());

            String portStr = editSshPort.getText().toString().trim();
            try {
                profile.setSshPort(Integer.parseInt(portStr));
            } catch (NumberFormatException e) {
                profile.setSshPort(22);
            }

            profile.setSshUser(editSshUser.getText().toString().trim());
            profile.setSshKeyFile(editSshKeyFile.getText().toString().trim());
            profile.setSshArgs(editSshArgs.getText().toString().trim());
        }

        // 验证profile
        if (!profile.isValid()) {
            Log.d(TAG, "saveProfile: profile is invalid, type=" + profile.getType() + ", sshHost=" + profile.getSshHost() + ", sshUser=" + profile.getSshUser());
            Toast.makeText(requireContext(), "Invalid profile configuration", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "saveProfile: profile is valid, saving...");

        // 保存profile
        ProfileManager profileManager = ProfileManager.getInstance(requireContext());
        boolean success;
        if (isEditMode) {
            success = profileManager.updateProfile(profile);
        } else {
            success = profileManager.createProfile(profile);
        }

        if (success) {
            Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onProfileSaved(profile);
            }
            dismiss();
        } else {
            Toast.makeText(requireContext(), "Failed to save profile", Toast.LENGTH_SHORT).show();
        }
    }
}

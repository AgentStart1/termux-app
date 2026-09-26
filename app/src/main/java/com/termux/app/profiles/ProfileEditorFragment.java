package com.termux.app.profiles;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.termux.R;
import java.util.*;

public class ProfileEditorFragment extends BottomSheetDialogFragment {
    public interface OnProfileSavedListener { void onProfileSaved(Profile profile); }
    private OnProfileSavedListener listener;
    private Profile profile;
    private EditText name, directory, environment, command;
    private Spinner bases;
    private Button save;
    private boolean editing;

    public static ProfileEditorFragment newInstance() { return new ProfileEditorFragment(); }
    public static ProfileEditorFragment newInstance(String id) {
        ProfileEditorFragment result = newInstance();
        Bundle args = new Bundle(); args.putString("profile_id", id); result.setArguments(args);
        return result;
    }
    public void setOnProfileSavedListener(OnProfileSavedListener value) { listener = value; }
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_profile_editor, parent, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        name = view.findViewById(R.id.edit_name);
        directory = view.findViewById(R.id.edit_working_dir);
        environment = view.findViewById(R.id.edit_profile_environment);
        command = view.findViewById(R.id.edit_proot_command);
        bases = view.findViewById(R.id.spinner_type);
        save = view.findViewById(R.id.button_save);
        save.setEnabled(false);
        editing = getArguments() != null && getArguments().containsKey("profile_id");
        view.findViewById(R.id.button_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.button_refresh_connections).setOnClickListener(v -> refreshBases());
        save.setOnClickListener(v -> saveProfile());
        if (state != null && state.containsKey("draft")) {
            try { profile = Profile.fromJson(new org.json.JSONObject(state.getString("draft"))); }
            catch (Exception ignored) { profile = null; }
        }
        if (profile != null) { populate(); refreshBases(); }
        else if (editing) ProfileManager.getInstance(requireContext()).getProfile(getViewLifecycleOwner(),
            getArguments().getString("profile_id"), loaded -> {
                if (loaded == null) { dismiss(); return; }
                profile = loaded; populate(); refreshBases();
            });
        else { profile = new Profile(); refreshBases(); }
    }
    private void populate() {
        name.setText(profile.getName()); directory.setText(profile.getWorkingDirectory());
        command.setText(profile.getStartupCommand());
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String,String> item : profile.getEnvironmentVariables().entrySet())
            text.append(item.getKey()).append("=").append(item.getValue()).append("\n");
        environment.setText(text);
    }
    private void refreshBases() {
        if (profile == null) return;
        BaseConnection current = (BaseConnection) bases.getSelectedItem();
        if (current != null) { profile.setType(current.type); profile.setBaseId(current.id); }
        save.setEnabled(false);
        ProfileManager.getInstance(requireContext()).connections(getViewLifecycleOwner(), found -> {
            List<BaseConnection> choices = new ArrayList<>(found);
            int selected = -1;
            for (int i=0;i<choices.size();i++) if (choices.get(i).matches(profile)) selected=i;
            if (selected < 0 && !profile.getBaseId().isEmpty()) {
                choices.add(new BaseConnection(profile.getType(), profile.getBaseId()) {
                    @Override public String toString() { return super.toString() + " (unavailable)"; }
                });
                selected=choices.size()-1;
            }
            ArrayAdapter<BaseConnection> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, choices);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            bases.setAdapter(adapter); if (selected >= 0) bases.setSelection(selected);
            save.setEnabled(true);
        });
    }
    private void readFields() {
        profile.setName(name.getText().toString());
        profile.setWorkingDirectory(directory.getText().toString());
        profile.setStartupCommand(command.getText().toString());
        BaseConnection base = (BaseConnection) bases.getSelectedItem();
        if (base != null) { profile.setType(base.type); profile.setBaseId(base.id); }
        Map<String,String> env = new LinkedHashMap<>();
        for (String line : environment.getText().toString().split("\n")) {
            if (line.trim().isEmpty()) continue;
            int equals=line.indexOf('=');
            if (equals <= 0) throw new IllegalArgumentException("Use NAME=value for each environment variable");
            String key=line.substring(0,equals);
            if (env.containsKey(key)) throw new IllegalArgumentException("Duplicate environment variable");
            env.put(key,line.substring(equals+1));
        }
        profile.setEnvironmentVariables(env);
    }
    private void saveProfile() {
        try {
            readFields();
            if (!profile.isValid()) throw new IllegalArgumentException("Check name, base connection and environment variable names");
        } catch (IllegalArgumentException error) {
            Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_LONG).show(); return;
        }
        save.setEnabled(false);
        ProfileManager.getInstance(requireContext()).saveProfile(getViewLifecycleOwner(), profile, editing, success -> {
            save.setEnabled(true);
            if (success) { if (listener != null) listener.onProfileSaved(profile); dismiss(); }
            else Toast.makeText(requireContext(), "Could not save Profile", Toast.LENGTH_LONG).show();
        });
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        if (profile != null && name != null) {
            try { readFields(); state.putString("draft", profile.toJson().toString()); }
            catch (Exception ignored) { /* Invalid draft is not persisted as a usable profile. */ }
        }
    }
}

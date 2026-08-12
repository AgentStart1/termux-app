package com.termux.app.profiles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Profile列表RecyclerView适配器
 */
public class ProfileSessionAdapter extends RecyclerView.Adapter<ProfileSessionAdapter.ProfileViewHolder> {

    private List<Profile> profiles = new ArrayList<>();
    private String defaultProfileId;
    private final OnProfileClickListener listener;

    public interface OnProfileClickListener {
        void onProfileClick(Profile profile);
        void onProfileEditClick(Profile profile);
        void onProfileDeleteClick(Profile profile);
        void onSetDefaultClick(Profile profile);
    }

    public ProfileSessionAdapter(OnProfileClickListener listener) {
        this.listener = listener;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    public void setDefaultProfileId(String defaultProfileId) {
        this.defaultProfileId = defaultProfileId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profiles.get(position);
        holder.bind(profile);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconType;
        private final TextView textName;
        private final TextView textDescription;
        private final ImageButton buttonMenu;
        private final View defaultIndicator;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            iconType = itemView.findViewById(R.id.icon_profile_type);
            textName = itemView.findViewById(R.id.text_profile_name);
            textDescription = itemView.findViewById(R.id.text_profile_description);
            buttonMenu = itemView.findViewById(R.id.button_profile_menu);
            defaultIndicator = itemView.findViewById(R.id.default_indicator);
        }

        void bind(Profile profile) {
            textName.setText(profile.getName());
            textDescription.setText(profile.getDescription());

            // 设置类型图标
            switch (profile.getType()) {
                case LOCAL:
                    iconType.setImageResource(R.drawable.ic_terminal);
                    break;
                case PROOT:
                    iconType.setImageResource(R.drawable.ic_linux);
                    break;
                case SSH:
                    iconType.setImageResource(R.drawable.ic_ssh);
                    break;
            }

            // 设置默认指示器
            boolean isDefault = profile.getId().equals(defaultProfileId);
            defaultIndicator.setVisibility(isDefault ? View.VISIBLE : View.INVISIBLE);

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProfileClick(profile);
                }
            });

            // 菜单按钮
            buttonMenu.setOnClickListener(v -> showPopupMenu(v, profile));
        }

        private void showPopupMenu(View view, Profile profile) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.menu_profile_item, popup.getMenu());

            // 设置菜单项
            popup.getMenu().findItem(R.id.action_set_default)
                    .setEnabled(!profile.getId().equals(defaultProfileId));

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    if (listener != null) {
                        listener.onProfileEditClick(profile);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (listener != null) {
                        listener.onProfileDeleteClick(profile);
                    }
                    return true;
                } else if (itemId == R.id.action_set_default) {
                    if (listener != null) {
                        listener.onSetDefaultClick(profile);
                    }
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
}

/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ProfileAdapter.java
 * Versión: v3.6.0
 * Descripción: Adaptador para la pantalla de perfil que organiza la información 
 *              del usuario y sus publicaciones personales.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.network.models.Profile;
import binaryqva.educative.preusync.utils.common.NumberFormatUtil;
import binaryqva.educative.preusync.utils.common.RoleHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;
import binaryqva.educative.preusync.utils.ui.CustomCircularProgressDrawable;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_PROFILE_HEADER = 0;
    public static final int TYPE_PROFILE_ACTIONS = 1;
    public static final int TYPE_PROFILE_STATS = 2;
    public static final int TYPE_SECTION_TITLE = 3;
    public static final int TYPE_POST_ITEM = 4;
    public static final int TYPE_LOAD_MORE = 5;
    public static final int TYPE_LOADING = 6;
    public static final int TYPE_EMPTY = 7;
    public static final int TYPE_ERROR = 8;

    public interface OnPostClickListener { void onPostClick(Post post); }
    public interface OnLoadMoreListener { void onLoadMore(); }
    public interface OnRetryListener { void onRetry(); }
    public interface OnEditProfileListener { void onEditProfile(); }
    public interface OnSettingsListener { void onSettings(); }
    public interface OnLogoutListener { void onLogout(); }

    private final Context context;
    private Profile profileData;
    private List<Post> postsData = new ArrayList<>();
    
    private boolean isLoading = false;
    private boolean hasMore = true;
    private boolean isError = false;
    private boolean isProfileLoaded = false;

    private OnPostClickListener postClickListener;
    private OnLoadMoreListener loadMoreListener;
    private OnRetryListener retryListener;
    private OnEditProfileListener editProfileListener;
    private OnSettingsListener settingsListener;
    private OnLogoutListener logoutListener;

    public ProfileAdapter(Context context) {
        this.context = context;
        setHasStableIds(true);
    }

    public void setProfileData(Profile data) {
        this.profileData = data;
        this.isProfileLoaded = true;
        notifyDataSetChanged();
    }

    public void setPostsData(List<Post> newData, boolean hasMore) {
        this.postsData = newData != null ? newData : new ArrayList<>();
        this.hasMore = hasMore;
        notifyDataSetChanged();
    }

    public void setLoadingState(boolean loading) { this.isLoading = loading; notifyDataSetChanged(); }
    public void setErrorState(boolean error) { this.isError = error; notifyDataSetChanged(); }
    public int getPostsCount() { return postsData.size(); }

    public void setOnPostClickListener(OnPostClickListener l) { this.postClickListener = l; }
    public void setOnLoadMoreListener(OnLoadMoreListener l) { this.loadMoreListener = l; }
    public void setOnRetryListener(OnRetryListener l) { this.retryListener = l; }
    public void setOnEditProfileListener(OnEditProfileListener l) { this.editProfileListener = l; }
    public void setOnSettingsListener(OnSettingsListener l) { this.settingsListener = l; }
    public void setOnLogoutListener(OnLogoutListener l) { this.logoutListener = l; }

    private int getHeaderCount() { return isProfileLoaded ? 4 : 0; }
    private int getFooterCount() {
        if (!isProfileLoaded) return 0;
        if (postsData.isEmpty()) return 1;
        return hasMore ? 1 : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (!isProfileLoaded) return TYPE_LOADING;
        if (position == 0) return TYPE_PROFILE_HEADER;
        if (position == 1) return TYPE_PROFILE_ACTIONS;
        if (position == 2) return TYPE_PROFILE_STATS;
        if (position == 3) return TYPE_SECTION_TITLE;
        int postsEnd = 4 + postsData.size();
        if (position >= 4 && position < postsEnd) return TYPE_POST_ITEM;
        if (isError && postsData.isEmpty()) return TYPE_ERROR;
        if (postsData.isEmpty()) return TYPE_EMPTY;
        return TYPE_LOAD_MORE;
    }

    @Override
    public long getItemId(int position) {
        int type = getItemViewType(position);
        if (type == TYPE_POST_ITEM) {
            String id = postsData.get(position - getHeaderCount()).getId();
            return id != null ? id.hashCode() : position;
        }
        return 1000000L + type * 1000 + position;
    }

    @Override
    public int getItemCount() { return getHeaderCount() + postsData.size() + getFooterCount(); }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(context);
        switch (viewType) {
            case TYPE_PROFILE_HEADER: return new ProfileHeaderViewHolder(inf.inflate(R.layout.item_profile_header, parent, false));
            case TYPE_PROFILE_ACTIONS: return new ProfileActionsViewHolder(inf.inflate(R.layout.item_profile_actions, parent, false));
            case TYPE_PROFILE_STATS: return new ProfileStatsViewHolder(inf.inflate(R.layout.item_profile_stats, parent, false));
            case TYPE_SECTION_TITLE: return new SectionTitleViewHolder(inf.inflate(R.layout.item_profile_section_title, parent, false));
            case TYPE_POST_ITEM: return new PostViewHolder(inf.inflate(R.layout.item_user_post, parent, false));
            case TYPE_LOAD_MORE: return new LoadMoreViewHolder(inf.inflate(R.layout.item_load_more, parent, false));
            case TYPE_LOADING: return new LoadingViewHolder(inf.inflate(R.layout.item_loading, parent, false));
            case TYPE_EMPTY: return new EmptyViewHolder(inf.inflate(R.layout.item_empty_posts, parent, false));
            case TYPE_ERROR: return new ErrorViewHolder(inf.inflate(R.layout.item_error_posts, parent, false));
            default: return new EmptyViewHolder(inf.inflate(R.layout.item_empty_posts, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_PROFILE_HEADER: ((ProfileHeaderViewHolder) holder).bind(profileData); break;
            case TYPE_PROFILE_ACTIONS: ((ProfileActionsViewHolder) holder).bind(editProfileListener, settingsListener, logoutListener); break;
            case TYPE_PROFILE_STATS: ((ProfileStatsViewHolder) holder).bind(profileData); break;
            case TYPE_POST_ITEM: ((PostViewHolder) holder).bind(postsData.get(position - getHeaderCount())); break;
            case TYPE_LOAD_MORE: ((LoadMoreViewHolder) holder).bind(isLoading); break;
        }
    }

    static class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView fName, lName, uName, rName, rInfo, iText;
        private final ImageView rAvatar, sImg;
        private final Context ctx;

        ProfileHeaderViewHolder(@NonNull View v) {
            super(v); ctx = v.getContext();
            fName = v.findViewById(R.id.firstNameText); lName = v.findViewById(R.id.lastNameText);
            uName = v.findViewById(R.id.usernameText); rName = v.findViewById(R.id.roleText);
            rInfo = v.findViewById(R.id.roleInfoText); iText = v.findViewById(R.id.initialAvatarText);
            rAvatar = v.findViewById(R.id.roleAvatarImage); sImg = v.findViewById(R.id.statusImage);
        }

        void bind(Profile p) {
            if (p == null) return;
            fName.setText(p.getFirstName()); lName.setText(p.getLastName());
            uName.setText(p.getUsername()); rName.setText(RoleHelper.getLocalizedFromRoleValue(p.getRole(), ctx));
            
            StringBuilder info = new StringBuilder();
            if (p.getSchool() != null) info.append(ctx.getString(R.string.profile_school_label, p.getSchool()));
            if (p.getGroup() != null) info.append(info.length() > 0 ? "\n" : "").append(ctx.getString(R.string.profile_group_label, p.getGroup()));
            rInfo.setText(info.toString()); rInfo.setVisibility(info.length() > 0 ? View.VISIBLE : View.GONE);

            String s = p.getStatus();
            sImg.setImageResource("verified".equals(s) ? R.drawable.ic_verified : R.drawable.ic_warning);
            sImg.setColorFilter(ThemeManager.getThemeColor(ctx, "verified".equals(s) ? R.attr.colorAccent : R.attr.colorWarning));

            renderAvatar(p);
        }

        private void renderAvatar(Profile p) {
            String url = p.getAvatarUrl();
            if (url != null && url.startsWith("{")) {
                try {
                    JSONObject j = new JSONObject(url);
                    if ("inicial".equals(j.getString("tipo"))) {
                        iText.setText(j.getString("inicial")); iText.setVisibility(View.VISIBLE); rAvatar.setVisibility(View.GONE);
                    } else {
                        rAvatar.setImageResource(RoleHelper.getRoleIconResId(j.getString("rol")));
                        rAvatar.setVisibility(View.VISIBLE); iText.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {}
            } else if (url != null && !url.isEmpty()) {
                iText.setVisibility(View.GONE); rAvatar.setVisibility(View.VISIBLE);
                Glide.with(ctx).load(url).circleCrop().into(rAvatar);
            } else {
                rAvatar.setImageResource(R.drawable.ic_nav_profile); rAvatar.setVisibility(View.VISIBLE); iText.setVisibility(View.GONE);
            }
        }
    }

    static class ProfileActionsViewHolder extends RecyclerView.ViewHolder {
        private final View btnE, btnS, btnL;
        ProfileActionsViewHolder(@NonNull View v) {
            super(v); btnE = v.findViewById(R.id.editButton); btnS = v.findViewById(R.id.settingsButton); btnL = v.findViewById(R.id.logoutButton);
        }
        void bind(OnEditProfileListener e, OnSettingsListener s, OnLogoutListener l) {
            btnE.setOnClickListener(v -> { if (e != null) e.onEditProfile(); });
            btnS.setOnClickListener(v -> { if (s != null) s.onSettings(); });
            btnL.setOnClickListener(v -> { if (l != null) l.onLogout(); });
        }
    }

    static class ProfileStatsViewHolder extends RecyclerView.ViewHolder {
        private final TextView pC, lC, dC;
        ProfileStatsViewHolder(@NonNull View v) {
            super(v); pC = v.findViewById(R.id.statsPostsCount); lC = v.findViewById(R.id.statsLikesCount); dC = v.findViewById(R.id.statsDislikesCount);
        }
        void bind(Profile p) {
            // Stats logic here
        }
    }

    static class SectionTitleViewHolder extends RecyclerView.ViewHolder { SectionTitleViewHolder(@NonNull View v) { super(v); } }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView img;
        private final TextView tit, dat, lik, dis;
        PostViewHolder(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.userPostImage); tit = v.findViewById(R.id.userPostTitle);
            dat = v.findViewById(R.id.userPostDate); lik = v.findViewById(R.id.userPostLikes);
            dis = v.findViewById(R.id.userPostDislikes);
        }
        void bind(Post post) {
            tit.setText(post.getDetails());
            dat.setText(post.getCreatedAt());
            lik.setText(NumberFormatUtil.formatNumber(post.getScore()));
            String url = post.getImageUrl();
            if (url != null && !url.isEmpty()) {
                img.setVisibility(View.VISIBLE);
                Glide.with(context).load(url).centerCrop().into(img);
            } else img.setVisibility(View.GONE);
            itemView.setOnClickListener(v -> { if (postClickListener != null) postClickListener.onPostClick(post); });
        }
    }

    class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        private final MaterialButton btn; private final View prg;
        LoadMoreViewHolder(@NonNull View v) { super(v); btn = v.findViewById(R.id.loadMoreButton); prg = v.findViewById(R.id.loadMoreProgress); }
        void bind(boolean loading) {
            btn.setVisibility(loading ? View.GONE : View.VISIBLE); prg.setVisibility(loading ? View.VISIBLE : View.GONE);
            btn.setOnClickListener(v -> { if (loadMoreListener != null) loadMoreListener.onLoadMore(); });
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder { LoadingViewHolder(@NonNull View v) { super(v); } }
    static class EmptyViewHolder extends RecyclerView.ViewHolder { EmptyViewHolder(@NonNull View v) { super(v); } }
    class ErrorViewHolder extends RecyclerView.ViewHolder {
        ErrorViewHolder(@NonNull View v) { super(v); v.findViewById(R.id.retryButton).setOnClickListener(view -> { if (retryListener != null) retryListener.onRetry(); }); }
    }
}



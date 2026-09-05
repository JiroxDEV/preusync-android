/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: PostsCarouselAdapter.java
 * Versión: v3.0.0
 * Descripción: Adaptador para el carrusel de publicaciones en el inicio. 
 *              Refactorizado para HomeViewModel.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.adapters.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.ui.activities.HomeActivity;
import binaryqva.educative.preusync.ui.viewmodels.HomeViewModel;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.AvatarHelper;
import binaryqva.educative.preusync.utils.common.NumberFormatUtil;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.ui.CustomCircularProgressDrawable;
import binaryqva.educative.preusync.utils.theme.ThemeManager;
import binaryqva.educative.preusync.utils.ui.ImageTextColorHelper;

public class PostsCarouselAdapter extends RecyclerView.Adapter<PostsCarouselAdapter.ViewHolder> {

    private final Context context;
    private final List<Post> data;
    private final HomeViewModel viewModel;
    private final String currentUsername;

    public PostsCarouselAdapter(Context context, List<Post> data, HomeViewModel viewModel) {
        this.context = context;
        this.data = data != null ? data : new ArrayList<>();
        this.viewModel = viewModel;
        this.currentUsername = PreferenceManager.getInstance(context).getUsername();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_mini, parent, false);
        int w = (int) (parent.getMeasuredWidth() * 0.85);
        if (w <= 0) w = ViewGroup.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(new RecyclerView.LayoutParams(w, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() { return data.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, authorTextView, likeCount;
        ImageView featuredImage, authorImageView, likeIcon, dislikeIcon, detailsImageView;
        View likeContainer, dislikeContainer, scrim, infoLayout;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.heading); authorTextView = v.findViewById(R.id.autor);
            likeCount = v.findViewById(R.id.likeCount); featuredImage = v.findViewById(R.id.featuredImage);
            authorImageView = v.findViewById(R.id.miniPostAuthorAvatar); likeIcon = v.findViewById(R.id.likeIcon);
            dislikeIcon = v.findViewById(R.id.dislikeIcon); likeContainer = v.findViewById(R.id.likeContainer);
            dislikeContainer = v.findViewById(R.id.dislikeContainer); scrim = v.findViewById(R.id.scrim);
            infoLayout = v.findViewById(R.id.infoLayout); detailsImageView = v.findViewById(R.id.detailsImageView);
        }

        void bind(Post post) {
            int colorAccent = ThemeManager.getThemeColor(context, R.attr.colorAccent);
            title.setText(post.getTitle());

            String url = post.getImageUrl();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infoLayout.getLayoutParams();

            if (url != null && !url.isEmpty() && !"null".equals(url)) {
                featuredImage.setVisibility(View.VISIBLE); if (scrim != null) scrim.setVisibility(View.VISIBLE);
                params.removeRule(RelativeLayout.CENTER_IN_PARENT); params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                CustomCircularProgressDrawable cp = new CustomCircularProgressDrawable(context); cp.start();
                Glide.with(context).asBitmap().load(Uri.parse(url)).placeholder(cp).error(R.drawable.ic_image_broken)
                        .listener(new RequestListener<Bitmap>() {
                            @Override public boolean onResourceReady(Bitmap r, Object m, Target<Bitmap> t, DataSource d, boolean f) {
                                cp.stop(); featuredImage.post(() -> ImageTextColorHelper.adjustTextColor(featuredImage, title)); return false;
                            }
                            @Override public boolean onLoadFailed(@Nullable GlideException e, Object m, Target<Bitmap> t, boolean f) { cp.stop(); return false; }
                        }).into(featuredImage);
            } else {
                featuredImage.setVisibility(View.GONE); if (scrim != null) scrim.setVisibility(View.GONE);
                params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM); params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            }
            infoLayout.setLayoutParams(params);

            String id = post.getId();
            int vote = 0;
            Map<String, Integer> vm = viewModel.getVoteStates().getValue();
            if (vm != null && vm.containsKey(id)) {
                Integer v = vm.get(id); if (v != null) vote = v;
            }

            if (likeIcon != null) likeIcon.setImageResource(vote == 1 ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
            if (dislikeIcon != null) dislikeIcon.setImageResource(vote == 2 ? R.drawable.ic_dislike_filled : R.drawable.ic_dislike_outline);

            likeCount.setText(NumberFormatUtil.formatNumber(post.getScore()));

            Set<String> proc = viewModel.getProcessingPosts().getValue();
            final boolean isProc = proc != null && proc.contains(id);
            if (likeContainer != null) { likeContainer.setEnabled(!isProc); likeContainer.setAlpha(isProc ? 0.5f : 1.0f); }
            if (dislikeContainer != null) { dislikeContainer.setEnabled(!isProc); dislikeContainer.setAlpha(isProc ? 0.5f : 1.0f); }

            HomeActivity act = (HomeActivity) context;
            String author = post.getAuthor();
            final int finalVote = vote;
            if (likeContainer != null) likeContainer.setOnClickListener(v -> handleVote(id, author, isProc, finalVote == 1 ? "none" : "like", act));
            if (dislikeContainer != null) dislikeContainer.setOnClickListener(v -> handleVote(id, author, isProc, finalVote == 2 ? "none" : "dislike", act));

            View.OnClickListener details = v -> act.showPostDetailsBottomSheet(post);
            if (detailsImageView != null) detailsImageView.setOnClickListener(details);
            View b = itemView.findViewById(R.id.body); if (b != null) b.setOnClickListener(details);

            authorTextView.setText(author);
            if (author != null && !author.isEmpty()) {
                authorImageView.setImageBitmap(AvatarHelper.generateInitialAvatar(context, "?", colorAccent));
                act.fetchAuthorData(author, data -> { if (!act.isFinishing()) act.updateAuthorAvatar(authorImageView, data, author); });
            }
        }

        private void handleVote(String id, String author, boolean isProc, String type, HomeActivity act) {
            if (isProc) return;
            if (currentUsername != null && currentUsername.equals(author)) { AppUtils.showMessage(context, context.getString(R.string.error_self_vote)); return; }
            act.setVote(id, type);
        }
    }
}



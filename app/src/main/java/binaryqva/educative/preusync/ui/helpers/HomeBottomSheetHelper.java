/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: HomeBottomSheetHelper.java
 * Versión: v3.0.0
 * Descripción: Ayudante para la gestión de BottomSheets. Controla la visualización 
 *              detallada de noticias, publicaciones, eventos y efemérides.
 *              Refactorizada para usar el modelo unificado Event.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.Ephemeris;
import binaryqva.educative.preusync.network.models.News;
import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.network.models.Event;
import binaryqva.educative.preusync.ui.activities.HomeActivity;
import binaryqva.educative.preusync.ui.viewmodels.HomeViewModel;
import binaryqva.educative.preusync.data.repositories.VoteRepository;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.AvatarHelper;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.common.FilePickerHelper;
import binaryqva.educative.preusync.utils.common.FileUtils;
import binaryqva.educative.preusync.utils.common.NumberFormatUtil;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.markdown.MarkdownWebViewHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;
import binaryqva.educative.preusync.utils.ui.ButtonAnimator;
import binaryqva.educative.preusync.utils.ui.CustomCircularProgressDrawable;

/**
 * Encapsula la lógica de presentación de paneles deslizantes (BottomSheets).
 */
public class HomeBottomSheetHelper {

    private final HomeActivity activity;
    private final Context context;
    private final HomeViewModel viewModel;
    private final PreferenceManager preferenceManager;
    private final FilePickerHelper filePickerHelper;

    private BottomSheetDialog postDetailsBottomSheet;
    private BottomSheetDialog createPostBottomSheet;
    private BottomSheetDialog postPreviewBottomSheet;

    private TextView bottomSheetTitle, bottomSheetAuthor, bottomSheetLikeCount;
    private ImageView bottomSheetFeaturedImage, bottomSheetAuthorAvatar, bottomSheetLikeIcon, bottomSheetDislikeIcon;
    private View bottomSheetLikeContainer, bottomSheetDislikeContainer, bottomSheetScrim;
    private String bottomSheetCurrentPostId = "";
    private View bottomSheetMoreActionsCardView;
    private TextView bottomSheetPostDetailsText;

    private Observer<Map<String, Integer>> voteStateObserver;
    private Observer<Map<String, VoteRepository.PostCounts>> postCountsObserver;

    private EditText titleEditText, contentEditText;
    private ImageView photoImageView;
    private TextInputLayout titleTextInputLayout, contentTextInputLayout;
    private String pathBase64 = "";

    public HomeBottomSheetHelper(HomeActivity activity, HomeViewModel viewModel) {
        this.activity = activity;
        this.context = activity;
        this.viewModel = viewModel;
        this.preferenceManager = PreferenceManager.getInstance(context);
        this.filePickerHelper = new FilePickerHelper(activity);
    }

    public void showCreatePostBottomSheet() {
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_new_post, null);
        ButtonAnimator.applyTo(view);
        createPostBottomSheet = createBaseSheet(view);
        bindCreatePostViews(view);
        setupCreatePostLogic(view);
        createPostBottomSheet.show();
    }

    private void bindCreatePostViews(View v) {
        titleEditText = v.findViewById(R.id.titleEditText);
        contentEditText = v.findViewById(R.id.contentEditText);
        photoImageView = v.findViewById(R.id.photoImageView);
        titleTextInputLayout = v.findViewById(R.id.titleTextInputLayout);
        contentTextInputLayout = v.findViewById(R.id.contentTextInputLayout);
    }

    private void setupCreatePostLogic(View v) {
        contentTextInputLayout.setEndIconOnClickListener(view -> showPostPreviewBottomSheet(contentEditText.getText().toString().trim()));
        photoImageView.setOnClickListener(view -> filePickerHelper.pickFile("image/jpeg", new FilePickerHelper.OnFilePickedListener() {
            @Override public void onFilePicked(String path, String name, String mime, String base64) {
                pathBase64 = base64; photoImageView.setImageBitmap(FileUtils.decodeSampleBitmapFromPath(path, 256, 256));
            }
            @Override public void onPickCancelled() { AppUtils.showMessage(activity, activity.getString(R.string.toast_no_file_selected)); }
        }));
        v.findViewById(R.id.buttonCancel).setOnClickListener(view -> { createPostBottomSheet.dismiss(); clearPostFields(); });
        v.findViewById(R.id.buttonCreate).setOnClickListener(view -> validateAndCreatePost());
    }

    private void validateAndCreatePost() {
        String t = titleEditText.getText().toString().trim(), c = contentEditText.getText().toString().trim();
        boolean ok = true;
        if (t.isEmpty()) { titleTextInputLayout.setError(activity.getString(R.string.error_title_required)); ok = false; }
        if (c.isEmpty()) { contentTextInputLayout.setError(activity.getString(R.string.error_content_required)); ok = false; }
        if (ok) { activity.onCreatePost(t, c, pathBase64); createPostBottomSheet.dismiss(); clearPostFields(); }
    }

    private void clearPostFields() {
        titleEditText.setText(""); contentEditText.setText(""); pathBase64 = "";
        photoImageView.setImageResource(R.drawable.ic_add_photo);
    }

    public void showPostPreviewBottomSheet(String content) {
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_post_preview, null);
        postPreviewBottomSheet = createBaseSheet(view);
        view.findViewById(R.id.closeImage).setOnClickListener(v -> postPreviewBottomSheet.dismiss());
        String raw = content.isEmpty() ? "### " + activity.getString(R.string.message_preview_empty) : content;
        MarkdownWebViewHelper.replaceWithWebView(activity, view.findViewById(R.id.contentTextView), raw, "preview", R.id.previewLoading);
        postPreviewBottomSheet.show();
    }

    public void showPostDetailsBottomSheet(Post post) {
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_post_details, null);
        postDetailsBottomSheet = createBaseSheet(view);
        bindDetailsViews(view);
        String id = post.getId();
        bottomSheetCurrentPostId = id;
        setupVoteObservers(id);
        view.findViewById(R.id.closeImageView).setOnClickListener(v -> postDetailsBottomSheet.dismiss());
        renderPostDetails(post);
        setupPostActions(post);
        postDetailsBottomSheet.show();
    }

    private void bindDetailsViews(View v) {
        bottomSheetTitle = v.findViewById(R.id.heading); bottomSheetFeaturedImage = v.findViewById(R.id.featuredImage);
        bottomSheetLikeCount = v.findViewById(R.id.likeCount); bottomSheetLikeIcon = v.findViewById(R.id.likeIcon);
        bottomSheetDislikeIcon = v.findViewById(R.id.dislikeIcon); bottomSheetLikeContainer = v.findViewById(R.id.likeContainer);
        bottomSheetDislikeContainer = v.findViewById(R.id.dislikeContainer); bottomSheetScrim = v.findViewById(R.id.scrim);
        bottomSheetPostDetailsText = v.findViewById(R.id.postDetailsText);
        bottomSheetAuthor = v.findViewById(R.id.autor); bottomSheetAuthorAvatar = v.findViewById(R.id.postAuthorAvatar);
        bottomSheetMoreActionsCardView = v.findViewById(R.id.moreActionsCardView);
    }

    private void setupVoteObservers(String id) {
        voteStateObserver = voteMap -> {
            if (bottomSheetLikeIcon == null || voteMap == null || !voteMap.containsKey(id)) return;
            int s = voteMap.get(id);
            bottomSheetLikeIcon.setImageResource(s == 1 ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
            bottomSheetDislikeIcon.setImageResource(s == 2 ? R.drawable.ic_dislike_filled : R.drawable.ic_dislike_outline);
        };
        viewModel.getVoteStates().observeForever(voteStateObserver);

        postCountsObserver = countsMap -> {
            if (bottomSheetLikeCount == null || countsMap == null || !countsMap.containsKey(id)) return;
            VoteRepository.PostCounts counts = countsMap.get(id);
            if (counts != null) {
                bottomSheetLikeCount.setText(NumberFormatUtil.formatNumber(counts.likes));
            }
        };
        viewModel.getPostCounts().observeForever(postCountsObserver);
    }

    private void renderPostDetails(Post post) {
        bottomSheetTitle.setText(post.getTitle());
        String url = post.getImageUrl();
        if (url != null && !url.isEmpty()) {
            bottomSheetFeaturedImage.setVisibility(View.VISIBLE);
            Glide.with(activity).load(Uri.parse(url)).placeholder(new CustomCircularProgressDrawable(activity)).into(bottomSheetFeaturedImage);
        } else bottomSheetFeaturedImage.setVisibility(View.GONE);

        MarkdownWebViewHelper.replaceWithWebView(activity, bottomSheetPostDetailsText, post.getDetails(), post.getId(), R.id.detailsLoading);
        bottomSheetAuthor.setText(post.getAuthor());
    }

    public void showNewsDetailsBottomSheet(News news) {
        View v = LayoutInflater.from(context).inflate(R.layout.bottomsheet_news_details, null);
        BottomSheetDialog sheet = createBaseSheet(v);
        ((TextView) v.findViewById(R.id.heading)).setText(news.getHeadline());
        ((TextView) v.findViewById(R.id.source)).setText(news.getSource());
        String url = news.getImageUrl();
        if (url != null && !url.isEmpty()) Glide.with(activity).load(Uri.parse(url)).into((ImageView) v.findViewById(R.id.featuredImage));
        MarkdownWebViewHelper.replaceWithWebView(activity, v.findViewById(R.id.detailsText), news.getDetails(), news.getId(), R.id.newsDetailsLoading);
        v.findViewById(R.id.closeImageView).setOnClickListener(view -> sheet.dismiss());
        sheet.show();
    }

    public void showEventDetailsBottomSheet(Event event) {
        View v = LayoutInflater.from(context).inflate(R.layout.bottomsheet_event_details, null);
        BottomSheetDialog sheet = createBaseSheet(v);
        ((TextView) v.findViewById(R.id.heading)).setText(event.getTitle());
        ((TextView) v.findViewById(R.id.date)).setText(event.getDate());
        MarkdownWebViewHelper.replaceWithWebView(activity, v.findViewById(R.id.detailsText), event.getDetails(), event.getId(), R.id.eventDetailsLoading);
        v.findViewById(R.id.closeImageView).setOnClickListener(view -> sheet.dismiss());
        sheet.show();
    }

    public void showEphemerisDetailsBottomSheet(Ephemeris eph) {
        View v = LayoutInflater.from(context).inflate(R.layout.bottomsheet_ephemeris, null);
        BottomSheetDialog sheet = createBaseSheet(v);
        ((TextView) v.findViewById(R.id.heading)).setText(eph.getTitle());
        ((TextView) v.findViewById(R.id.date)).setText(eph.getDate());
        MarkdownWebViewHelper.replaceWithWebView(activity, v.findViewById(R.id.detailsText), eph.getDetails(), eph.getId(), R.id.ephemerisDetailsLoading);
        v.findViewById(R.id.closeImageView).setOnClickListener(view -> sheet.dismiss());
        sheet.show();
    }

    private BottomSheetDialog createBaseSheet(View v) {
        BottomSheetDialog s = new BottomSheetDialog(activity); s.setContentView(v);
        s.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        s.setCancelable(false);
        return s;
    }

    private void setupPostActions(Post post) {
        String id = post.getId();
        String auth = post.getAuthor();
        boolean isOwner = preferenceManager.getUsername() != null && preferenceManager.getUsername().equals(auth);

        bottomSheetMoreActionsCardView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(new ContextThemeWrapper(activity, R.style.PopupMenuStyle), v);
            popup.getMenu().add(R.string.menu_share);
            popup.getMenu().add(isOwner ? R.string.menu_delete : R.string.menu_report);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals(activity.getString(R.string.menu_share))) AppUtils.showMessage(activity, activity.getString(R.string.menu_share));
                else if (item.getTitle().equals(activity.getString(R.string.menu_delete))) activity.onDeletePost(id);
                else activity.onReportPost(id);
                return true;
            });
            popup.show();
        });
        bottomSheetLikeContainer.setOnClickListener(v -> handleVoteAction(id, auth, 1));
        bottomSheetDislikeContainer.setOnClickListener(v -> handleVoteAction(id, auth, 2));
    }

    private void handleVoteAction(String id, String auth, int type) {
        if (preferenceManager.getUsername() != null && preferenceManager.getUsername().equals(auth)) return;
        activity.onVoteAction(id, type == 1 ? "like" : "dislike");
    }

    public void dismissAll() {
        if (postDetailsBottomSheet != null) postDetailsBottomSheet.dismiss();
        if (createPostBottomSheet != null) createPostBottomSheet.dismiss();
        if (postPreviewBottomSheet != null) postPreviewBottomSheet.dismiss();
    }
}



/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: UserPostsAdapter.java
 * Versión: v2.4.1
 * Descripción: Adaptador especializado para listar publicaciones del autor en 
 *              la sección de perfil.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.common.DataConverter;
import binaryqva.educative.preusync.utils.common.NumberFormatUtil;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.ui.CustomCircularProgressDrawable;

/**
 * Renderiza de forma compacta las publicaciones del usuario autenticado.
 * Implementa una gestión de imágenes eficiente con Glide y placeholders dinámicos.
 */
public class UserPostsAdapter extends RecyclerView.Adapter<UserPostsAdapter.ViewHolder> {

    private static final String TAG = "UserPostsAdapter";

    private final Context context;
    private final ArrayList<HashMap<String, Object>> data;
    private final OnPostClickListener onPostClickListener;
    private final SwipeRefreshLayout swipeRefreshLayout;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnPostClickListener { void onPostClick(HashMap<String, Object> post); }

    public UserPostsAdapter(Context context, ArrayList<HashMap<String, Object>> data,
                            OnPostClickListener listener, SwipeRefreshLayout swipeRefreshLayout) {
        this.context = context;
        this.data = data != null ? data : new ArrayList<>();
        this.onPostClickListener = listener;
        this.swipeRefreshLayout = swipeRefreshLayout;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= data.size()) return RecyclerView.NO_ID;
        Object id = data.get(position).get("id");
        if (id != null) return id instanceof Number ? ((Number) id).longValue() : id.toString().hashCode();
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_post, parent, false);
        return new ViewHolder(view, swipeRefreshLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() { return data.size(); }

    // ==================== MÉTODOS DE ACTUALIZACIÓN ====================

    public void setData(ArrayList<HashMap<String, Object>> newData) {
        data.clear(); if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<HashMap<String, Object>> newItems) {
        if (newItems == null || newItems.isEmpty()) return;
        int start = data.size(); data.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    public void clearItems() { data.clear(); notifyDataSetChanged(); }

    // ==================== VIEW HOLDER ====================

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView titleText, dateText, likesText, dislikesText;
        private final SwipeRefreshLayout swipeRefreshLayout;

        ViewHolder(@NonNull View itemView, SwipeRefreshLayout swipeRefreshLayout) {
            super(itemView);
            this.swipeRefreshLayout = swipeRefreshLayout;
            imageView = itemView.findViewById(R.id.userPostImage);
            titleText = itemView.findViewById(R.id.userPostTitle);
            dateText = itemView.findViewById(R.id.userPostDate);
            likesText = itemView.findViewById(R.id.userPostLikes);
            dislikesText = itemView.findViewById(R.id.userPostDislikes);

            // Evitar que el SwipeRefresh se active accidentalmente durante el scroll lateral o pulsación.
            itemView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && swipeRefreshLayout != null) swipeRefreshLayout.setEnabled(false);
                else if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && swipeRefreshLayout != null) {
                    swipeRefreshLayout.postDelayed(() -> { if (swipeRefreshLayout != null) swipeRefreshLayout.setEnabled(true); }, 300);
                }
                return false;
            });

            itemView.setOnClickListener(v -> {
                if (onPostClickListener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) onPostClickListener.onPostClick(data.get(pos));
                }
            });
        }

        void bind(HashMap<String, Object> post) {
            titleText.setText(DataConverter.toString(post.get("title")));

            // FORMATEO DE FECHA TÉCNICA A HUMANA:
            String createdAt = DataConverter.toString(post.get("created_at"));
            if (!createdAt.isEmpty()) {
                try {
                    java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    Date parsedDate = iso.parse(createdAt);
                    if (parsedDate != null) dateText.setText(dateFormat.format(parsedDate));
                    else dateText.setText(createdAt);
                } catch (Exception e) { dateText.setText(createdAt); }
            }

            likesText.setText(NumberFormatUtil.formatNumber(DataConverter.parseLong(post.get("likes"), 0L)));
            dislikesText.setText(NumberFormatUtil.formatNumber(DataConverter.parseLong(post.get("dislikes"), 0L)));

            // CARGA DINÁMICA DE IMAGEN ADJUNTA:
            String url = DataConverter.toString(post.get("imageUrl"));
            if (!url.isEmpty() && !"null".equals(url)) {
                imageView.setVisibility(View.VISIBLE);
                CustomCircularProgressDrawable cp = new CustomCircularProgressDrawable(context); cp.start();
                int size = PreferenceManager.getInstance(context).getImageLoadSize();

                Glide.with(context).asBitmap().load(Uri.parse(url)).override(size, size).placeholder(cp).error(R.drawable.ic_image_broken)
                        .listener(new RequestListener<Bitmap>() {
                            @Override public boolean onResourceReady(Bitmap r, Object m, Target<Bitmap> t, DataSource d, boolean f) { cp.stop(); return false; }
                            @Override public boolean onLoadFailed(@Nullable GlideException e, Object m, Target<Bitmap> t, boolean f) { cp.stop(); return false; }
                        }).into(imageView);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
    }
}



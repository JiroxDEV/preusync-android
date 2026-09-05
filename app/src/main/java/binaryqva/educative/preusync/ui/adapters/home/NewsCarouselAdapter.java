/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NewsCarouselAdapter.java
 * Versión: v2.1.0
 * Descripción: Adaptador para el carrusel de noticias en la pantalla de inicio, 
 *              mostrando previsualizaciones horizontales.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.adapters.home;

import android.content.Context;
import android.content.Intent;
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

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.News;
import binaryqva.educative.preusync.ui.activities.HomeActivity;
import binaryqva.educative.preusync.utils.ui.CustomCircularProgressDrawable;

/**
 * Renderiza tarjetas de noticias optimizadas para visualización en carrusel.
 */
public class NewsCarouselAdapter extends RecyclerView.Adapter<NewsCarouselAdapter.ViewHolder> {

    private final Context context;
    private final List<News> data;

    public NewsCarouselAdapter(Context context, List<News> data) {
        this.context = context;
        this.data = data != null ? data : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_mini, parent, false);
        
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
        TextView title, sourceText;
        ImageView featuredImage, detailsImageView;
        View body, scrim, infoLayout;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.heading);
            sourceText = itemView.findViewById(R.id.sourceText);
            featuredImage = itemView.findViewById(R.id.featuredImage);
            body = itemView.findViewById(R.id.body);
            scrim = itemView.findViewById(R.id.scrim);
            infoLayout = itemView.findViewById(R.id.infoLayout);
            detailsImageView = itemView.findViewById(R.id.detailsImageView);
        }

        void bind(News news) {
            title.setText(news.getHeadline());
            sourceText.setText(news.getSource());

            String photoUrl = news.getImageUrl();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infoLayout.getLayoutParams();

            if (photoUrl != null && !photoUrl.isEmpty() && !"null".equals(photoUrl)) {
                featuredImage.setVisibility(View.VISIBLE);
                if (scrim != null) scrim.setVisibility(View.VISIBLE);
                
                params.removeRule(RelativeLayout.CENTER_IN_PARENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                
                CustomCircularProgressDrawable progress = new CustomCircularProgressDrawable(context);
                progress.start();

                Glide.with(context).asBitmap().load(Uri.parse(photoUrl))
                        .placeholder(progress).error(R.drawable.ic_image_broken)
                        .listener(new RequestListener<Bitmap>() {
                            @Override public boolean onResourceReady(Bitmap r, Object m, Target<Bitmap> t, DataSource d, boolean f) { progress.stop(); return false; }
                            @Override public boolean onLoadFailed(@Nullable GlideException e, Object m, Target<Bitmap> t, boolean f) { progress.stop(); return false; }
                        }).into(featuredImage);
            } else {
                featuredImage.setVisibility(View.GONE);
                if (scrim != null) scrim.setVisibility(View.GONE);
                
                params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            }
            infoLayout.setLayoutParams(params);

            View.OnClickListener detailListener = v -> {
                if (context instanceof HomeActivity) ((HomeActivity) context).showNewsDetailsBottomSheet(news);
            };
            body.setOnClickListener(detailListener);
            if (detailsImageView != null) detailsImageView.setOnClickListener(detailListener);

            // Fuente (autor o categoría en este modelo)
            sourceText.setOnClickListener(null); // Reset
        }
    }
}



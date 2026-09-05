/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: PostsFragmentHelper.java
 * Versión: v1.4.1
 * Descripción: Ayudante para el PostsFragment. Gestiona la actualización
 *              del feed, sincronización de votos y control de SwipeRefresh.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.helpers;

import android.content.Context;
import binaryqva.educative.preusync.debug.AppLogger;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.ui.activities.HomeActivity;
import binaryqva.educative.preusync.ui.adapters.PostsAdapter;
import binaryqva.educative.preusync.ui.viewmodels.HomeViewModel;
import binaryqva.educative.preusync.utils.ui.SwipeRefreshHelper;

/**
 * Facilita la interacción entre el fragmento de comunidad y su adaptador.
 * Centraliza las peticiones de refresco y la inyección de nuevos datos paginados.
 */
public class PostsFragmentHelper {

    private static final String TAG = "PostsFragmentHelper";

    private final Context context;
    private final HomeActivity homeActivity;
    private final HomeViewModel viewModel;
    private final SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshHelper swipeRefreshHelper;

    private RecyclerView recyclerView;
    private PostsAdapter adapter;

    private Runnable onLoadMoreListener;

    public PostsFragmentHelper(Context context, SwipeRefreshLayout swipeRefreshLayout,
                               int colorAccent, HomeActivity homeActivity,
                               HomeViewModel viewModel) {
        this.context = context;
        this.homeActivity = homeActivity;
        this.viewModel = viewModel;
        this.swipeRefreshLayout = swipeRefreshLayout;

        if (swipeRefreshLayout != null) {
            this.swipeRefreshHelper = new SwipeRefreshHelper(swipeRefreshLayout, colorAccent);
            this.swipeRefreshHelper.setOnRefreshListener(() -> {
                AppLogger.d(TAG, "Gesto de refresco detectado");
                if (onLoadMoreListener != null) onLoadMoreListener.run();
            });
        }
    }

    /**
     * Vincula el RecyclerView e inicializa el adaptador con el contexto de red.
     */
    public void setRecyclerView(RecyclerView rv) {
        this.recyclerView = rv;
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.adapter = new PostsAdapter(context, new ArrayList<>(), homeActivity, viewModel, swipeRefreshLayout);
        this.recyclerView.setAdapter(this.adapter);
    }

    public void setOnLoadMoreListener(Runnable l) { this.onLoadMoreListener = l; }

    /**
     * Actualiza el listado de publicaciones en el adaptador.
     * Soporta carga incremental de páginas adicionales.
     */
    public void updatePosts(List<Post> list, boolean isFirstPage, boolean hasMore) {
        if (adapter == null) return;

        if (isFirstPage) adapter.setData(list);
        else if (list != null && !list.isEmpty()) adapter.addItems(list);

        // Sincronización del estado del cargador de pie de página.
        if (adapter instanceof binaryqva.educative.preusync.ui.adapters.common.FooterableAdapter) {
            ((binaryqva.educative.preusync.ui.adapters.common.FooterableAdapter) adapter)
                .setFooterState(binaryqva.educative.preusync.ui.adapters.common.FooterableAdapter.FOOTER_NONE);
        }

        adapter.notifyDataSetChanged();
    }

    public void onVoteUpdated(String id, long likes, long dislikes, int state) {
        if (adapter != null) {
            adapter.updateUserVote(id, state);
            adapter.updatePostCounts(id, likes, dislikes);
            adapter.notifyDataSetChanged();
        }
    }

    public void startRefreshing() { if (swipeRefreshHelper != null) swipeRefreshHelper.startRefreshingAnimation(); }
    public void finishRefreshing() { if (swipeRefreshHelper != null) swipeRefreshHelper.finishRefresh(); }

    public void cleanup() { if (swipeRefreshHelper != null) swipeRefreshHelper.cleanup(); }

    public PostsAdapter getAdapter() { return adapter; }
}




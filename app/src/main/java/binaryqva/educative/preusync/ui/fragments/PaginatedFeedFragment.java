/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase Abstracta: PaginatedFeedFragment.java
 * Versión: v6.4.9
 * Descripción: Abstracción genérica para feeds con soporte de persistencia
 *              local, paginación y manejo de estados de carga.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.fragments;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.ui.adapters.common.FooterableAdapter;
import binaryqva.educative.preusync.utils.common.CacheManager;
import binaryqva.educative.preusync.utils.common.OfflineSnackbarManager;
import binaryqva.educative.preusync.utils.common.PaginationState;
import binaryqva.educative.preusync.utils.ui.SmartSwipeRefreshLayout;
import binaryqva.educative.preusync.utils.ui.SwipeRefreshHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Abstracción genérica para feeds con soporte de persistencia local.
 * @param <T> Tipo de dato de la lista.
 */
public abstract class PaginatedFeedFragment<T> extends BaseFragment {

    protected RecyclerView recyclerView;
    protected SmartSwipeRefreshLayout swipeRefreshLayout;

    protected View loaderLayout;
    protected View emptyLayout;
    protected View errorLayout;

    protected SwipeRefreshHelper swipeRefreshHelper;
    protected OfflineSnackbarManager offlineSnackbarManager;
    protected CacheManager cacheManager;

    protected List<T> itemsList = new ArrayList<>();
    protected RecyclerView.Adapter adapter;
    protected PaginationState<T> currentState;
    protected boolean loading = false;

    private final Handler loadMoreHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingLoadMoreRunnable;

    protected abstract String getCacheKey();
    protected abstract RecyclerView.Adapter createAdapter(List<T> data);

    @Override
    protected void setupViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // BÚSQUEDA DE LAYOUTS DE ESTADO (ESTANDARIZADOS):
        loaderLayout = view.findViewById(R.id.loaderLayout);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        errorLayout = view.findViewById(R.id.errorLayout);

        setupRecyclerView();
        setupRefreshLogic(view);

        cacheManager = CacheManager.getInstance();
        adapter = createAdapter(itemsList);
        recyclerView.setAdapter(adapter);

        updateUiState(PaginationState.STATE_LOADING);

        setupScrollListener();
    }

    /**
     * Procesa y visualiza el estado de paginación actual.
     * @param state Objeto PaginationState inmutable.
     */
    protected void handlePaginationState(PaginationState<T> state) {
        if (state == null) return;
        this.currentState = state;
        this.loading = state.state == PaginationState.STATE_LOADING;

        updateUiState(state.state);

        if (state.state == PaginationState.STATE_CONTENT || state.state == PaginationState.STATE_EMPTY) {
            itemsList.clear();
            itemsList.addAll(state.items);
            adapter.notifyDataSetChanged();
            
            if (adapter instanceof FooterableAdapter) {
                ((FooterableAdapter) adapter).setFooterState(FooterableAdapter.FOOTER_NONE);
            }
        } else if (state.state == PaginationState.STATE_ERROR) {
            if (adapter instanceof FooterableAdapter && !itemsList.isEmpty()) {
                ((FooterableAdapter) adapter).setFooterState(FooterableAdapter.FOOTER_ERROR);
            }
        }

        if (swipeRefreshHelper != null) swipeRefreshHelper.finishRefresh();
        
        boolean hasData = !state.items.isEmpty();
        offlineSnackbarManager.setSectionHasCache(getCacheKey(), hasData);
        offlineSnackbarManager.checkAndUpdate();
    }

    private void updateUiState(int state) {
        if (recyclerView != null) recyclerView.setVisibility((state == PaginationState.STATE_CONTENT || (state == PaginationState.STATE_LOADING && !itemsList.isEmpty())) ? View.VISIBLE : View.GONE);
        if (loaderLayout != null) loaderLayout.setVisibility(state == PaginationState.STATE_LOADING && itemsList.isEmpty() ? View.VISIBLE : View.GONE);
        if (emptyLayout != null) emptyLayout.setVisibility(state == PaginationState.STATE_EMPTY ? View.VISIBLE : View.GONE);
        if (errorLayout != null) errorLayout.setVisibility(state == PaginationState.STATE_ERROR && itemsList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setTargetRecyclerView(recyclerView);
        }
    }

    private void setupRefreshLogic(View view) {
        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);
        swipeRefreshHelper = new SwipeRefreshHelper(swipeRefreshLayout, colorAccent);
        swipeRefreshHelper.setOnRefreshListener(this::resetAndReload);

        offlineSnackbarManager = new OfflineSnackbarManager(this, view, this::resetAndReload);
        String key = getCacheKey();
        offlineSnackbarManager.setSectionHasCache(key, CacheManager.getInstance().hasCache(key));
        offlineSnackbarManager.checkAndUpdate();
    }

    @Override
    public void onResume() { super.onResume(); offlineSnackbarManager.checkAndUpdate(); }

    @Override
    public void onPause() {
        super.onPause();
        offlineSnackbarManager.dismiss();
        if (pendingLoadMoreRunnable != null) loadMoreHandler.removeCallbacks(pendingLoadMoreRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (swipeRefreshHelper != null) swipeRefreshHelper.cleanup();
        offlineSnackbarManager.onDestroy();
        loadMoreHandler.removeCallbacksAndMessages(null);
    }

    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                    if (lm == null) return;
                    int last = lm.findLastVisibleItemPosition(), total = adapter.getItemCount();
                    boolean hasMore = currentState != null && currentState.hasMore;
                    if (last >= total - 2 && hasMore && !loading && total > 0) {
                        if (pendingLoadMoreRunnable != null) loadMoreHandler.removeCallbacks(pendingLoadMoreRunnable);
                        pendingLoadMoreRunnable = () -> {
                            if (currentState != null && currentState.hasMore && !loading) {
                                if (adapter instanceof FooterableAdapter) ((FooterableAdapter) adapter).setFooterState(FooterableAdapter.FOOTER_LOADING);
                                loadMoreItems();
                            }
                        };
                        loadMoreHandler.postDelayed(pendingLoadMoreRunnable, 250);
                    }
                }
            }
        });
    }

    protected abstract void loadMoreItems();

    public void resetAndReload() {
        if (pendingLoadMoreRunnable != null) loadMoreHandler.removeCallbacks(pendingLoadMoreRunnable);
        itemsList.clear(); 
        loading = false;
        updateUiState(PaginationState.STATE_LOADING);
        if (swipeRefreshHelper != null) swipeRefreshHelper.startRefreshingAnimation();
        loadMoreItems();
    }

    @Override
    protected void showLoading(boolean show) {
        if (swipeRefreshHelper != null) {
            if (show) swipeRefreshHelper.startRefreshingAnimation();
            else swipeRefreshHelper.finishRefresh();
        }
    }
}



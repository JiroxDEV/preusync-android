/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: HomeFragmentHelper.java
 * Versión: v2.9.8
 * Descripción: Ayudante para el HomeFragment refactorizado para modelos tipados.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.network.models.Ephemeris;
import binaryqva.educative.preusync.network.models.News;
import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.ui.adapters.home.HomeFeedAdapter;
import binaryqva.educative.preusync.ui.viewmodels.HomeViewModel;
import binaryqva.educative.preusync.utils.ui.SwipeRefreshHelper;

/**
 * Provee soporte lógico al fragmento de inicio.
 */
public class HomeFragmentHelper {

    private static final String TAG = "PreuSync_HomeFragmentHelper";

    private final Context context;
    private final HomeViewModel viewModel;
    private final SwipeRefreshHelper swipeRefreshHelper;
    private HomeFeedAdapter adapter;
    private RecyclerView recyclerView;

    private boolean isAutoScrollNewsRunning = false, isAutoScrollPostsRunning = false;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollNewsRunnable, autoScrollPostsRunnable;

    private int newsState = HomeViewModel.STATE_LOADING;
    private int postsState = HomeViewModel.STATE_LOADING;
    private int ephemerisState = HomeViewModel.STATE_LOADING;
    private int shiftState = HomeViewModel.STATE_LOADING;

    private List<News> currentNews = new ArrayList<>();
    private List<Post> currentPosts = new ArrayList<>();
    private Ephemeris currentEphemeris = null;

    public HomeFragmentHelper(Context context, SwipeRefreshLayout swipeRefreshLayout,
                              int colorAccent, HomeViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        this.swipeRefreshHelper = new SwipeRefreshHelper(swipeRefreshLayout, colorAccent);
        this.swipeRefreshHelper.setOnRefreshListener(() -> {
            if (viewModel != null) viewModel.loadHomeData();
        });
    }

    public void setRecyclerView(RecyclerView rv) {
        this.recyclerView = rv;
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.adapter = new HomeFeedAdapter(context, viewModel);
        this.recyclerView.setAdapter(this.adapter);

        this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) { stopAutoScrollNews(); stopAutoScrollPosts(); }
            }
        });
    }

    public void updateNews(List<News> list) {
        currentNews = list != null ? list : new ArrayList<>();
        if (adapter != null) adapter.updateNews(currentNews);
        if (currentNews.size() > 1) startAutoScrollNewsDelayed();
        else stopAutoScrollNews();
    }

    public void updatePosts(List<Post> list) {
        currentPosts = list != null ? list : new ArrayList<>();
        if (adapter != null) adapter.updatePosts(currentPosts);
        if (currentPosts.size() > 1) startAutoScrollPostsDelayed();
        else stopAutoScrollPosts();
    }

    public void updateEphemeris(Ephemeris data) {
        currentEphemeris = data;
        if (adapter != null) adapter.updateEphemeris(data);
    }

    public void updateShift(String num, String range, String subj) {
        if (adapter != null) adapter.updateShift(num, range, subj);
    }

    public void updateVisibility(boolean news, boolean posts, boolean eph, boolean sh) {
        if (adapter != null) {
            adapter.setVisibility(news, posts, eph, sh);
        }
    }

    public void setNewsState(int s) { if (this.newsState != s) { this.newsState = s; if (adapter != null) adapter.setNewsState(s); } }
    public void setPostsState(int s) { if (this.postsState != s) { this.postsState = s; if (adapter != null) adapter.setPostsState(s); } }
    public void setEphemerisState(int s) { if (this.ephemerisState != s) { this.ephemerisState = s; if (adapter != null) adapter.setEphemerisState(s); } }
    public void setShiftState(int s) { if (this.shiftState != s) { this.shiftState = s; if (adapter != null) adapter.setShiftState(s); } }

    private void startAutoScrollNewsDelayed() {
        autoScrollHandler.removeCallbacks(autoScrollNewsRunnable);
        autoScrollHandler.postDelayed(() -> {
            if (adapter != null && adapter.getNewsRecyclerView() != null) startAutoScrollNews(adapter.getNewsRecyclerView());
        }, 500);
    }

    private void startAutoScrollPostsDelayed() {
        autoScrollHandler.removeCallbacks(autoScrollPostsRunnable);
        autoScrollHandler.postDelayed(() -> {
            if (adapter != null && adapter.getPostsRecyclerView() != null) startAutoScrollPosts(adapter.getPostsRecyclerView());
        }, 500);
    }

    private void startAutoScrollNews(RecyclerView rv) {
        if (isAutoScrollNewsRunning) return;
        isAutoScrollNewsRunning = true;
        autoScrollNewsRunnable = new Runnable() {
            @Override public void run() {
                if (!isAutoScrollNewsRunning || rv == null || rv.getAdapter() == null) { stopAutoScrollNews(); return; }
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null || rv.getAdapter().getItemCount() <= 1) { stopAutoScrollNews(); return; }
                int next = (lm.findFirstVisibleItemPosition() + 1) % rv.getAdapter().getItemCount();
                rv.smoothScrollToPosition(next);
                autoScrollHandler.postDelayed(this, 3000);
            }
        };
        autoScrollHandler.postDelayed(autoScrollNewsRunnable, 3000);
    }

    private void startAutoScrollPosts(RecyclerView rv) {
        if (isAutoScrollPostsRunning) return;
        isAutoScrollPostsRunning = true;
        autoScrollPostsRunnable = new Runnable() {
            @Override public void run() {
                if (!isAutoScrollPostsRunning || rv == null || rv.getAdapter() == null) { stopAutoScrollPosts(); return; }
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null || rv.getAdapter().getItemCount() <= 1) { stopAutoScrollPosts(); return; }
                int next = (lm.findFirstVisibleItemPosition() + 1) % rv.getAdapter().getItemCount();
                rv.smoothScrollToPosition(next);
                autoScrollHandler.postDelayed(this, 3000);
            }
        };
        autoScrollHandler.postDelayed(autoScrollPostsRunnable, 3000);
    }

    public void stopAutoScrollNews() { isAutoScrollNewsRunning = false; if (autoScrollNewsRunnable != null) autoScrollHandler.removeCallbacks(autoScrollNewsRunnable); }
    public void stopAutoScrollPosts() { isAutoScrollPostsRunning = false; if (autoScrollPostsRunnable != null) autoScrollHandler.removeCallbacks(autoScrollPostsRunnable); }

    public void clearTimers() { stopAutoScrollNews(); stopAutoScrollPosts(); autoScrollHandler.removeCallbacksAndMessages(null); }

    public void startRefreshing() { swipeRefreshHelper.startRefreshingAnimation(); }
    public void finishRefreshing() { swipeRefreshHelper.finishRefresh(); }

    public void cleanup() { clearTimers(); if (swipeRefreshHelper != null) swipeRefreshHelper.cleanup(); }

    public HomeFeedAdapter getAdapter() { return adapter; }
    public List<News> getCurrentNews() { return currentNews; }
    public List<Post> getCurrentPosts() { return currentPosts; }
    public Ephemeris getCurrentEphemeris() { return currentEphemeris; }
}



/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: HomeFragment.java
 * Versión: v14.0.0
 * Descripción: Fragmento de inicio que presenta un feed dinámico. Refactorizado
 *              para usar HomeViewModel y heredar de BaseFragment.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Map;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.ui.activities.HomeActivity;
import binaryqva.educative.preusync.ui.helpers.HomeFragmentHelper;
import binaryqva.educative.preusync.ui.viewmodels.HomeViewModel;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.theme.ThemeManager;
import binaryqva.educative.preusync.utils.ui.SmartSwipeRefreshLayout;

public class HomeFragment extends BaseFragment {

    private HomeViewModel viewModel;
    private HomeFragmentHelper helper;
    private boolean isRefreshing = false;

    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    @Override protected int getLayoutResId() { return R.layout.fragment_home; }

    @Override
    protected void setupViews(View view) {
        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        if (swipeRefreshLayout instanceof SmartSwipeRefreshLayout) {
            ((SmartSwipeRefreshLayout) swipeRefreshLayout).setTargetRecyclerView(recyclerView);
        }

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        helper = new HomeFragmentHelper(requireContext(), swipeRefreshLayout, colorAccent, viewModel);
        helper.setRecyclerView(recyclerView);

        updateFeaturedVisibility();
        viewModel.loadHomeData();
    }

    @Override
    protected void setupObservers() {
        if (viewModel == null || helper == null) return;

        viewModel.getNewsState().observe(getViewLifecycleOwner(), state -> { helper.setNewsState(state); checkRefreshState(); });
        viewModel.getPostsState().observe(getViewLifecycleOwner(), state -> { helper.setPostsState(state); checkRefreshState(); });
        viewModel.getEphemerisState().observe(getViewLifecycleOwner(), state -> { helper.setEphemerisState(state); checkRefreshState(); });
        viewModel.getShiftState().observe(getViewLifecycleOwner(), state -> { helper.setShiftState(state); checkRefreshState(); });

        viewModel.getNewsList().observe(getViewLifecycleOwner(), list -> helper.updateNews(list));
        viewModel.getPostsList().observe(getViewLifecycleOwner(), list -> helper.updatePosts(list));
        viewModel.getEphemerisData().observe(getViewLifecycleOwner(), ephemeris -> helper.updateEphemeris(ephemeris));
        viewModel.getShiftData().observe(getViewLifecycleOwner(), shift -> {
            if (shift != null) helper.updateShift(shift.shiftNumber, shift.timeRange, shift.subject);
        });

        viewModel.getVoteStates().observe(getViewLifecycleOwner(), voteMap -> {
            if (helper != null && helper.getAdapter() != null) {
                for (Map.Entry<String, Integer> entry : voteMap.entrySet()) {
                    helper.getAdapter().updatePostVote(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFeaturedVisibility();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (helper != null) helper.clearTimers();
        timeoutHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (helper != null) helper.cleanup();
        timeoutHandler.removeCallbacksAndMessages(null);
    }

    private void checkRefreshState() {
        if (viewModel == null) return;
        Integer nS = viewModel.getNewsState().getValue();
        Integer pS = viewModel.getPostsState().getValue();
        Integer eS = viewModel.getEphemerisState().getValue();
        Integer sS = viewModel.getShiftState().getValue();

        if (nS == null || pS == null || eS == null || sS == null) return;

        boolean isLoading = (nS == HomeViewModel.STATE_LOADING || pS == HomeViewModel.STATE_LOADING || eS == HomeViewModel.STATE_LOADING || sS == HomeViewModel.STATE_LOADING);

        if (isLoading && !isRefreshing) {
            isRefreshing = true;
            helper.startRefreshing();
            startRefreshTimeout();
        } else if (!isLoading && isRefreshing) {
            isRefreshing = false;
            helper.finishRefreshing();
            if (timeoutRunnable != null) timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    private void startRefreshTimeout() {
        if (timeoutRunnable != null) timeoutHandler.removeCallbacks(timeoutRunnable);
        timeoutRunnable = () -> {
            if (isRefreshing) { isRefreshing = false; helper.finishRefreshing(); AppLogger.w(TAG, "SwipeRefresh timeout"); }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 5000);
    }

    private void updateFeaturedVisibility() {
        PreferenceManager prefs = PreferenceManager.getInstance(requireContext());
        helper.updateVisibility(prefs.isShowFeaturedNews(), prefs.isShowFeaturedPosts(), 
            prefs.isShowDailyEphemeris(), prefs.isShowCurrentShift());
    }

    public void clearTimers() {
        if (helper != null) helper.clearTimers();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}



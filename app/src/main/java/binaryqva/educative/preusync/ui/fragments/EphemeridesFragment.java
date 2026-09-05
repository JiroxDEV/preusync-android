/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: EphemeridesFragment.java
 * Versión: v7.0.0
 * Descripción: Fragmento de efemérides. Implementa el patrón BaseFragment y 
 *              soporte Room.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.Ephemeris;
import binaryqva.educative.preusync.ui.viewmodels.EphemeridesViewModel;
import binaryqva.educative.preusync.utils.common.DialogHelper;
import binaryqva.educative.preusync.utils.common.OfflineSnackbarManager;
import binaryqva.educative.preusync.utils.ui.SmartSwipeRefreshLayout;
import binaryqva.educative.preusync.utils.ui.SwipeRefreshHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class EphemeridesFragment extends BaseFragment {

    private static final int STATE_LOADING = 0;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_ERROR = 2;
    private static final int STATE_CONTENT = 3;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView dateTextView;
    private ImageButton changeDateButton;

    private SwipeRefreshHelper swipeRefreshHelper;
    private OfflineSnackbarManager offlineSnackbarManager;

    private EphemeridesViewModel ephemeridesViewModel;
    private EphemeridesAdapter adapter;

    @Override protected int getLayoutResId() { return R.layout.fragment_events_ephemeris; }

    @Override
    protected void setupViews(View view) {
        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        dateTextView = view.findViewById(R.id.dateText);
        changeDateButton = view.findViewById(R.id.changeDateButton);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (swipeRefreshLayout instanceof SmartSwipeRefreshLayout) {
            ((SmartSwipeRefreshLayout) swipeRefreshLayout).setTargetRecyclerView(recyclerView);
        }
        adapter = new EphemeridesAdapter();
        recyclerView.setAdapter(adapter);

        swipeRefreshHelper = new SwipeRefreshHelper(swipeRefreshLayout, colorAccent);
        swipeRefreshHelper.setOnRefreshListener(() -> ephemeridesViewModel.refresh());

        offlineSnackbarManager = new OfflineSnackbarManager(this, view, () -> ephemeridesViewModel.refresh());
        
        ephemeridesViewModel = new ViewModelProvider(requireActivity()).get(EphemeridesViewModel.class);
        changeDateButton.setOnClickListener(v -> showDayMonthPicker());
    }

    @Override
    protected void setupObservers() {
        ephemeridesViewModel.getEphemerisList().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                adapter.setData(new ArrayList<>(list));
                adapter.setState(list.isEmpty() ? STATE_EMPTY : STATE_CONTENT);
            }
            if (swipeRefreshHelper != null) swipeRefreshHelper.finishRefresh();
            offlineSnackbarManager.checkAndUpdate();
        });

        ephemeridesViewModel.getEphemerisState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            if (state == EphemeridesViewModel.STATE_LOADING) adapter.setState(STATE_LOADING);
            else if (state == EphemeridesViewModel.STATE_ERROR) adapter.setState(STATE_ERROR);
            if (swipeRefreshHelper != null) swipeRefreshHelper.finishRefresh();
        });

        ephemeridesViewModel.getFormattedDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) dateTextView.setText(date);
        });
    }

    @Override public void onResume() { super.onResume(); offlineSnackbarManager.checkAndUpdate(); }
    @Override public void onPause() { super.onPause(); offlineSnackbarManager.dismiss(); }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (swipeRefreshHelper != null) swipeRefreshHelper.cleanup();
        offlineSnackbarManager.onDestroy();
    }

    private void showDayMonthPicker() {
        Calendar c = ephemeridesViewModel.getSelectedDate();
        DialogHelper.showDatePickerDialog(getActivity(), getString(R.string.label_select_date),
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), (day, month) -> {
                    adapter.setState(STATE_LOADING);
                    ephemeridesViewModel.setSelectedDate(day, month);
                });
    }

    private class EphemeridesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Ephemeris> data = new ArrayList<>();
        private int state = STATE_LOADING;

        @Override public int getItemCount() { return data.isEmpty() ? 1 : data.size(); }
        @Override public int getItemViewType(int position) { return data.isEmpty() ? state : 100; }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int p) {
            if (h instanceof ItemViewHolder) ((ItemViewHolder) h).bind(data.get(p));
        }

        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup pr, int vt) {
            LayoutInflater inf = LayoutInflater.from(pr.getContext());
            if (vt == STATE_LOADING) return new LoadingViewHolder(inf.inflate(R.layout.item_loading, pr, false));
            if (vt == STATE_EMPTY) return new EmptyViewHolder(inf.inflate(R.layout.item_empty_ephemeris, pr, false));
            if (vt == STATE_ERROR) return new ErrorViewHolder(inf.inflate(R.layout.item_error_ephemeris, pr, false));
            return new ItemViewHolder(inf.inflate(R.layout.item_ephemeris, pr, false));
        }

        void setData(List<Ephemeris> d) { this.data = d; notifyDataSetChanged(); }
        void setState(int s) { this.state = s; notifyDataSetChanged(); }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc;
            ItemViewHolder(View v) { super(v); title = v.findViewById(R.id.heading); desc = v.findViewById(R.id.detailsText); }
            void bind(Ephemeris e) { title.setText(e.getTitle()); desc.setText(e.getDetails()); }
        }
        class LoadingViewHolder extends RecyclerView.ViewHolder { LoadingViewHolder(View v) { super(v); } }
        class EmptyViewHolder extends RecyclerView.ViewHolder { EmptyViewHolder(View v) { super(v); } }
        class ErrorViewHolder extends RecyclerView.ViewHolder { ErrorViewHolder(View v) { super(v); } }
    }
}



/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ScheduleFragment.java
 * Versión: v8.0.0
 * Descripción: Fragmento de horario escolar. Soporta modelos tipados para grupos.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.fragments;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.network.models.SchoolGroup;
import binaryqva.educative.preusync.ui.viewmodels.ScheduleViewModel;
import binaryqva.educative.preusync.utils.ui.SmartSwipeRefreshLayout;
import binaryqva.educative.preusync.utils.ui.SwipeRefreshHelper;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class ScheduleFragment extends BaseFragment {

    private ViewFlipper viewFlipper;
    private TableLayout scheduleTable;
    private TextInputLayout groupTextInputLayout;
    private AutoCompleteTextView groupAutoComplete;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton retryButton;

    private SwipeRefreshHelper swipeRefreshHelper;
    private ScheduleViewModel viewModel;

    private static final int ROWS = 15;
    private static final int DAYS = 5;
    private static final int COLUMNS = 7;
    private static final String[] DAY_LETTERS = {"L", "M", "X", "J", "V"};
    private static final String[] TURN_LABELS = {"1", "5M", "2", "5M", "3", "RC", "4", "5M", "5", "AZ", "6", "5M", "7", "5M", "8"};
    
    private String[] currentTimeRanges;
    private Typeface monoTypeface;
    private final TextView[][] cells = new TextView[ROWS][COLUMNS];
    private final TextView[] headerDayViews = new TextView[DAYS];

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateHighlightRunnable;

    @Override protected int getLayoutResId() { return R.layout.fragment_schedule; }

    @Override
    protected void setupViews(View view) {
        viewFlipper = view.findViewById(R.id.viewFlipper);
        scheduleTable = view.findViewById(R.id.scheduleTable);
        groupTextInputLayout = view.findViewById(R.id.groupTextInputLayout);
        groupAutoComplete = view.findViewById(R.id.groupAutoComplete);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        retryButton = view.findViewById(R.id.retryButton);

        if (swipeRefreshLayout instanceof SmartSwipeRefreshLayout) {
            ((SmartSwipeRefreshLayout) swipeRefreshLayout).setTargetScrollableView(view.findViewById(R.id.scrollView));
        }

        int colorAccent = ThemeManager.getThemeColor(requireContext(), R.attr.colorAccent);
        swipeRefreshHelper = new SwipeRefreshHelper(swipeRefreshLayout, colorAccent);
        swipeRefreshHelper.setOnRefreshListener(() -> viewModel.refreshAll());

        monoTypeface = ResourcesCompat.getFont(requireContext(), R.font.roboto_mono);
        if (monoTypeface == null) monoTypeface = Typeface.MONOSPACE;

        retryButton.setOnClickListener(v -> viewModel.refreshAll());

        viewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModel.class);
        viewModel.loadGroups();
    }

    @Override
    protected void setupObservers() {
        viewModel.getCombinedState().observe(getViewLifecycleOwner(), this::updateUiState);

        viewModel.getGroupsList().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null && !groups.isEmpty()) {
                List<String> names = new ArrayList<>();
                for (SchoolGroup sg : groups) names.add(sg.getName());
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
                groupAutoComplete.setAdapter(adapter);
                groupAutoComplete.setOnItemClickListener((p, v, pos, id) -> viewModel.selectGroup(names.get(pos)));
                String current = viewModel.getCurrentGroup().getValue();
                groupAutoComplete.setText(current != null && names.contains(current) ? current : names.get(0), false);
            }
        });

        viewModel.getNormalScheduleMap().observe(getViewLifecycleOwner(), map -> {
            if (viewModel.getCombinedState().getValue() != null && viewModel.getCombinedState().getValue() == ScheduleViewModel.STATE_SUCCESS) {
                displaySchedule();
            }
        });
    }

    private void updateUiState(int state) {
        if (viewFlipper != null) viewFlipper.setDisplayedChild(state);
        if (swipeRefreshHelper != null) swipeRefreshHelper.finishRefresh();
        groupTextInputLayout.setVisibility(state == ScheduleViewModel.STATE_SUCCESS || state == ScheduleViewModel.STATE_EMPTY ? View.VISIBLE : View.GONE);
    }

    private void displaySchedule() {
        String group = viewModel.getCurrentGroup().getValue();
        Map<String, Boolean> typeMap = viewModel.getScheduleTypeByGroup().getValue();
        if (group == null || typeMap == null) return;

        boolean isContinuous = typeMap.containsKey(group) && typeMap.get(group);
        String[][] matrix = isContinuous ? viewModel.getContinuousScheduleMap().getValue().get(group) : viewModel.getNormalScheduleMap().getValue().get(group);
        
        currentTimeRanges = isContinuous ? new String[]{"08:00-08:45", "08:45-08:50", "08:50-09:35", "09:35-09:40", "09:40-10:25", "10:25-10:45", "10:45-11:30", "11:30-11:35", "11:35-12:20", "12:20-13:20", "13:20-14:05", "14:05-14:10", "14:10-14:55", "14:55-15:00", "15:00-15:45"} 
                                         : new String[]{"07:40-08:25", "08:25-08:30", "08:30-09:15", "09:15-09:20", "09:20-10:05", "10:05-10:25", "10:25-11:10", "11:10-11:15", "11:15-12:00", "12:00-13:00", "13:00-13:45", "13:45-13:50", "13:50-14:35", "14:40-15:25", "14:40-15:25"};

        if (matrix == null) return;
        renderTable(matrix);
        applyHighlight();
        startPeriodicUpdate();
    }

    private void renderTable(String[][] matrix) {
        int sCol = ThemeManager.getThemeColor(requireContext(), R.attr.colorHorarioAsignatura);
        int fCol = ThemeManager.getThemeColor(requireContext(), R.attr.colorHorarioLibre);
        int hCol = ThemeManager.getThemeColor(requireContext(), R.attr.colorHorarioPrincipal);
        int tCol = ThemeManager.getThemeColor(requireContext(), R.attr.colorText);

        scheduleTable.removeAllViews();
        addHeaderRow(hCol, tCol);

        for (int r = 0; r < ROWS; r++) {
            TableRow row = new TableRow(requireContext());
            boolean isClass = (r % 2 == 0);
            cells[r][0] = createCell(TURN_LABELS[r], hCol, tCol, true, false);
            cells[r][1] = createCell(currentTimeRanges[r], hCol, tCol, true, false);
            row.addView(cells[r][0]); row.addView(cells[r][1]);
            for (int d = 0; d < DAYS; d++) {
                String sub = matrix[r][d];
                int bg = !isClass ? fCol : (sub == null || sub.isEmpty() || sub.equals("//") ? fCol : sCol);
                cells[r][d + 2] = createCell(!isClass ? "-" : (sub == null || sub.isEmpty() ? "//" : sub), bg, tCol, false, true);
                row.addView(cells[r][d + 2]);
            }
            scheduleTable.addView(row);
        }
    }

    private void addHeaderRow(int bg, int text) {
        TableRow hRow = new TableRow(requireContext());
        hRow.addView(createCell(getString(R.string.label_shift), bg, text, true, false));
        hRow.addView(createCell(getString(R.string.label_time), bg, text, true, false));
        for (int i = 0; i < DAYS; i++) {
            headerDayViews[i] = createCell(DAY_LETTERS[i], bg, text, true, true);
            hRow.addView(headerDayViews[i]);
        }
        scheduleTable.addView(hRow);
    }

    private TextView createCell(String txt, int bg, int text, boolean bold, boolean weight) {
        TextView tv = new TextView(requireContext());
        tv.setText(txt); tv.setGravity(Gravity.CENTER); tv.setBackgroundColor(bg); tv.setTextColor(text);
        tv.setTypeface(monoTypeface, bold ? Typeface.BOLD : Typeface.NORMAL);
        int p = (int) (4 * getResources().getDisplayMetrics().density); tv.setPadding(p, p, p, p);
        tv.setLayoutParams(new TableRow.LayoutParams(weight ? 0 : TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, weight ? 1 : 0));
        return tv;
    }

    private void startPeriodicUpdate() {
        stopPeriodicUpdate();
        updateHighlightRunnable = () -> {
            if (isAdded()) { applyHighlight(); handler.postDelayed(updateHighlightRunnable, 60000); }
        };
        handler.postDelayed(updateHighlightRunnable, (60 - Calendar.getInstance().get(Calendar.SECOND)) * 1000L);
    }

    private void stopPeriodicUpdate() { if (updateHighlightRunnable != null) handler.removeCallbacks(updateHighlightRunnable); }

    private void applyHighlight() {
        int r = getCurrentRow(), c = getCurrentColumn();
        int sH = ThemeManager.getThemeColor(requireContext(), R.attr.colorHorarioResaltadoSecundario);
        int pH = ThemeManager.getThemeColor(requireContext(), R.attr.colorHorarioResaltadoPrincipal);
        int bg = ThemeManager.getThemeColor(requireContext(), R.attr.colorBackground);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (cells[i][j] == null) continue;
                if (i == r || j == c) cells[i][j].setBackgroundColor(sH);
                if (i == r && j == c) { cells[i][j].setBackgroundColor(pH); cells[i][j].setTextColor(bg); }
            }
        }
    }

    private int getCurrentRow() {
        int now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE);
        for (int i = 0; i < currentTimeRanges.length; i++) {
            String[] p = currentTimeRanges[i].split("-");
            if (now >= toMin(p[0]) && now < toMin(p[1])) return i;
        }
        return -1;
    }

    private int getCurrentColumn() {
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: return 2; case Calendar.TUESDAY: return 3;
            case Calendar.WEDNESDAY: return 4; case Calendar.THURSDAY: return 5;
            case Calendar.FRIDAY: return 6; default: return -1;
        }
    }

    private int toMin(String t) { String[] p = t.split(":"); return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]); }

    @Override public void onResume() { super.onResume(); }
    @Override public void onPause() { super.onPause(); stopPeriodicUpdate(); }
}

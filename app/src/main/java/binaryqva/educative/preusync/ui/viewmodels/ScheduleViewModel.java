/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ScheduleViewModel.java
 * Versión: v7.0.0
 * Descripción: ViewModel para la gestión del horario escolar. Implementa 
 *              soporte híbrido Room/API.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import binaryqva.educative.preusync.data.repositories.ScheduleRepository;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Schedule;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleViewModel extends AndroidViewModel {

    public static final int STATE_LOADING = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_EMPTY = 2;
    public static final int STATE_ERROR = 3;

    private final MutableLiveData<List<String>> groupsList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> groupsState = new MutableLiveData<>(STATE_LOADING);
    
    private final MutableLiveData<Map<String, String[][]>> normalScheduleMap = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, String[][]>> continuousScheduleMap = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<String, Boolean>> scheduleTypeByGroup = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<String> currentGroup = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> scheduleState = new MutableLiveData<>(STATE_LOADING);
    private final MutableLiveData<Integer> combinedState = new MutableLiveData<>(STATE_LOADING);

    private final ScheduleRepository repository;
    private final PreferenceManager preferenceManager;

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ScheduleRepository(application);
        this.preferenceManager = PreferenceManager.getInstance(application);
    }

    public LiveData<List<String>> getGroupsList() { return groupsList; }
    public LiveData<Map<String, String[][]>> getNormalScheduleMap() { return normalScheduleMap; }
    public LiveData<Map<String, String[][]>> getContinuousScheduleMap() { return continuousScheduleMap; }
    public LiveData<Map<String, Boolean>> getScheduleTypeByGroup() { return scheduleTypeByGroup; }
    public LiveData<String> getCurrentGroup() { return currentGroup; }
    public LiveData<Integer> getCombinedState() { return combinedState; }

    public void loadGroups() {
        groupsState.setValue(STATE_LOADING);
        String schoolId = preferenceManager.getSchoolId();
        if (schoolId == null) {
            groupsState.setValue(STATE_ERROR);
            updateCombinedState();
            return;
        }

        if (!AppUtils.isConnected(getApplication())) {
            // Grupos aún dependen de CacheManager/SharedPreferences por ser lista de Strings simple.
            updateCombinedState(); return;
        }

        repository.getGroups(schoolId, new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<String>>> call, @NonNull Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> groups = response.body().getData();
                    groupsList.setValue(groups);
                    groupsState.setValue(groups.isEmpty() ? STATE_EMPTY : STATE_SUCCESS);
                    if (!groups.isEmpty()) {
                        String last = preferenceManager.getLastScheduleGroup();
                        String g = (last != null && groups.contains(last)) ? last : groups.get(0);
                        selectGroup(g);
                    }
                } else groupsState.setValue(STATE_ERROR);
                updateCombinedState();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<String>>> call, @NonNull Throwable t) {
                groupsState.setValue(STATE_ERROR);
                updateCombinedState();
            }
        });
    }

    public void loadScheduleForGroup(String group) {
        if (group == null || group.isEmpty()) return;
        scheduleState.setValue(STATE_LOADING);
        currentGroup.setValue(group);
        updateCombinedState();

        String schoolId = preferenceManager.getSchoolId();
        if (schoolId == null) {
            loadFromRoom(group);
            return;
        }

        if (!AppUtils.isConnected(getApplication())) {
            loadFromRoom(group);
            return;
        }

        repository.getSchedule(group, schoolId, new Callback<ApiResponse<List<Schedule>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Schedule>>> call, @NonNull Response<ApiResponse<List<Schedule>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Schedule> data = response.body().getData();
                    processSchedule(group, data);
                    scheduleState.setValue(STATE_SUCCESS);
                    preferenceManager.setLastScheduleGroup(group);
                } else loadFromRoom(group);
                updateCombinedState();
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<List<Schedule>>> call, @NonNull Throwable t) {
                loadFromRoom(group);
                updateCombinedState();
            }
        });
    }

    private void loadFromRoom(String group) {
        repository.getLocalSchedule(group, data -> {
            if (data != null && !data.isEmpty()) {
                processSchedule(group, data);
                scheduleState.setValue(STATE_SUCCESS);
            } else {
                scheduleState.setValue(STATE_ERROR);
            }
            updateCombinedState();
        });
    }

    private void processSchedule(String group, List<Schedule> rows) {
        String[][] normalMatrix = new String[15][5];
        String[][] continuousMatrix = new String[15][5];
        for(int i=0; i<15; i++) for(int j=0; j<5; j++) { normalMatrix[i][j]=""; continuousMatrix[i][j]=""; }
        boolean isContinuous = false;

        for (Schedule s : rows) {
            int d = getDayIndex(s.getDay());
            int r = getRowIndexForShift(s.getShift());
            if (d >= 0 && r >= 0) {
                if ("Normal".equals(s.getScheduleType())) normalMatrix[r][d] = s.getSubject();
                else { continuousMatrix[r][d] = s.getSubject(); isContinuous = true; }
            }
        }
        
        Map<String, String[][]> nMap = new HashMap<>(normalScheduleMap.getValue() != null ? normalScheduleMap.getValue() : new HashMap<>());
        nMap.put(group, normalMatrix);
        normalScheduleMap.setValue(nMap);

        Map<String, String[][]> cMap = new HashMap<>(continuousScheduleMap.getValue() != null ? continuousScheduleMap.getValue() : new HashMap<>());
        cMap.put(group, continuousMatrix);
        continuousScheduleMap.setValue(cMap);

        Map<String, Boolean> tMap = new HashMap<>(scheduleTypeByGroup.getValue() != null ? scheduleTypeByGroup.getValue() : new HashMap<>());
        tMap.put(group, isContinuous);
        scheduleTypeByGroup.setValue(tMap);
    }

    private int getRowIndexForShift(int shift) {
        switch (shift) {
            case 1: return 0; case 2: return 2; case 3: return 4; case 4: return 6;
            case 5: return 8; case 6: return 10; case 7: return 12; case 8: return 14;
            default: return -1;
        }
    }

    private int getDayIndex(String day) {
        String[] days = {"L", "M", "X", "J", "V"};
        for (int i = 0; i < days.length; i++) if (days[i].equals(day)) return i;
        return -1;
    }

    public void selectGroup(String group) {
        if (group != null && !group.equals(currentGroup.getValue())) {
            currentGroup.setValue(group);
            loadScheduleForGroup(group);
        }
    }

    public void refreshAll() { loadGroups(); }

    private void updateCombinedState() {
        Integer gS = groupsState.getValue(), sS = scheduleState.getValue();
        if (gS == null || sS == null) { combinedState.setValue(STATE_LOADING); return; }
        if (gS == STATE_ERROR || sS == STATE_ERROR) combinedState.setValue(STATE_ERROR);
        else if (gS == STATE_LOADING || sS == STATE_LOADING) combinedState.setValue(STATE_LOADING);
        else if (gS == STATE_EMPTY || sS == STATE_EMPTY) combinedState.setValue(STATE_EMPTY);
        else combinedState.setValue(STATE_SUCCESS);
    }
}



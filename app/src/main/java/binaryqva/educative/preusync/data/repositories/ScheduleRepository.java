/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ScheduleRepository.java
 * Versión: v2.0.0
 * Descripción: Repositorio de horarios con soporte híbrido de red y 
 *              persistencia local mediante Room.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.data.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import binaryqva.educative.preusync.data.local.PreuSyncDatabase;
import binaryqva.educative.preusync.data.local.dao.ScheduleDao;
import binaryqva.educative.preusync.data.local.entities.ScheduleEntity;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Schedule;
import binaryqva.educative.preusync.network.retrofit.ApiService;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Orquestador de datos para horarios. Gestiona el flujo entre la API y la DB.
 */
public class ScheduleRepository {

    private final ApiService apiService;
    private final ScheduleDao scheduleDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ScheduleRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
        this.scheduleDao = PreuSyncDatabase.getInstance(context).scheduleDao();
    }

    public void getSchedule(String group, String schoolId, Callback<ApiResponse<List<Schedule>>> callback) {
        apiService.getSchedule(group, schoolId).enqueue(new Callback<ApiResponse<List<Schedule>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Schedule>>> call, @NonNull Response<ApiResponse<List<Schedule>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    saveToLocal(response.body().getData(), group);
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Schedule>>> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getGroups(String schoolId, Callback<ApiResponse<List<String>>> callback) {
        apiService.getGroups(schoolId).enqueue(callback);
    }

    private void saveToLocal(List<Schedule> list, String group) {
        if (list == null || list.isEmpty()) return;
        executor.execute(() -> {
            scheduleDao.deleteByGroup(group);
            List<ScheduleEntity> entities = new ArrayList<>();
            for (Schedule s : list) { entities.add(s.toEntity()); }
            scheduleDao.insertAll(entities);
        });
    }

    public void getLocalSchedule(String group, LocalDataCallback<List<Schedule>> callback) {
        executor.execute(() -> {
            List<ScheduleEntity> entities = scheduleDao.getScheduleByGroup(group);
            List<Schedule> list = new ArrayList<>();
            for (ScheduleEntity e : entities) { list.add(new Schedule(e)); }
            mainHandler.post(() -> callback.onDataLoaded(list));
        });
    }

    public interface LocalDataCallback<T> {
        void onDataLoaded(T data);
    }
}



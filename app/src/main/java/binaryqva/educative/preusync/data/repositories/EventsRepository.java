/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: EventsRepository.java
 * Versión: v2.0.0
 * Descripción: Repositorio de eventos con soporte híbrido de red y persistencia
 *              local mediante Room.
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
import binaryqva.educative.preusync.data.local.dao.EventDao;
import binaryqva.educative.preusync.data.local.entities.EventEntity;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Event;
import binaryqva.educative.preusync.network.retrofit.ApiService;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Orquestador de datos para eventos. Gestiona el flujo entre la API y la DB.
 */
public class EventsRepository {

    private final ApiService apiService;
    private final EventDao eventDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public EventsRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
        this.eventDao = PreuSyncDatabase.getInstance(context).eventDao();
    }

    public void getSchoolEvents(int start, int count, String schoolId, Callback<ApiResponse<List<Event>>> callback) {
        apiService.getSchoolEvents(start, count, schoolId).enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Event>>> call, @NonNull Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    saveToLocal(response.body().getData(), "school");
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Event>>> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getExternalEvents(int start, int count, String schoolId, Callback<ApiResponse<List<Event>>> callback) {
        apiService.getExternalEvents(start, count, schoolId).enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Event>>> call, @NonNull Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    saveToLocal(response.body().getData(), "external");
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Event>>> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getEventById(String id, Callback<ApiResponse<Event>> callback) {
        apiService.getEventById(id).enqueue(callback);
    }

    private void saveToLocal(List<Event> events, String type) {
        if (events == null || events.isEmpty()) return;
        executor.execute(() -> {
            List<EventEntity> entities = new ArrayList<>();
            for (Event e : events) { entities.add(e.toEntity()); }
            eventDao.insertAll(entities);
        });
    }

    public void getLocalEvents(String type, LocalDataCallback<List<Event>> callback) {
        executor.execute(() -> {
            List<EventEntity> entities = eventDao.getEventsByType(type);
            List<Event> events = new ArrayList<>();
            for (EventEntity e : entities) { events.add(new Event(e)); }
            mainHandler.post(() -> callback.onDataLoaded(events));
        });
    }

    public interface LocalDataCallback<T> {
        void onDataLoaded(T data);
    }
}



/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: EphemerisRepository.java
 * Versión: v2.0.0
 * Descripción: Repositorio de efemérides con soporte híbrido de red y 
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
import binaryqva.educative.preusync.data.local.dao.EphemerisDao;
import binaryqva.educative.preusync.data.local.entities.EphemerisEntity;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Ephemeris;
import binaryqva.educative.preusync.network.retrofit.ApiService;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Orquestador de datos para efemérides. Gestiona el flujo entre la API y la DB.
 */
public class EphemerisRepository {

    private final ApiService apiService;
    private final EphemerisDao ephemerisDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public EphemerisRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
        this.ephemerisDao = PreuSyncDatabase.getInstance(context).ephemerisDao();
    }

    public void getEphemeris(String date, Callback<ApiResponse<List<Ephemeris>>> callback) {
        apiService.getEphemeris(date).enqueue(new Callback<ApiResponse<List<Ephemeris>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Ephemeris>>> call, @NonNull Response<ApiResponse<List<Ephemeris>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    saveToLocal(response.body().getData());
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Ephemeris>>> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getEphemerisById(String id, Callback<ApiResponse<Ephemeris>> callback) {
        apiService.getEphemerisById(id).enqueue(callback);
    }

    private void saveToLocal(List<Ephemeris> list) {
        if (list == null || list.isEmpty()) return;
        executor.execute(() -> {
            List<EphemerisEntity> entities = new ArrayList<>();
            for (Ephemeris e : list) { entities.add(e.toEntity()); }
            ephemerisDao.insertAll(entities);
        });
    }

    public void getLocalEphemeris(String date, LocalDataCallback<List<Ephemeris>> callback) {
        executor.execute(() -> {
            List<EphemerisEntity> entities = ephemerisDao.getEphemerisByDate(date);
            List<Ephemeris> list = new ArrayList<>();
            for (EphemerisEntity e : entities) { list.add(new Ephemeris(e)); }
            mainHandler.post(() -> callback.onDataLoaded(list));
        });
    }

    public interface LocalDataCallback<T> {
        void onDataLoaded(T data);
    }
}



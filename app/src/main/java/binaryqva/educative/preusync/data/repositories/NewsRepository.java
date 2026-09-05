/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NewsRepository.java
 * Versión: v2.1.0
 * Descripción: Repositorio de noticias con soporte híbrido de red y persistencia
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
import binaryqva.educative.preusync.data.local.dao.NewsDao;
import binaryqva.educative.preusync.data.local.entities.NewsEntity;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.News;
import binaryqva.educative.preusync.network.retrofit.ApiService;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Orquestador de datos para noticias. Gestiona el flujo entre la API y la DB.
 */
public class NewsRepository {

    private final ApiService apiService;
    private final NewsDao newsDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public NewsRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
        this.newsDao = PreuSyncDatabase.getInstance(context).newsDao();
    }

    public void getNewsRange(int start, int count, Callback<ApiResponse<List<News>>> callback) {
        apiService.getNewsRange(start, count).enqueue(new Callback<ApiResponse<List<News>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<News>>> call, @NonNull Response<ApiResponse<List<News>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    saveToLocal(response.body().getData());
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<News>>> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getTopNews(int limit, Callback<ApiResponse<List<News>>> callback) {
        apiService.getTopNews(limit).enqueue(callback);
    }

    public void getNewsById(String id, Callback<ApiResponse<News>> callback) {
        apiService.getNewsById(id).enqueue(callback);
    }

    /**
     * Persiste una lista de noticias en la base de datos local.
     */
    private void saveToLocal(List<News> newsList) {
        if (newsList == null || newsList.isEmpty()) return;
        executor.execute(() -> {
            List<NewsEntity> entities = new ArrayList<>();
            for (News n : newsList) { entities.add(n.toEntity()); }
            newsDao.insertAll(entities);
        });
    }

    /**
     * Recupera las noticias guardadas localmente.
     */
    public void getLocalNews(LocalDataCallback<List<News>> callback) {
        executor.execute(() -> {
            List<NewsEntity> entities = newsDao.getAllNews();
            List<News> news = new ArrayList<>();
            for (NewsEntity e : entities) { news.add(new News(e)); }
            mainHandler.post(() -> callback.onDataLoaded(news));
        });
    }

    public interface LocalDataCallback<T> {
        void onDataLoaded(T data);
    }
}



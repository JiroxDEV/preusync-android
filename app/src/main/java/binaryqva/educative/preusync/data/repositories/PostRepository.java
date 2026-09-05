/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: PostRepository.java
 * Versión: v2.0.0
 * Descripción: Repositorio de publicaciones con soporte híbrido de red y persistencia
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import binaryqva.educative.preusync.data.local.PreuSyncDatabase;
import binaryqva.educative.preusync.data.local.dao.PostDao;
import binaryqva.educative.preusync.data.local.entities.PostEntity;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.network.requests.CreatePostRequest;
import binaryqva.educative.preusync.network.requests.ReportPostRequest;
import binaryqva.educative.preusync.network.requests.VotePostRequest;
import binaryqva.educative.preusync.network.retrofit.ApiService;
import binaryqva.educative.preusync.network.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Orquestador de datos para publicaciones. Gestiona el flujo entre la API y la DB.
 */
public class PostRepository {

    private final ApiService apiService;
    private final PostDao postDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public PostRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
        this.postDao = PreuSyncDatabase.getInstance(context).postDao();
    }

    public void getPostsRange(int start, int count, Callback<ApiResponse<List<Post>>> callback) {
        apiService.getPostsRange(start, count).enqueue(new Callback<ApiResponse<List<Post>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Post>>> call, @NonNull Response<ApiResponse<List<Post>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    saveToLocal(response.body().getData());
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Post>>> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    public void getTopPosts(int limit, Callback<ApiResponse<List<Post>>> callback) {
        apiService.getTopPosts(limit).enqueue(callback);
    }

    public void getPostById(String id, Callback<ApiResponse<Post>> callback) {
        apiService.getPostById(id).enqueue(callback);
    }

    public void createPost(CreatePostRequest request, Callback<ApiResponse<Post>> callback) {
        apiService.createPost(request).enqueue(callback);
    }

    public void votePost(String postId, String voteType, Callback<ApiResponse<HashMap<String, Object>>> callback) {
        apiService.votePost(postId, new VotePostRequest(voteType)).enqueue(callback);
    }

    public void deletePost(String postId, Callback<ApiResponse<Void>> callback) {
        apiService.deletePost(postId).enqueue(callback);
    }

    public void reportPost(String postId, Callback<ApiResponse<Void>> callback) {
        apiService.reportPost(postId, new ReportPostRequest("")).enqueue(callback);
    }

    /**
     * Persiste una lista de publicaciones en la base de datos local.
     */
    private void saveToLocal(List<Post> postsList) {
        if (postsList == null || postsList.isEmpty()) return;
        executor.execute(() -> {
            List<PostEntity> entities = new ArrayList<>();
            for (Post p : postsList) { entities.add(p.toEntity()); }
            postDao.insertAll(entities);
        });
    }

    /**
     * Recupera las publicaciones guardadas localmente.
     */
    public void getLocalPosts(LocalDataCallback<List<Post>> callback) {
        executor.execute(() -> {
            List<PostEntity> entities = postDao.getAllPosts();
            List<Post> posts = new ArrayList<>();
            for (PostEntity e : entities) { posts.add(new Post(e)); }
            mainHandler.post(() -> callback.onDataLoaded(posts));
        });
    }

    public interface LocalDataCallback<T> {
        void onDataLoaded(T data);
    }
}



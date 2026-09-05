/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: PostsViewModel.java
 * Versión: v8.0.0
 * Descripción: ViewModel encargado de gestionar el feed de la comunidad. 
 *              Implementa el patrón BaseViewModel y soporte híbrido Room/API.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.data.repositories.PostRepository;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.Post;
import binaryqva.educative.preusync.network.requests.CreatePostRequest;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.PaginationState;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostsViewModel extends BaseViewModel<Post> {

    private static final int PAGE_SIZE = 10;
    private final PostRepository repository;
    private int currentPage = 0;
    private boolean isLoadingMore = false;
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    public PostsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new PostRepository(application);
    }

    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }

    @Override
    public void refresh() {
        currentPage = 0;
        isLoadingMore = false;
        paginationState.setValue(PaginationState.loading());
        loadPage(0, true);
    }

    public void loadMore() {
        PaginationState<Post> current = paginationState.getValue();
        if (current == null || isLoadingMore || !current.hasMore || current.state == PaginationState.STATE_ERROR) return;
        isLoadingMore = true;
        loadPage(currentPage, false);
    }

    private void loadPage(int page, boolean isRefresh) {
        if (!AppUtils.isConnected(getApplication())) {
            if (isRefresh) {
                repository.getLocalPosts(localData -> {
                    if (localData != null && !localData.isEmpty()) {
                        paginationState.setValue(PaginationState.content(localData, false));
                    } else {
                        paginationState.setValue(PaginationState.error("Sin conexión y sin datos locales"));
                    }
                });
            }
            isLoadingMore = false;
            return;
        }

        int start = page * PAGE_SIZE;
        repository.getPostsRange(start, PAGE_SIZE, new Callback<ApiResponse<List<Post>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Post>>> call, @NonNull Response<ApiResponse<List<Post>>> response) {
                isLoadingMore = false;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateState(response.body().getData(), isRefresh);
                } else {
                    paginationState.setValue(PaginationState.error("Error del servidor"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Post>>> call, @NonNull Throwable t) {
                isLoadingMore = false;
                if (isRefresh) {
                    repository.getLocalPosts(localData -> {
                        if (localData != null && !localData.isEmpty()) {
                            paginationState.setValue(PaginationState.content(localData, false));
                        } else {
                            paginationState.setValue(PaginationState.error(t.getMessage()));
                        }
                    });
                }
            }
        });
    }

    private void updateState(List<Post> newItems, boolean isRefresh) {
        PaginationState<Post> current = paginationState.getValue();
        List<Post> allItems = (isRefresh || current == null) ? new ArrayList<>() : new ArrayList<>(current.items);
        allItems.addAll(newItems);
        if (isRefresh) currentPage = 1; else currentPage++;
        if (allItems.isEmpty()) paginationState.setValue(PaginationState.empty());
        else paginationState.setValue(PaginationState.content(allItems, newItems.size() >= PAGE_SIZE));
    }

    public void createPost(String title, String details, String imageBase64) {
        CreatePostRequest request = new CreatePostRequest(title, details);
        if (imageBase64 != null) request.setImageBase64(imageBase64);
        repository.createPost(request, new Callback<ApiResponse<Post>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Post>> call, @NonNull Response<ApiResponse<Post>> response) {
                operationSuccess.setValue(response.isSuccessful());
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Post>> call, @NonNull Throwable t) {
                operationSuccess.setValue(false);
            }
        });
    }

    public void deletePost(String id) {
        repository.deletePost(id, new Callback<ApiResponse<Void>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                operationSuccess.setValue(response.isSuccessful());
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                operationSuccess.setValue(false);
            }
        });
    }

    public void reportPost(String id) {
        repository.reportPost(id, new Callback<ApiResponse<Void>>() {
            @Override public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                operationSuccess.setValue(response.isSuccessful());
            }
            @Override public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                operationSuccess.setValue(false);
            }
        });
    }
}



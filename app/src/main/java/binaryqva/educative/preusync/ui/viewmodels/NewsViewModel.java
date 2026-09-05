/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NewsViewModel.java
 * Versión: v8.0.0
 * Descripción: ViewModel encargado de gestionar el feed de noticias. 
 *              Implementa el patrón BaseViewModel y soporte híbrido Room/API.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import binaryqva.educative.preusync.data.repositories.NewsRepository;
import binaryqva.educative.preusync.network.models.ApiResponse;
import binaryqva.educative.preusync.network.models.News;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.common.PaginationState;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsViewModel extends BaseViewModel<News> {

    private static final int PAGE_SIZE = 10;
    private final NewsRepository repository;
    private int currentPage = 0;
    private boolean isLoadingMore = false;

    public NewsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new NewsRepository(application);
    }

    @Override
    public void refresh() {
        currentPage = 0;
        isLoadingMore = false;
        paginationState.setValue(PaginationState.loading());
        loadPage(0, true);
    }

    public void loadMore() {
        PaginationState<News> current = paginationState.getValue();
        if (current == null || isLoadingMore || !current.hasMore || current.state == PaginationState.STATE_ERROR) return;
        isLoadingMore = true;
        loadPage(currentPage, false);
    }

    private void loadPage(int page, boolean isRefresh) {
        if (!AppUtils.isConnected(getApplication())) {
            if (isRefresh) {
                repository.getLocalNews(localData -> {
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
        repository.getNewsRange(start, PAGE_SIZE, new Callback<ApiResponse<List<News>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<News>>> call, @NonNull Response<ApiResponse<List<News>>> response) {
                isLoadingMore = false;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateState(response.body().getData(), isRefresh);
                } else {
                    paginationState.setValue(PaginationState.error("Error del servidor"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<News>>> call, @NonNull Throwable t) {
                isLoadingMore = false;
                if (isRefresh) {
                    repository.getLocalNews(localData -> {
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

    private void updateState(List<News> newItems, boolean isRefresh) {
        PaginationState<News> current = paginationState.getValue();
        List<News> allItems = (isRefresh || current == null) ? new ArrayList<>() : new ArrayList<>(current.items);
        allItems.addAll(newItems);
        
        if (isRefresh) currentPage = 1; else currentPage++;
        
        if (allItems.isEmpty()) paginationState.setValue(PaginationState.empty());
        else paginationState.setValue(PaginationState.content(allItems, newItems.size() >= PAGE_SIZE));
    }
}


